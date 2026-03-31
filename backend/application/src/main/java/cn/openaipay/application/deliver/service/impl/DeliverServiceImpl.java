package cn.openaipay.application.deliver.service.impl;

import cn.openaipay.application.audience.facade.AudienceFacade;
import cn.openaipay.application.deliver.command.DeliverCommand;
import cn.openaipay.application.deliver.command.DeliverEventCommand;
import cn.openaipay.application.deliver.dto.DeliverCreativeDTO;
import cn.openaipay.application.deliver.dto.DeliverPositionDTO;
import cn.openaipay.application.deliver.processor.DeliverProcessor;
import cn.openaipay.application.deliver.service.DeliverService;
import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.DeliverEventRecord;
import cn.openaipay.domain.deliver.model.DeliverEventType;
import cn.openaipay.domain.deliver.model.DeliverMaterial;
import cn.openaipay.domain.deliver.model.Position;
import cn.openaipay.domain.deliver.repository.DeliverEventRecordRepository;
import cn.openaipay.domain.deliver.repository.DeliverMaterialRepository;
import cn.openaipay.domain.deliver.repository.PositionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 投放应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Service
public class DeliverServiceImpl implements DeliverService {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(DeliverServiceImpl.class);

    /** 人群门面 */
    private final AudienceFacade audienceFacade;
    /** 位置信息 */
    private final PositionRepository positionRepository;
    /** 投放信息 */
    private final DeliverMaterialRepository deliverMaterialRepository;
    /** 投放事件记录信息 */
    private final DeliverEventRecordRepository deliverEventRecordRepository;
    /** 投放信息 */
    private final List<DeliverProcessor> deliverProcessors;

    public DeliverServiceImpl(AudienceFacade audienceFacade,
                              PositionRepository positionRepository,
                              DeliverMaterialRepository deliverMaterialRepository,
                              DeliverEventRecordRepository deliverEventRecordRepository,
                              List<DeliverProcessor> deliverProcessors) {
        this.audienceFacade = audienceFacade;
        this.positionRepository = positionRepository;
        this.deliverMaterialRepository = deliverMaterialRepository;
        this.deliverEventRecordRepository = deliverEventRecordRepository;
        this.deliverProcessors = deliverProcessors.stream()
                .sorted(Comparator.comparingInt(DeliverProcessor::sort))
                .toList();
    }

    /**
     * 处理投放信息。
     */
    @Override
    @Transactional
    public Map<String, DeliverPositionDTO> deliver(DeliverCommand command) {
        validate(command);
        Set<String> mergedUserTags = mergeUserTags(command.userId(), command.userTags());
        Set<String> matchedAudienceSegmentCodes = resolveAudienceSegmentCodes(command.userId());
        DeliverContext baseContext = new DeliverContext(
                command.clientId(),
                command.userId(),
                command.sceneCode(),
                command.channel(),
                mergedUserTags,
                matchedAudienceSegmentCodes,
                command.requestTime(),
                false
        );
        LinkedHashMap<String, DeliverPositionDTO> result = new LinkedHashMap<>();
        for (String positionCode : command.positionCodeList()) {
            result.put(positionCode, deliverSingle(positionCode, baseContext));
        }
        return result;
    }

    private DeliverPositionDTO deliverSingle(String positionCode, DeliverContext baseContext) {
        Optional<Position> optionalPosition = positionRepository.findPublishedByCode(positionCode, baseContext.requestTime());
        if (optionalPosition.isEmpty()) {
            String scene = resolveScene(baseContext.sceneCode(), "投放查询");
            String request = "positionCode=" + positionCode;
            log.info("[{}]入参：{}", scene, request);
            return DeliverPositionDTO.empty(positionCode);
        }
        Position position = optionalPosition.get();
        Position delivered = runPipeline(position, baseContext.withFallbackMode(false));
        fillMaterials(delivered, baseContext.requestTime());
        boolean fallbackReturned = false;
        if (delivered.creativeCount() <= 0 && position.isNeedFallback()) {
            delivered = runPipeline(position, baseContext.withFallbackMode(true));
            fillMaterials(delivered, baseContext.requestTime());
            fallbackReturned = delivered.creativeCount() > 0;
        }
        recordExposure(delivered, baseContext);
        return toPositionDTO(delivered, fallbackReturned);
    }

    /**
     * 记录事件信息。
     */
    @Override
    @Transactional
    public void recordEvent(DeliverEventCommand command) {
        validate(command);
        LocalDateTime eventTime = command.eventTime();
        List<DeliverEventRecord> eventRecords = new ArrayList<>();
        eventRecords.add(new DeliverEventRecord(
                null,
                command.clientId(),
                command.userId(),
                DeliverEntityType.POSITION,
                command.positionCode(),
                command.positionCode(),
                null,
                null,
                command.eventType(),
                command.sceneCode(),
                command.channel(),
                eventTime,
                eventTime
        ));
        if (command.unitCode() != null) {
            eventRecords.add(new DeliverEventRecord(
                    null,
                    command.clientId(),
                    command.userId(),
                    DeliverEntityType.UNIT,
                    command.unitCode(),
                    command.positionCode(),
                    command.unitCode(),
                    null,
                    command.eventType(),
                    command.sceneCode(),
                    command.channel(),
                    eventTime,
                    eventTime
            ));
        }
        if (command.creativeCode() != null) {
            eventRecords.add(new DeliverEventRecord(
                    null,
                    command.clientId(),
                    command.userId(),
                    DeliverEntityType.CREATIVE,
                    command.creativeCode(),
                    command.positionCode(),
                    command.unitCode(),
                    command.creativeCode(),
                    command.eventType(),
                    command.sceneCode(),
                    command.channel(),
                    eventTime,
                    eventTime
            ));
        }
        deliverEventRecordRepository.saveAll(eventRecords);
    }

