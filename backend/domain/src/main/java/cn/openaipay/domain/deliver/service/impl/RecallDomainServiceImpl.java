package cn.openaipay.domain.deliver.service.impl;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.repository.PositionUnitCreativeRelationRepository;
import cn.openaipay.domain.deliver.service.RecallDomainService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 召回领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public class RecallDomainServiceImpl implements RecallDomainService {

    /** 位置单元信息 */
    private final PositionUnitCreativeRelationRepository positionUnitCreativeRelationRepository;

    public RecallDomainServiceImpl(PositionUnitCreativeRelationRepository positionUnitCreativeRelationRepository) {
        this.positionUnitCreativeRelationRepository = positionUnitCreativeRelationRepository;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public List<DeliverCreative> recall(Long positionId, boolean fallback, LocalDateTime now) {
        return positionUnitCreativeRelationRepository.queryPositionCreativeList(positionId, fallback, now);
    }
}
