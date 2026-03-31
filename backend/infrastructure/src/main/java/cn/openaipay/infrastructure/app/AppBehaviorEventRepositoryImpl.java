package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppBehaviorEvent;
import cn.openaipay.domain.app.model.AppBehaviorEventQuery;
import cn.openaipay.domain.app.model.AppBehaviorEventReportRow;
import cn.openaipay.domain.app.model.AppBehaviorEventStats;
import cn.openaipay.domain.app.model.AppBehaviorMetricItem;
import cn.openaipay.domain.app.repository.AppBehaviorEventRepository;
import cn.openaipay.infrastructure.app.dataobject.AppBehaviorEventDO;
import cn.openaipay.infrastructure.app.mapper.AppBehaviorEventMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * App 行为埋点仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Repository
public class AppBehaviorEventRepositoryImpl implements AppBehaviorEventRepository {

    /** 默认统计标签值。 */
    private static final String UNKNOWN_LABEL = "UNKNOWN";
    /** 成功状态匹配条件。 */
    private static final String SUCCESS_STATUS_CONDITION =
            "UPPER(COALESCE(result_status,'')) IN ('SUCCESS','SUCCEEDED','OK')";
    /** 失败状态匹配条件。 */
    private static final String FAILURE_STATUS_CONDITION =
            "UPPER(COALESCE(result_status,'')) IN ('FAIL','FAILED','ERROR','REJECTED','DENIED','CANCELED','CANCELLED')";

    /** 埋点Mapper。 */
    private final AppBehaviorEventMapper appBehaviorEventMapper;

    public AppBehaviorEventRepositoryImpl(AppBehaviorEventMapper appBehaviorEventMapper) {
        this.appBehaviorEventMapper = appBehaviorEventMapper;
    }

    @Override
    public AppBehaviorEvent save(AppBehaviorEvent event) {
        AppBehaviorEventDO entity = new AppBehaviorEventDO();
        entity.setEventId(event.getEventId());
        entity.setSessionId(event.getSessionId());
        entity.setAppCode(event.getAppCode());
        entity.setEventName(event.getEventName());
        entity.setEventType(event.getEventType());
        entity.setEventCode(event.getEventCode());
        entity.setPageName(event.getPageName());
        entity.setActionName(event.getActionName());
        entity.setResultStatus(event.getResultStatus());
        entity.setTraceId(event.getTraceId());
        entity.setDeviceId(event.getDeviceId());
        entity.setClientId(event.getClientId());
        entity.setUserId(event.getUserId());
        entity.setAipayUid(event.getAipayUid());
        entity.setLoginId(event.getLoginId());
        entity.setAccountStatus(event.getAccountStatus());
        entity.setKycLevel(event.getKycLevel());
        entity.setNickname(event.getNickname());
        entity.setMobile(event.getMobile());
        entity.setIpAddress(event.getIpAddress());
        entity.setLocationInfo(event.getLocationInfo());
        entity.setTenantCode(event.getTenantCode());
        entity.setNetworkType(event.getNetworkType());
        entity.setAppVersionNo(event.getAppVersionNo());
        entity.setAppBuildNo(event.getAppBuildNo());
        entity.setDeviceBrand(event.getDeviceBrand());
        entity.setDeviceModel(event.getDeviceModel());
        entity.setDeviceName(event.getDeviceName());
        entity.setDeviceType(event.getDeviceType());
        entity.setOsName(event.getOsName());
        entity.setOsVersion(event.getOsVersion());
        entity.setLocale(event.getLocale());
        entity.setTimezone(event.getTimezone());
        entity.setLanguage(event.getLanguage());
        entity.setCountryCode(event.getCountryCode());
        entity.setCarrierName(event.getCarrierName());
        entity.setScreenWidth(event.getScreenWidth());
        entity.setScreenHeight(event.getScreenHeight());
        entity.setViewportWidth(event.getViewportWidth());
        entity.setViewportHeight(event.getViewportHeight());
        entity.setDurationMs(event.getDurationMs());
        entity.setLoginDurationMs(event.getLoginDurationMs());
        entity.setEventOccurredAt(event.getEventOccurredAt());
        entity.setPayloadJson(event.getPayloadJson());
        entity.setCreatedAt(event.getCreatedAt());
        return toDomain(appBehaviorEventMapper.save(entity));
    }

