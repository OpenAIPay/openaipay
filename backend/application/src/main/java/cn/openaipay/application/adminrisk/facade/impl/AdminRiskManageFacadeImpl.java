package cn.openaipay.application.adminrisk.facade.impl;

import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.application.adminrisk.facade.AdminRiskManageFacade;
import cn.openaipay.application.adminrisk.service.AdminRiskManageService;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 风控管理门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminRiskManageFacadeImpl implements AdminRiskManageFacade {

    private final AdminRiskManageService adminRiskManageService;

    public AdminRiskManageFacadeImpl(AdminRiskManageService adminRiskManageService) {
        this.adminRiskManageService = adminRiskManageService;
    }

    @Override
    public AdminRiskOverviewDTO overview() {
        return adminRiskManageService.overview();
    }

    @Override
    public List<AdminRiskUserRowDTO> listUsers(String keyword, String kycLevel, String riskLevel, Integer pageNo, Integer pageSize) {
        return adminRiskManageService.listUsers(keyword, kycLevel, riskLevel, pageNo, pageSize);
    }

    @Override
    public List<AdminRiskBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, Integer pageNo, Integer pageSize) {
        return adminRiskManageService.listBlacklists(ownerUserId, blockedUserId, pageNo, pageSize);
    }

    @Override
    public AdminRiskUserRowDTO updateRiskProfile(Long userId,
                                                 String kycLevel,
                                                 String riskLevel,
                                                 String twoFactorMode,
                                                 Boolean deviceLockEnabled,
                                                 Boolean privacyModeEnabled) {
        return adminRiskManageService.updateRiskProfile(
                userId,
                kycLevel,
                riskLevel,
                twoFactorMode,
                deviceLockEnabled,
                privacyModeEnabled
        );
    }

    @Override
    public List<AdminRiskRuleDTO> listRules(String sceneCode, String status, Integer pageNo, Integer pageSize) {
        return adminRiskManageService.listRules(sceneCode, status, pageNo, pageSize);
    }

    @Override
    public AdminRiskRuleDTO saveRule(AdminRiskRuleDTO rule) {
        return adminRiskManageService.saveRule(rule);
    }

    @Override
    public AdminRiskRuleDTO changeRuleStatus(String ruleCode, String status, String operator) {
        return adminRiskManageService.changeRuleStatus(ruleCode, status, operator);
    }

    @Override
    public RiskDecision evaluateTradeRisk(String sceneCode, Long userId, BigDecimal amount, String currencyCode) {
        return adminRiskManageService.evaluateTradeRisk(sceneCode, userId, amount, currencyCode);
    }
}
