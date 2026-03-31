package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.PositionUnitCreativeRelation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 展位-单元-创意关系仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface PositionUnitCreativeRelationRepository {

    /**
     * 查询广告位创意列表。
     */
    List<DeliverCreative> queryPositionCreativeList(Long positionId, boolean fallback, LocalDateTime now);

    /**
     * 按广告位ID查找记录。
     */
    List<PositionUnitCreativeRelation> findByPositionId(Long positionId);

    /**
     * 按ID查找关联关系信息。
     */
    Optional<PositionUnitCreativeRelation> findRelationById(Long id);

    /**
     * 查找ALL关联关系信息。
     */
    List<PositionUnitCreativeRelation> findAllRelations();

    /**
     * 保存关联关系信息。
     */
    PositionUnitCreativeRelation saveRelation(PositionUnitCreativeRelation relation);

    /**
     * 按创意ID删除关联关系信息。
     */
    void deleteByCreativeId(Long creativeId);
}