    /**
     * 按条件查询埋点明细。
     */
    @Override
    public List<AppBehaviorEvent> listByQuery(AppBehaviorEventQuery query) {
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.orderByDesc("event_occurred_at", "id");
        wrapper.last("LIMIT " + normalizeLimit(query.limit(), 50));
        return appBehaviorEventMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    /**
     * 按条件聚合埋点统计数据。
     */
    @Override
    public AppBehaviorEventStats summarize(AppBehaviorEventQuery query, int topLimit) {
        long totalCount = safeCount(appBehaviorEventMapper.selectCount(buildBaseWrapper(query)));
        if (totalCount <= 0) {
            return new AppBehaviorEventStats(
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    null,
                    null,
                    List.of(),
                    List.of()
            );
        }

        long uniqueDeviceCount = querySingleLong(query, "COUNT(DISTINCT device_id)");
        long uniqueUserCount = querySingleLong(
                query,
                "COUNT(DISTINCT CASE WHEN user_id IS NULL OR user_id <= 0 THEN NULL ELSE user_id END)"
        );
        long successCount = safeCount(appBehaviorEventMapper.selectCount(buildStatusWrapper(query, SUCCESS_STATUS_CONDITION)));
        long failureCount = safeCount(appBehaviorEventMapper.selectCount(buildStatusWrapper(query, FAILURE_STATUS_CONDITION)));
        BigDecimal avgDurationMs = queryAverageDuration(query);
        LocalDateTime firstOccurredAt = queryBoundaryTime(query, true);
        LocalDateTime lastOccurredAt = queryBoundaryTime(query, false);
        List<AppBehaviorMetricItem> eventTypeDistribution = queryDistribution(query, "event_type", topLimit);
        List<AppBehaviorMetricItem> topEventDistribution = queryDistribution(query, "event_name", topLimit);

        return new AppBehaviorEventStats(
                totalCount,
                uniqueDeviceCount,
                uniqueUserCount,
                successCount,
                failureCount,
                avgDurationMs,
                firstOccurredAt,
                lastOccurredAt,
                eventTypeDistribution,
                topEventDistribution
        );
    }

    /**
     * 按条件生成埋点报表行。
     */
    @Override
    public List<AppBehaviorEventReportRow> listReportRows(AppBehaviorEventQuery query) {
        String eventTypeExpr = normalizedTextExpression("event_type");
        String eventNameExpr = normalizedTextExpression("event_name");
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.select(
                "DATE(event_occurred_at) AS stat_date",
                eventTypeExpr + " AS event_type",
                eventNameExpr + " AS event_name",
                "COUNT(1) AS total_count",
                "SUM(CASE WHEN " + SUCCESS_STATUS_CONDITION + " THEN 1 ELSE 0 END) AS success_count",
                "SUM(CASE WHEN " + FAILURE_STATUS_CONDITION + " THEN 1 ELSE 0 END) AS failure_count",
                "COUNT(DISTINCT device_id) AS device_count",
                "COUNT(DISTINCT CASE WHEN user_id IS NULL OR user_id <= 0 THEN NULL ELSE user_id END) AS user_count",
                "ROUND(AVG(duration_ms), 2) AS avg_duration_ms",
                "MAX(duration_ms) AS max_duration_ms"
        );
        wrapper.groupBy("DATE(event_occurred_at)", eventTypeExpr, eventNameExpr);
        wrapper.orderByDesc("DATE(event_occurred_at)", "total_count");
        wrapper.last("LIMIT " + normalizeLimit(query.limit(), 200));
        return appBehaviorEventMapper.selectMaps(wrapper).stream().map(this::toReportRow).toList();
    }

    private QueryWrapper<AppBehaviorEventDO> buildBaseWrapper(AppBehaviorEventQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        QueryWrapper<AppBehaviorEventDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", query.appCode());
        eqIfPresent(wrapper, "event_type", query.eventType());
        eqIfPresent(wrapper, "event_name", query.eventName());
        eqIfPresent(wrapper, "page_name", query.pageName());
        eqIfPresent(wrapper, "device_id", query.deviceId());
        if (query.userId() != null && query.userId() > 0) {
            wrapper.eq("user_id", query.userId());
        }
        if (query.startAt() != null) {
            wrapper.ge("event_occurred_at", query.startAt());
        }
        if (query.endAt() != null) {
            wrapper.le("event_occurred_at", query.endAt());
        }
        return wrapper;
    }

    private QueryWrapper<AppBehaviorEventDO> buildStatusWrapper(AppBehaviorEventQuery query, String statusConditionSql) {
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.apply(statusConditionSql);
        return wrapper;
    }

    private BigDecimal queryAverageDuration(AppBehaviorEventQuery query) {
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.select("AVG(duration_ms)");
        wrapper.isNotNull("duration_ms");
        Object value = firstObject(appBehaviorEventMapper.selectObjs(wrapper));
        BigDecimal decimal = toBigDecimal(value);
        if (decimal == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime queryBoundaryTime(AppBehaviorEventQuery query, boolean first) {
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.select(first ? "MIN(event_occurred_at)" : "MAX(event_occurred_at)");
        return toLocalDateTime(firstObject(appBehaviorEventMapper.selectObjs(wrapper)));
    }

    private long querySingleLong(AppBehaviorEventQuery query, String selectExpression) {
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.select(selectExpression);
        return toLong(firstObject(appBehaviorEventMapper.selectObjs(wrapper)));
    }

    private List<AppBehaviorMetricItem> queryDistribution(AppBehaviorEventQuery query, String columnName, int topLimit) {
        String normalizedExpr = normalizedTextExpression(columnName);
        QueryWrapper<AppBehaviorEventDO> wrapper = buildBaseWrapper(query);
        wrapper.select(
                normalizedExpr + " AS metric_key",
                "COUNT(1) AS metric_count"
        );
        wrapper.groupBy(normalizedExpr);
        wrapper.orderByDesc("metric_count");
        wrapper.last("LIMIT " + Math.max(1, topLimit));
        return appBehaviorEventMapper.selectMaps(wrapper).stream()
                .map(row -> new AppBehaviorMetricItem(
                        normalizeMetricKey(readMapText(row, "metric_key")),
                        readMapLong(row, "metric_count")
                ))
                .toList();
    }

    private AppBehaviorEventReportRow toReportRow(Map<String, Object> row) {
        return new AppBehaviorEventReportRow(
                toLocalDate(readMapValue(row, "stat_date")),
                normalizeMetricKey(readMapText(row, "event_type")),
                normalizeMetricKey(readMapText(row, "event_name")),
                readMapLong(row, "total_count"),
                readMapLong(row, "success_count"),
                readMapLong(row, "failure_count"),
                readMapLong(row, "device_count"),
                readMapLong(row, "user_count"),
                readMapDecimal(row, "avg_duration_ms"),
                readMapNullableLong(row, "max_duration_ms")
        );
    }

    private String normalizedTextExpression(String columnName) {
        return "COALESCE(NULLIF(TRIM(" + columnName + "), ''), '" + UNKNOWN_LABEL + "')";
    }

    private String normalizeMetricKey(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN_LABEL;
        }
        return value.trim();
    }

    private int normalizeLimit(Integer limit, int defaultLimit) {
        if (limit == null || limit <= 0) {
            return defaultLimit;
        }
        return limit;
    }

    private void eqIfPresent(QueryWrapper<AppBehaviorEventDO> wrapper, String column, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        wrapper.eq(column, value.trim());
    }

    private long safeCount(Long count) {
        return count == null ? 0L : Math.max(0L, count);
    }

    private Object firstObject(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private String readMapText(Map<String, Object> row, String key) {
        Object value = readMapValue(row, key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    private long readMapLong(Map<String, Object> row, String key) {
        return toLong(readMapValue(row, key));
    }

    private Long readMapNullableLong(Map<String, Object> row, String key) {
        Object value = readMapValue(row, key);
        if (value == null) {
            return null;
        }
        return toLong(value);
    }

    private BigDecimal readMapDecimal(Map<String, Object> row, String key) {
        BigDecimal decimal = toBigDecimal(readMapValue(row, key));
        if (decimal == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    private Object readMapValue(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty() || key == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return new BigDecimal(String.valueOf(value).trim()).longValue();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private AppBehaviorEvent toDomain(AppBehaviorEventDO entity) {
        return new AppBehaviorEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getSessionId(),
                entity.getAppCode(),
                entity.getEventName(),
                entity.getEventType(),
                entity.getEventCode(),
                entity.getPageName(),
                entity.getActionName(),
                entity.getResultStatus(),
                entity.getTraceId(),
                entity.getDeviceId(),
                entity.getClientId(),
                entity.getUserId(),
                entity.getAipayUid(),
                entity.getLoginId(),
                entity.getAccountStatus(),
                entity.getKycLevel(),
                entity.getNickname(),
                entity.getMobile(),
                entity.getIpAddress(),
                entity.getLocationInfo(),
                entity.getTenantCode(),
                entity.getNetworkType(),
                entity.getAppVersionNo(),
                entity.getAppBuildNo(),
                entity.getDeviceBrand(),
                entity.getDeviceModel(),
                entity.getDeviceName(),
                entity.getDeviceType(),
                entity.getOsName(),
                entity.getOsVersion(),
                entity.getLocale(),
                entity.getTimezone(),
                entity.getLanguage(),
                entity.getCountryCode(),
                entity.getCarrierName(),
                entity.getScreenWidth(),
                entity.getScreenHeight(),
                entity.getViewportWidth(),
                entity.getViewportHeight(),
                entity.getDurationMs(),
                entity.getLoginDurationMs(),
                entity.getEventOccurredAt(),
                entity.getPayloadJson(),
                entity.getCreatedAt()
        );
    }
}
