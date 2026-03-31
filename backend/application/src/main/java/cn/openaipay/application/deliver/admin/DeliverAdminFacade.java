package cn.openaipay.application.deliver.admin;

import java.util.List;

/**
 * 投放管理后台门面。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverAdminFacade {

    /**
     * 查询控制台信息。
     */
    DeliverAdminDTOs.Console queryConsole();

    /**
     * 查询广告位信息列表。
     */
    List<DeliverAdminDTOs.PositionItem> listPositions();

    /**
     * 查询单元信息列表。
     */
    List<DeliverAdminDTOs.UnitItem> listUnits();

    /**
     * 查询素材信息列表。
     */
    List<DeliverAdminDTOs.MaterialItem> listMaterials();

    /**
     * 查询创意信息列表。
     */
    List<DeliverAdminDTOs.CreativeItem> listCreatives();

    /**
     * 查询关联关系信息列表。
     */
    List<DeliverAdminDTOs.RelationItem> listRelations();

    /**
     * 查询频控规则列表。
     */
    List<DeliverAdminDTOs.FatigueRuleItem> listFatigueRules();

    /**
     * 查询定向规则列表。
     */
    List<DeliverAdminDTOs.TargetingRuleItem> listTargetingRules();

    /**
     * 保存广告位信息。
     */
    DeliverAdminDTOs.PositionItem savePosition(DeliverAdminCommands.UpsertPositionCommand command);

    /**
     * 保存单元信息。
     */
    DeliverAdminDTOs.UnitItem saveUnit(DeliverAdminCommands.UpsertUnitCommand command);

    /**
     * 保存素材信息。
     */
    DeliverAdminDTOs.MaterialItem saveMaterial(DeliverAdminCommands.UpsertMaterialCommand command);

    /**
     * 保存创意信息。
     */
    DeliverAdminDTOs.CreativeItem saveCreative(DeliverAdminCommands.UpsertCreativeCommand command);

    /**
     * 删除创意信息。
     */
    void deleteCreative(Long creativeId);

    /**
     * 保存关联关系信息。
     */
    DeliverAdminDTOs.RelationItem saveRelation(DeliverAdminCommands.UpsertRelationCommand command);

    /**
     * 保存频控规则。
     */
    DeliverAdminDTOs.FatigueRuleItem saveFatigueRule(DeliverAdminCommands.UpsertFatigueRuleCommand command);

    /**
     * 保存定向规则。
     */
    DeliverAdminDTOs.TargetingRuleItem saveTargetingRule(DeliverAdminCommands.UpsertTargetingRuleCommand command);
}
