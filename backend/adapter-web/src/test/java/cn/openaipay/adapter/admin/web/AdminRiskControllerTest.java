package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.application.adminrisk.facade.AdminRiskManageFacade;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 风控中心控制器测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminRiskControllerTest {

    /** 风控中心门面 */
    @Mock
    private AdminRiskManageFacade adminRiskManageFacade;

    /** 控制器 */
    private AdminRiskController controller;
    /** 映射器 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminRiskController(adminRiskManageFacade);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void overviewShouldKeepResponseFieldNames() throws Exception {
        when(adminRiskManageFacade.overview()).thenReturn(new AdminRiskOverviewDTO(20, 2, 5, 8, 5, 10, 6, 4, 3));

        ApiResponse<AdminRiskController.OverviewResponse> response = controller.overview();

        verify(adminRiskManageFacade).overview();
        assertEquals(true, response.success());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/data/totalUserCount").asInt()).isEqualTo(20);
        assertThat(json.at("/data/l0Count").asInt()).isEqualTo(2);
        assertThat(json.at("/data/l1Count").asInt()).isEqualTo(5);
        assertThat(json.at("/data/l2Count").asInt()).isEqualTo(8);
        assertThat(json.at("/data/l3Count").asInt()).isEqualTo(5);
        assertThat(json.at("/data/lowRiskCount").asInt()).isEqualTo(10);
        assertThat(json.at("/data/mediumRiskCount").asInt()).isEqualTo(6);
        assertThat(json.at("/data/highRiskCount").asInt()).isEqualTo(4);
        assertThat(json.at("/data/blacklistCount").asInt()).isEqualTo(3);
    }

    @Test
    void listUsersShouldKeepResponseFieldNames() throws Exception {
        when(adminRiskManageFacade.listUsers("gu", "L2", "MEDIUM", null, 5)).thenReturn(List.of(
                new AdminRiskUserRowDTO(
                        880100068483692100L,
                        "顾郡",
                        "gujun001",
                        "gujun_login",
                        "138****0001",
                        "ACTIVE",
                        "L2",
                        "MEDIUM",
                        "SMS",
                        true,
                        false,
                        true,
                        true,
                        false,
                        true,
                        LocalDateTime.of(2026, 3, 21, 14, 0)
                )
        ));

        ApiResponse<List<AdminRiskController.RiskUserRow>> response =
                controller.listUsers("gu", "L2", "MEDIUM", null, null, 5);

        verify(adminRiskManageFacade).listUsers("gu", "L2", "MEDIUM", null, 5);
        assertEquals(1, response.data().size());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        JsonNode row = json.at("/data/0");
        assertThat(row.at("/userId").asLong()).isEqualTo(880100068483692100L);
        assertThat(row.at("/displayName").asText()).isEqualTo("顾郡");
        assertThat(row.at("/aipayUid").asText()).isEqualTo("gujun001");
        assertThat(row.at("/loginId").asText()).isEqualTo("gujun_login");
        assertThat(row.at("/mobile").asText()).isEqualTo("138****0001");
        assertThat(row.at("/accountStatus").asText()).isEqualTo("ACTIVE");
        assertThat(row.at("/kycLevel").asText()).isEqualTo("L2");
        assertThat(row.at("/riskLevel").asText()).isEqualTo("MEDIUM");
        assertThat(row.at("/twoFactorMode").asText()).isEqualTo("SMS");
        assertThat(row.at("/deviceLockEnabled").asBoolean()).isTrue();
        assertThat(row.at("/privacyModeEnabled").asBoolean()).isFalse();
        assertThat(row.at("/allowSearchByMobile").asBoolean()).isTrue();
        assertThat(row.at("/allowSearchByAipayUid").asBoolean()).isTrue();
        assertThat(row.at("/hideRealName").asBoolean()).isFalse();
        assertThat(row.at("/personalizedRecommendationEnabled").asBoolean()).isTrue();
        assertThat(row.get("updatedAt")).isNotNull();
    }

    @Test
    void listBlacklistsShouldKeepResponseFieldNames() throws Exception {
        when(adminRiskManageFacade.listBlacklists(880100068483692100L, null, null, 10)).thenReturn(List.of(
                new AdminRiskBlacklistRowDTO(
                        880100068483692100L,
                        "顾郡",
                        "gujun001",
                        880100068483692101L,
                        "祁欣",
                        "qixin001",
                        "骚扰",
                        LocalDateTime.of(2026, 3, 21, 12, 0),
                        LocalDateTime.of(2026, 3, 21, 12, 1)
                )
        ));

        ApiResponse<List<AdminRiskController.RiskBlacklistRow>> response =
                controller.listBlacklists(880100068483692100L, null, null, null, 10);

        verify(adminRiskManageFacade).listBlacklists(880100068483692100L, null, null, 10);
        assertEquals(1, response.data().size());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        JsonNode row = json.at("/data/0");
        assertThat(row.at("/ownerUserId").asLong()).isEqualTo(880100068483692100L);
        assertThat(row.at("/ownerDisplayName").asText()).isEqualTo("顾郡");
        assertThat(row.at("/ownerAipayUid").asText()).isEqualTo("gujun001");
        assertThat(row.at("/blockedUserId").asLong()).isEqualTo(880100068483692101L);
        assertThat(row.at("/blockedDisplayName").asText()).isEqualTo("祁欣");
        assertThat(row.at("/blockedAipayUid").asText()).isEqualTo("qixin001");
        assertThat(row.at("/reason").asText()).isEqualTo("骚扰");
        assertThat(row.get("createdAt")).isNotNull();
        assertThat(row.get("updatedAt")).isNotNull();
    }

    @Test
    void listRulesShouldKeepResponseFieldNames() throws Exception {
        when(adminRiskManageFacade.listRules("TRADE_PAY", "ACTIVE", null, 10)).thenReturn(List.of(
                new AdminRiskRuleDTO(
                        "TRADE_PAY_SINGLE_LIMIT",
                        "TRADE_PAY",
                        "SINGLE_LIMIT",
                        "GLOBAL",
                        null,
                        new java.math.BigDecimal("50000.00"),
                        "CNY",
                        100,
                        "ACTIVE",
                        "支付单笔限额",
                        "ops_admin",
                        LocalDateTime.of(2026, 3, 23, 10, 0),
                        LocalDateTime.of(2026, 3, 23, 11, 0)
                )
        ));

        ApiResponse<List<AdminRiskController.RiskRuleRow>> response = controller.listRules("TRADE_PAY", "ACTIVE", null, null, 10);

        verify(adminRiskManageFacade).listRules("TRADE_PAY", "ACTIVE", null, 10);
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        JsonNode row = json.at("/data/0");
        assertThat(row.at("/ruleCode").asText()).isEqualTo("TRADE_PAY_SINGLE_LIMIT");
        assertThat(row.at("/sceneCode").asText()).isEqualTo("TRADE_PAY");
        assertThat(row.at("/ruleType").asText()).isEqualTo("SINGLE_LIMIT");
        assertThat(row.at("/status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    void saveRuleShouldForwardRequest() {
        AdminRiskController.SaveRiskRuleRequest request = new AdminRiskController.SaveRiskRuleRequest(
                "TRADE_TRANSFER_SINGLE_LIMIT",
                "TRADE_TRANSFER",
                "SINGLE_LIMIT",
                "GLOBAL",
                null,
                new java.math.BigDecimal("50000.00"),
                "CNY",
                100,
                "ACTIVE",
                "转账单笔限额",
                "ops_admin"
        );
        when(adminRiskManageFacade.saveRule(org.mockito.ArgumentMatchers.any())).thenReturn(
                new AdminRiskRuleDTO(
                        "TRADE_TRANSFER_SINGLE_LIMIT",
                        "TRADE_TRANSFER",
                        "SINGLE_LIMIT",
                        "GLOBAL",
                        null,
                        new java.math.BigDecimal("50000.00"),
                        "CNY",
                        100,
                        "ACTIVE",
                        "转账单笔限额",
                        "ops_admin",
                        null,
                        null
                )
        );

        ApiResponse<AdminRiskController.RiskRuleRow> response = controller.saveRule(request);

        assertThat(response.data().ruleCode()).isEqualTo("TRADE_TRANSFER_SINGLE_LIMIT");
        verify(adminRiskManageFacade).saveRule(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void evaluateShouldReturnDecisionFields() throws Exception {
        when(adminRiskManageFacade.evaluateTradeRisk("TRADE_PAY", 880100068483692100L, new java.math.BigDecimal("88.00"), "CNY"))
                .thenReturn(RiskDecision.reject("RISK_DAILY_LIMIT", "daily limit exceeded"));

        ApiResponse<AdminRiskController.RiskDecisionResponse> response =
                controller.evaluate(new AdminRiskController.EvaluateRiskRequest(
                        "TRADE_PAY",
                        880100068483692100L,
                        new java.math.BigDecimal("88.00"),
                        "CNY"
                ));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/data/passed").asBoolean()).isFalse();
        assertThat(json.at("/data/code").asText()).isEqualTo("RISK_DAILY_LIMIT");
        assertThat(json.at("/data/message").asText()).isEqualTo("daily limit exceeded");
    }
}
