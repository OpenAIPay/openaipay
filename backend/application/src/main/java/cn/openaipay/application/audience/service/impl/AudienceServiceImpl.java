package cn.openaipay.application.audience.service.impl;

import cn.openaipay.application.audience.command.UpsertAudienceSegmentCommand;
import cn.openaipay.application.audience.command.UpsertAudienceSegmentRuleCommand;
import cn.openaipay.application.audience.command.UpsertAudienceTagDefinitionCommand;
import cn.openaipay.application.audience.command.UpsertAudienceUserTagCommand;
import cn.openaipay.application.audience.dto.AudienceSegmentDTO;
import cn.openaipay.application.audience.dto.AudienceSegmentRuleDTO;
import cn.openaipay.application.audience.dto.AudienceTagDefinitionDTO;
import cn.openaipay.application.audience.dto.AudienceUserTagDTO;
import cn.openaipay.application.audience.service.AudienceService;
import cn.openaipay.domain.audience.model.AudienceRuleOperator;
import cn.openaipay.domain.audience.model.AudienceRuleRelation;
import cn.openaipay.domain.audience.model.AudienceSegment;
import cn.openaipay.domain.audience.model.AudienceSegmentRule;
import cn.openaipay.domain.audience.model.AudienceSegmentStatus;
import cn.openaipay.domain.audience.model.AudienceTagDefinition;
import cn.openaipay.domain.audience.model.AudienceTagType;
import cn.openaipay.domain.audience.model.AudienceUserTagSnapshot;
import cn.openaipay.domain.audience.repository.AudienceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人群服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Service
public class AudienceServiceImpl implements AudienceService {

    /** 人群仓储 */
    private final AudienceRepository audienceRepository;

    public AudienceServiceImpl(AudienceRepository audienceRepository) {
        this.audienceRepository = audienceRepository;
    }

    /**
     * 查询标签定义
     */
    @Override
    public List<AudienceTagDefinitionDTO> listTagDefinitions() {
        return audienceRepository.listTagDefinitions().stream()
                .sorted(Comparator.comparing(AudienceTagDefinition::tagCode))
                .map(this::toTagDefinitionDTO)
                .toList();
    }

