package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppClientType;
import cn.openaipay.domain.app.model.AppVisitRecord;
import cn.openaipay.domain.app.repository.AppVisitRecordRepository;
import cn.openaipay.infrastructure.app.dataobject.AppVisitRecordDO;
import cn.openaipay.infrastructure.app.mapper.AppVisitRecordMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * App 访问记录仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Repository
public class AppVisitRecordRepositoryImpl implements AppVisitRecordRepository {

    /** 应用访问记录信息 */
    private final AppVisitRecordMapper appVisitRecordMapper;

    public AppVisitRecordRepositoryImpl(AppVisitRecordMapper appVisitRecordMapper) {
        this.appVisitRecordMapper = appVisitRecordMapper;
    }

    @Override
    public AppVisitRecord save(AppVisitRecord record) {
        AppVisitRecordDO entity = new AppVisitRecordDO();
        entity.setDeviceId(record.getDeviceId());
        entity.setAppCode(record.getAppCode());
        entity.setClientId(record.getClientId());
        entity.setIpAddress(record.getIpAddress());
        entity.setLocationInfo(record.getLocationInfo());
        entity.setTenantCode(record.getTenantCode());
        entity.setClientType(record.getClientType().name());
        entity.setNetworkType(record.getNetworkType());
        entity.setAppVersionId(record.getAppVersionId());
        entity.setDeviceBrand(record.getDeviceBrand());
        entity.setOsVersion(record.getOsVersion());
        entity.setApiName(record.getApiName());
        entity.setRequestParamsText(record.getRequestParamsText());
        entity.setCalledAt(record.getCalledAt());
        entity.setResultSummary(record.getResultSummary());
        entity.setDurationMs(record.getDurationMs());
        entity.setCreatedAt(record.getCalledAt());
        return toDomain(appVisitRecordMapper.save(entity));
    }

    /**
     * 按设备ID查询记录列表。
     */
    @Override
    public List<AppVisitRecord> listRecentByDeviceId(String deviceId, int limit) {
        return appVisitRecordMapper.listRecentByDeviceId(deviceId, limit).stream().map(this::toDomain).toList();
    }

    private AppVisitRecord toDomain(AppVisitRecordDO entity) {
        return new AppVisitRecord(
                entity.getId(),
                entity.getDeviceId(),
                entity.getAppCode(),
                entity.getClientId(),
                entity.getIpAddress(),
                entity.getLocationInfo(),
                entity.getTenantCode(),
                AppClientType.fromCode(entity.getClientType()),
                entity.getNetworkType(),
                entity.getAppVersionId(),
                entity.getDeviceBrand(),
                entity.getOsVersion(),
                entity.getApiName(),
                entity.getRequestParamsText(),
                entity.getCalledAt(),
                entity.getResultSummary(),
                entity.getDurationMs()
        );
    }
}