    private Position runPipeline(Position sourcePosition, DeliverContext context) {
        Position workingPosition = sourcePosition.copyForDeliver();
        for (DeliverProcessor processor : deliverProcessors) {
            processor.process(workingPosition, context);
            if (workingPosition.creativeCount() <= 0) {
                break;
            }
        }
        return workingPosition;
    }

    private void fillMaterials(Position position, LocalDateTime now) {
        if (position.creativeCount() <= 0) {
            return;
        }
        Set<String> materialCodes = position.getDeliverCreativeList().stream()
                .map(DeliverCreative::getMaterialCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (materialCodes.isEmpty()) {
            position.setDeliverCreativeList(List.of());
            return;
        }
        Map<String, DeliverMaterial> materialMap = deliverMaterialRepository.findByCodes(materialCodes);
        List<DeliverCreative> filledCreatives = position.getDeliverCreativeList().stream()
                .filter(creative -> materialMap.containsKey(creative.getMaterialCode()))
                .filter(creative -> materialMap.get(creative.getMaterialCode()).isPublishedAt(now))
                .peek(creative -> creative.fillMaterial(materialMap.get(creative.getMaterialCode())))
                .toList();
        position.setDeliverCreativeList(filledCreatives);
    }

    private void recordExposure(Position position, DeliverContext context) {
        if (position.creativeCount() <= 0) {
            return;
        }
        LocalDateTime eventTime = context.requestTime();
        List<DeliverEventRecord> eventRecords = new ArrayList<>();
        eventRecords.add(new DeliverEventRecord(
                null,
                context.clientId(),
                context.userId(),
                DeliverEntityType.POSITION,
                position.getPositionCode(),
                position.getPositionCode(),
                null,
                null,
                DeliverEventType.DISPLAY,
                context.sceneCode(),
                context.channel(),
                eventTime,
                eventTime
        ));

        position.getDeliverCreativeList().stream()
                .map(DeliverCreative::getUnitCode)
                .filter(unitCode -> unitCode != null && !unitCode.isBlank())
                .distinct()
                .forEach(unitCode -> eventRecords.add(new DeliverEventRecord(
                        null,
                        context.clientId(),
                        context.userId(),
                        DeliverEntityType.UNIT,
                        unitCode,
                        position.getPositionCode(),
                        unitCode,
                        null,
                        DeliverEventType.DISPLAY,
                        context.sceneCode(),
                        context.channel(),
                        eventTime,
                        eventTime
                )));

        position.getDeliverCreativeList().forEach(creative -> eventRecords.add(new DeliverEventRecord(
                null,
                context.clientId(),
                context.userId(),
                DeliverEntityType.CREATIVE,
                creative.getCreativeCode(),
                position.getPositionCode(),
                creative.getUnitCode(),
                creative.getCreativeCode(),
                DeliverEventType.DISPLAY,
                context.sceneCode(),
                context.channel(),
                eventTime,
                eventTime
        )));

        deliverEventRecordRepository.saveAll(eventRecords);
    }

    private DeliverPositionDTO toPositionDTO(Position position, boolean fallbackReturned) {
        List<DeliverCreativeDTO> creativeDTOList = position.getDeliverCreativeList().stream()
                .map(this::toCreativeDTO)
                .toList();
        return new DeliverPositionDTO(
                position.getPositionCode(),
                position.getPositionName(),
                position.getPositionType() == null ? null : position.getPositionType().name(),
                position.getPreviewImage(),
                position.getSlideInterval(),
                position.getMaxDisplayCount(),
                fallbackReturned,
                creativeDTOList
        );
    }

    private DeliverCreativeDTO toCreativeDTO(DeliverCreative creative) {
        return new DeliverCreativeDTO(
                creative.getCreativeCode(),
                creative.getCreativeName(),
                creative.getUnitCode(),
                creative.getMaterialCode(),
                creative.getMaterialType(),
                creative.getMaterialTitle(),
                creative.getImageUrl(),
                creative.getLandingUrl(),
                creative.getSchemaJson(),
                creative.getPriority(),
                creative.getWeight(),
                creative.getDisplayOrder(),
                creative.isFallback(),
                creative.getPreviewImage()
        );
    }

    private void validate(DeliverCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("deliver command must not be null");
        }
        if (command.positionCodeList() == null || command.positionCodeList().isEmpty()) {
            throw new IllegalArgumentException("positionCodeList must not be empty");
        }
        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
    }

    private void validate(DeliverEventCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("deliver event command must not be null");
        }
        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (command.positionCode() == null || command.positionCode().isBlank()) {
            throw new IllegalArgumentException("positionCode must not be blank");
        }
    }

    private String resolveScene(String sceneCode, String fallback) {
        if (sceneCode == null || sceneCode.isBlank()) {
            return fallback;
        }
        return sceneCode;
    }

    private Set<String> mergeUserTags(Long userId, Set<String> requestUserTags) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (requestUserTags != null && !requestUserTags.isEmpty()) {
            merged.addAll(requestUserTags);
        }
        if (userId != null && userId > 0) {
            merged.addAll(audienceFacade.resolveUserTagTokens(userId));
        }
        return merged.isEmpty() ? Set.of() : Set.copyOf(merged);
    }

    private Set<String> resolveAudienceSegmentCodes(Long userId) {
        if (userId == null || userId <= 0) {
            return Set.of();
        }
        Set<String> matchedSegmentCodes = audienceFacade.matchPublishedSegmentCodes(userId);
        return matchedSegmentCodes == null || matchedSegmentCodes.isEmpty()
                ? Set.of()
                : Set.copyOf(matchedSegmentCodes);
    }
}
