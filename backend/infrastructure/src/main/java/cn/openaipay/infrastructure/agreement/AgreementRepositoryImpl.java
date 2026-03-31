package cn.openaipay.infrastructure.agreement;

import cn.openaipay.domain.agreement.model.AgreementBizType;
import cn.openaipay.domain.agreement.model.AgreementSignItem;
import cn.openaipay.domain.agreement.model.AgreementSignRecord;
import cn.openaipay.domain.agreement.model.AgreementSignStatus;
import cn.openaipay.domain.agreement.model.AgreementTemplate;
import cn.openaipay.domain.agreement.repository.AgreementRepository;
import cn.openaipay.infrastructure.agreement.dataobject.AgreementSignItemDO;
import cn.openaipay.infrastructure.agreement.dataobject.AgreementSignRecordDO;
import cn.openaipay.infrastructure.agreement.dataobject.AgreementTemplateDO;
import cn.openaipay.infrastructure.agreement.mapper.AgreementSignItemMapper;
import cn.openaipay.infrastructure.agreement.mapper.AgreementSignRecordMapper;
import cn.openaipay.infrastructure.agreement.mapper.AgreementTemplateMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 协议仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class AgreementRepositoryImpl implements AgreementRepository {

    /** 协议模板信息 */
    private final AgreementTemplateMapper agreementTemplateMapper;
    /** 协议签约记录信息 */
    private final AgreementSignRecordMapper agreementSignRecordMapper;
    /** 协议签约条目信息 */
    private final AgreementSignItemMapper agreementSignItemMapper;

    public AgreementRepositoryImpl(AgreementTemplateMapper agreementTemplateMapper,
                                   AgreementSignRecordMapper agreementSignRecordMapper,
                                   AgreementSignItemMapper agreementSignItemMapper) {
        this.agreementTemplateMapper = agreementTemplateMapper;
        this.agreementSignRecordMapper = agreementSignRecordMapper;
        this.agreementSignItemMapper = agreementSignItemMapper;
    }

    /**
     * 查询模板信息列表。
     */
    @Override
    public List<AgreementTemplate> listActiveTemplates(AgreementBizType bizType) {
        return agreementTemplateMapper.findByBizTypeAndStatus(bizType.persistentCode(), "ACTIVE")
                .stream()
                .map(this::toDomainTemplate)
                .toList();
    }

    /**
     * 按单号查找记录。
     */
    @Override
    public Optional<AgreementSignRecord> findSignRecordBySignNo(String signNo) {
        return agreementSignRecordMapper.findBySignNo(signNo).map(this::toDomainSignRecord);
    }

    /**
     * 按KEY查找记录。
     */
    @Override
    public Optional<AgreementSignRecord> findSignRecordByIdempotencyKey(Long userId, AgreementBizType bizType, String idempotencyKey) {
        return agreementSignRecordMapper.findByIdempotencyKey(userId, bizType.persistentCode(), idempotencyKey)
                .map(this::toDomainSignRecord);
    }

    /**
     * 保存记录。
     */
    @Override
    @Transactional
    public AgreementSignRecord saveSignRecord(AgreementSignRecord signRecord) {
        AgreementSignRecordDO entity = agreementSignRecordMapper.findBySignNo(signRecord.getSignNo())
                .orElse(new AgreementSignRecordDO());
        fillSignRecordDO(entity, signRecord);
        agreementSignRecordMapper.save(entity);
        return toDomainSignRecord(entity);
    }

    /**
     * 处理条目信息。
     */
    @Override
    @Transactional
    public void replaceSignItems(String signNo, List<AgreementSignItem> signItems) {
        agreementSignItemMapper.deleteBySignNo(signNo);
        if (signItems == null || signItems.isEmpty()) {
            return;
        }
        for (AgreementSignItem signItem : signItems) {
            AgreementSignItemDO itemDO = new AgreementSignItemDO();
            fillSignItemDO(itemDO, signItem);
            agreementSignItemMapper.insert(itemDO);
        }
    }

    /**
     * 查询条目信息列表。
     */
    @Override
    public List<AgreementSignItem> listSignItems(String signNo) {
        return agreementSignItemMapper.findBySignNo(signNo).stream().map(this::toDomainSignItem).toList();
    }

    private AgreementTemplate toDomainTemplate(AgreementTemplateDO entity) {
        return new AgreementTemplate(
                entity.getTemplateCode(),
                entity.getTemplateVersion(),
                AgreementBizType.fromCode(entity.getBizType()),
                entity.getTitle(),
                entity.getContentUrl(),
                entity.getContentHash(),
                Boolean.TRUE.equals(entity.getRequiredFlag()),
                "ACTIVE".equalsIgnoreCase(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AgreementSignRecord toDomainSignRecord(AgreementSignRecordDO entity) {
        return new AgreementSignRecord(
                entity.getSignNo(),
                entity.getUserId(),
                AgreementBizType.fromCode(entity.getBizType()),
                entity.getFundCode(),
                entity.getCurrencyCode(),
                entity.getIdempotencyKey(),
                AgreementSignStatus.valueOf(entity.getSignStatus()),
                entity.getSignedAt(),
                entity.getOpenedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AgreementSignItem toDomainSignItem(AgreementSignItemDO entity) {
        return new AgreementSignItem(
                entity.getSignNo(),
                entity.getTemplateCode(),
                entity.getTemplateVersion(),
                entity.getTitle(),
                entity.getContentUrl(),
                entity.getContentHash(),
                Boolean.TRUE.equals(entity.getAccepted()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillSignRecordDO(AgreementSignRecordDO entity, AgreementSignRecord signRecord) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(signRecord.getCreatedAt() == null ? now : signRecord.getCreatedAt());
        }
        entity.setSignNo(signRecord.getSignNo());
        entity.setUserId(signRecord.getUserId());
        entity.setBizType(signRecord.getBizType().name());
        entity.setFundCode(signRecord.getFundCode());
        entity.setCurrencyCode(signRecord.getCurrencyCode());
        entity.setIdempotencyKey(signRecord.getIdempotencyKey());
        entity.setSignStatus(signRecord.getSignStatus().name());
        entity.setSignedAt(signRecord.getSignedAt());
        entity.setOpenedAt(signRecord.getOpenedAt());
        entity.setUpdatedAt(signRecord.getUpdatedAt() == null ? now : signRecord.getUpdatedAt());
    }

    private void fillSignItemDO(AgreementSignItemDO entity, AgreementSignItem signItem) {
        LocalDateTime now = LocalDateTime.now();
        entity.setSignNo(signItem.getSignNo());
        entity.setTemplateCode(signItem.getTemplateCode());
        entity.setTemplateVersion(signItem.getTemplateVersion());
        entity.setTitle(signItem.getTitle());
        entity.setContentUrl(signItem.getContentUrl());
        entity.setContentHash(signItem.getContentHash());
        entity.setAccepted(signItem.isAccepted());
        entity.setCreatedAt(signItem.getCreatedAt() == null ? now : signItem.getCreatedAt());
        entity.setUpdatedAt(signItem.getUpdatedAt() == null ? now : signItem.getUpdatedAt());
    }
}
