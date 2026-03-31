package cn.openaipay.application.adminrisk.port;

import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

/**
 * 风控管理端口
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface AdminRiskManagePort {

    /**
     * 查询概览。
     */
    AdminRiskOverviewDTO overview();

    /**
     * 查询风控用户列表。
     */
    List<AdminRiskUserRowDTO> listUsers(String keyword, String kycLevel, String riskLevel, int pageNo, int pageSize);

    /**
     * 查询黑名单列表。
     */
    List<AdminRiskBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, int pageNo, int pageSize);

    /**
     * 更新风控档案。
     */
    AdminRiskUserRowDTO updateRiskProfile(Long userId,
                                          String kycLevel,
                                          String riskLevel,
                                          String twoFactorMode,
                                          Boolean deviceLockEnabled,
                                          Boolean privacyModeEnabled);

    /**
     * 查询风控规则列表。
     */
    List<AdminRiskRuleDTO> listRules(String sceneCode, String status, int pageNo, int pageSize);

    /**
     * 按规则编码查询规则。
     */
    Optional<AdminRiskRuleDTO> findRule(String ruleCode);

    /**
     * 保存风控规则（存在则更新）。
     */
    AdminRiskRuleDTO saveRule(AdminRiskRuleDTO rule);

    /**
     * 变更风控规则状态。
     */
    AdminRiskRuleDTO changeRuleStatus(String ruleCode, String status, String operator);

    /**
     * 执行交易风控决策。
     */
    RiskDecision evaluateTradeRisk(String sceneCode, Long userId, BigDecimal amount, String currencyCode);
}