    /**
     * 保存标签定义
     */
    @Override
    @Transactional
    public AudienceTagDefinitionDTO saveTagDefinition(UpsertAudienceTagDefinitionCommand command) {
        String tagCode = required(command.tagCode(), "tagCode");
        AudienceTagDefinition existing = audienceRepository.findTagDefinitionByCode(tagCode).orElse(null);
        AudienceTagDefinition saved = audienceRepository.saveTagDefinition(new AudienceTagDefinition(
                existing == null ? null : existing.id(),
                tagCode,
                required(command.tagName(), "tagName"),
                parseEnum(command.tagType(), AudienceTagType.class, AudienceTagType.ENUM),
                trimToNull(command.valueScope()),
                trimToNull(command.description()),
                command.enabled() == null || command.enabled(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toTagDefinitionDTO(saved);
    }

    /**
     * 查询人群定义
     */
    @Override
    public List<AudienceSegmentDTO> listSegments() {
        return audienceRepository.listSegments().stream()
                .sorted(Comparator.comparing(AudienceSegment::segmentCode))
                .map(this::toSegmentDTO)
                .toList();
    }

    /**
     * 保存人群定义
     */
    @Override
    @Transactional
    public AudienceSegmentDTO saveSegment(UpsertAudienceSegmentCommand command) {
        String segmentCode = required(command.segmentCode(), "segmentCode");
        AudienceSegment existing = audienceRepository.findSegmentByCode(segmentCode).orElse(null);
        AudienceSegment saved = audienceRepository.saveSegment(new AudienceSegment(
                existing == null ? null : existing.id(),
                segmentCode,
                required(command.segmentName(), "segmentName"),
                trimToNull(command.description()),
                trimToNull(command.sceneCode()),
                parseEnum(command.status(), AudienceSegmentStatus.class, existing == null ? AudienceSegmentStatus.DRAFT : existing.status()),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toSegmentDTO(saved);
    }

    /**
     * 查询人群规则
     */
    @Override
    public List<AudienceSegmentRuleDTO> listSegmentRules(String segmentCode) {
        String normalizedSegmentCode = required(segmentCode, "segmentCode");
        return audienceRepository.listSegmentRules(normalizedSegmentCode).stream()
                .sorted(Comparator.comparing(AudienceSegmentRule::ruleCode))
                .map(this::toSegmentRuleDTO)
                .toList();
    }

    /**
     * 保存人群规则
     */
    @Override
    @Transactional
    public AudienceSegmentRuleDTO saveSegmentRule(UpsertAudienceSegmentRuleCommand command) {
        String segmentCode = required(command.segmentCode(), "segmentCode");
        audienceRepository.findSegmentByCode(segmentCode)
                .orElseThrow(() -> new IllegalArgumentException("segment not found: " + segmentCode));
        String tagCode = required(command.tagCode(), "tagCode");
        audienceRepository.findTagDefinitionByCode(tagCode)
                .orElseThrow(() -> new IllegalArgumentException("tagDefinition not found: " + tagCode));

        String ruleCode = required(command.ruleCode(), "ruleCode");
        AudienceSegmentRule existing = audienceRepository.listSegmentRules(segmentCode).stream()
                .filter(rule -> Objects.equals(rule.ruleCode(), ruleCode))
                .findFirst()
                .orElse(null);
        AudienceSegmentRule saved = audienceRepository.saveSegmentRule(new AudienceSegmentRule(
                existing == null ? null : existing.id(),
                ruleCode,
                segmentCode,
                tagCode,
                parseEnum(command.operator(), AudienceRuleOperator.class, AudienceRuleOperator.EQ),
                trimToNull(command.targetValue()),
                parseEnum(command.relation(), AudienceRuleRelation.class, AudienceRuleRelation.INCLUDE),
                command.enabled() == null || command.enabled(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toSegmentRuleDTO(saved);
    }

    /**
     * 查询用户标签
     */
    @Override
    public List<AudienceUserTagDTO> listUserTags(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return audienceRepository.listUserTags(userId).stream()
                .sorted(Comparator.comparing(AudienceUserTagSnapshot::tagCode))
                .map(this::toUserTagDTO)
                .toList();
    }

    /**
     * 保存用户标签
     */
    @Override
    @Transactional
    public AudienceUserTagDTO saveUserTag(UpsertAudienceUserTagCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String tagCode = required(command.tagCode(), "tagCode");
        audienceRepository.findTagDefinitionByCode(tagCode)
                .orElseThrow(() -> new IllegalArgumentException("tagDefinition not found: " + tagCode));
        String normalizedValue = required(command.tagValue(), "tagValue");

        AudienceUserTagSnapshot existing = audienceRepository.listUserTags(command.userId()).stream()
                .filter(snapshot -> Objects.equals(snapshot.tagCode(), tagCode))
                .findFirst()
                .orElse(null);
        AudienceUserTagSnapshot saved = audienceRepository.saveUserTag(new AudienceUserTagSnapshot(
                existing == null ? null : existing.id(),
                command.userId(),
                tagCode,
                normalizedValue,
                trimToNull(command.source()),
                command.valueUpdatedAt() == null ? LocalDateTime.now() : command.valueUpdatedAt(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toUserTagDTO(saved);
    }

    /**
     * 解析用户标签token
     */
    @Override
    public Set<String> resolveUserTagTokens(Long userId) {
        if (userId == null || userId <= 0) {
            return Set.of();
        }
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (AudienceUserTagSnapshot userTag : audienceRepository.listUserTags(userId)) {
            String tagCode = trimToNull(userTag.tagCode());
            String tagValue = trimToNull(userTag.tagValue());
            if (tagCode == null || tagValue == null) {
                continue;
            }
            tokens.add(tagCode);
            tokens.add(tagCode + ":" + tagValue);
        }
        return tokens.isEmpty() ? Set.of() : Set.copyOf(tokens);
    }

    /**
     * 计算用户命中人群
     */
    @Override
    public Set<String> matchPublishedSegmentCodes(Long userId) {
        if (userId == null || userId <= 0) {
            return Set.of();
        }
        Map<String, String> userTagValueMap = toTagValueMap(audienceRepository.listUserTags(userId));
        if (userTagValueMap.isEmpty()) {
            return Set.of();
        }
        List<AudienceSegment> publishedSegments = audienceRepository.listPublishedSegments();
        if (publishedSegments.isEmpty()) {
            return Set.of();
        }

        List<String> segmentCodes = publishedSegments.stream().map(AudienceSegment::segmentCode).toList();
        Map<String, List<AudienceSegmentRule>> segmentRuleMap = groupRulesBySegmentCode(
                audienceRepository.listSegmentRulesBySegmentCodes(segmentCodes)
        );

        LinkedHashSet<String> matched = new LinkedHashSet<>();
        for (AudienceSegment segment : publishedSegments) {
            if (matchesSegment(userTagValueMap, segmentRuleMap.get(segment.segmentCode()))) {
                matched.add(segment.segmentCode());
            }
        }
        return matched.isEmpty() ? Set.of() : Set.copyOf(matched);
    }

    private Map<String, String> toTagValueMap(Collection<AudienceUserTagSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (AudienceUserTagSnapshot snapshot : snapshots) {
            String tagCode = trimToNull(snapshot.tagCode());
            String tagValue = trimToNull(snapshot.tagValue());
            if (tagCode == null || tagValue == null) {
                continue;
            }
            map.put(tagCode, tagValue);
        }
        return map;
    }

    private Map<String, List<AudienceSegmentRule>> groupRulesBySegmentCode(Collection<AudienceSegmentRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, List<AudienceSegmentRule>> grouped = new LinkedHashMap<>();
        for (AudienceSegmentRule rule : rules) {
            if (!rule.enabled()) {
                continue;
            }
            grouped.computeIfAbsent(rule.segmentCode(), key -> new ArrayList<>()).add(rule);
        }
        return grouped;
    }

    private boolean matchesSegment(Map<String, String> userTagValueMap, List<AudienceSegmentRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }
        List<AudienceSegmentRule> includeRules = rules.stream()
                .filter(rule -> rule.relation() == AudienceRuleRelation.INCLUDE)
                .toList();
        if (includeRules.isEmpty()) {
            return false;
        }
        boolean includeMatched = includeRules.stream().allMatch(rule -> matchesRule(userTagValueMap, rule));
        if (!includeMatched) {
            return false;
        }
        return rules.stream()
                .filter(rule -> rule.relation() == AudienceRuleRelation.EXCLUDE)
                .noneMatch(rule -> matchesRule(userTagValueMap, rule));
    }

    private boolean matchesRule(Map<String, String> userTagValueMap, AudienceSegmentRule rule) {
        String actualValue = trimToNull(userTagValueMap.get(rule.tagCode()));
        String targetValue = trimToNull(rule.targetValue());
        return switch (rule.operator()) {
            case EXISTS -> actualValue != null;
            case NOT_EXISTS -> actualValue == null;
            case EQ -> actualValue != null && targetValue != null && actualValue.equals(targetValue);
            case NEQ -> actualValue == null || targetValue == null || !actualValue.equals(targetValue);
            case IN -> actualValue != null && splitCsv(targetValue).contains(actualValue);
            case NOT_IN -> actualValue == null || !splitCsv(targetValue).contains(actualValue);
            case CONTAINS -> actualValue != null && targetValue != null && actualValue.contains(targetValue);
            case GT -> compareNumber(actualValue, targetValue) > 0;
            case GTE -> compareNumber(actualValue, targetValue) >= 0;
            case LT -> compareNumber(actualValue, targetValue) < 0;
            case LTE -> compareNumber(actualValue, targetValue) <= 0;
            case BETWEEN -> inBetween(actualValue, targetValue);
        };
    }

    private int compareNumber(String actualValue, String targetValue) {
        if (actualValue == null || targetValue == null) {
            return Integer.MIN_VALUE;
        }
        try {
            return new BigDecimal(actualValue).compareTo(new BigDecimal(targetValue));
        } catch (NumberFormatException ignore) {
            return Integer.MIN_VALUE;
        }
    }

    private boolean inBetween(String actualValue, String targetValue) {
        if (actualValue == null || targetValue == null) {
            return false;
        }
        String normalized = targetValue.replace("~", ",");
        List<String> range = splitCsv(normalized);
        if (range.size() != 2) {
            return false;
        }
        try {
            BigDecimal actual = new BigDecimal(actualValue);
            BigDecimal lower = new BigDecimal(range.get(0));
            BigDecimal upper = new BigDecimal(range.get(1));
            return actual.compareTo(lower) >= 0 && actual.compareTo(upper) <= 0;
        } catch (NumberFormatException ignore) {
            return false;
        }
    }

    private List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(",")).stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private AudienceTagDefinitionDTO toTagDefinitionDTO(AudienceTagDefinition tagDefinition) {
        return new AudienceTagDefinitionDTO(
                tagDefinition.tagCode(),
                tagDefinition.tagName(),
                tagDefinition.tagType().name(),
                tagDefinition.valueScope(),
                tagDefinition.description(),
                tagDefinition.enabled(),
                tagDefinition.createdAt(),
                tagDefinition.updatedAt()
        );
    }

    private AudienceSegmentDTO toSegmentDTO(AudienceSegment segment) {
        return new AudienceSegmentDTO(
                segment.segmentCode(),
                segment.segmentName(),
                segment.description(),
                segment.sceneCode(),
                segment.status().name(),
                segment.createdAt(),
                segment.updatedAt()
        );
    }

    private AudienceSegmentRuleDTO toSegmentRuleDTO(AudienceSegmentRule segmentRule) {
        return new AudienceSegmentRuleDTO(
                segmentRule.ruleCode(),
                segmentRule.segmentCode(),
                segmentRule.tagCode(),
                segmentRule.operator().name(),
                segmentRule.targetValue(),
                segmentRule.relation().name(),
                segmentRule.enabled(),
                segmentRule.createdAt(),
                segmentRule.updatedAt()
        );
    }

    private AudienceUserTagDTO toUserTagDTO(AudienceUserTagSnapshot userTagSnapshot) {
        return new AudienceUserTagDTO(
                userTagSnapshot.userId(),
                userTagSnapshot.tagCode(),
                userTagSnapshot.tagValue(),
                userTagSnapshot.source(),
                userTagSnapshot.valueUpdatedAt(),
                userTagSnapshot.createdAt(),
                userTagSnapshot.updatedAt()
        );
    }

    private String required(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <T extends Enum<T>> T parseEnum(String raw, Class<T> enumType, T defaultValue) {
        String normalized = trimToNull(raw);
        if (normalized == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid enum value: " + raw);
        }
    }
}
