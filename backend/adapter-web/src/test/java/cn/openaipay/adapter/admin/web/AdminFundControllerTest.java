package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.config.JacksonMoneyConfig;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import cn.openaipay.application.adminfund.facade.AdminFundManageFacade;
import cn.openaipay.application.cashier.facade.CashierFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 资金中心控制器测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminFundControllerTest {

    /** 资金中心门面 */
    @Mock
    private AdminFundManageFacade adminFundManageFacade;
    /** 收银台门面 */
    @Mock
    private CashierFacade cashierFacade;

    /** 控制器 */
    private AdminFundController controller;
    /** 映射器 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminFundController(adminFundManageFacade, cashierFacade);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JacksonMoneyConfig().moneyModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void overviewShouldReturnStableResponseFields() throws Exception {
        when(adminFundManageFacade.overview()).thenReturn(new AdminFundOverviewDTO(12, 6, 3, 2, 8));

        ApiResponse<AdminFundController.OverviewResponse> response = controller.overview();

        verify(adminFundManageFacade).overview();
        assertEquals(true, response.success());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/success").asBoolean()).isTrue();
        assertThat(json.at("/data/walletAccountCount").asInt()).isEqualTo(12);
        assertThat(json.at("/data/fundAccountCount").asInt()).isEqualTo(6);
        assertThat(json.at("/data/creditAccountCount").asInt()).isEqualTo(3);
        assertThat(json.at("/data/loanAccountCount").asInt()).isEqualTo(2);
        assertThat(json.at("/data/bankCardCount").asInt()).isEqualTo(8);
    }

    @Test
    void listWalletAccountsShouldKeepExistingJsonFieldNames() throws Exception {
        when(adminFundManageFacade.listWalletAccounts(880100068483692100L, "ACTIVE", 1, 5)).thenReturn(List.of(
                new AdminWalletAccountRowDTO(
                        880100068483692100L,
                        "顾郡",
                        "gujun001",
                        "CNY",
                        money("77.00"),
                        money("8.00"),
                        "ACTIVE",
                        LocalDateTime.of(2026, 3, 21, 10, 0),
                        LocalDateTime.of(2026, 3, 21, 10, 5)
                )
        ));

        ApiResponse<List<AdminFundController.WalletAccountRow>> response =
                controller.listWalletAccounts(880100068483692100L, "ACTIVE", 1, 5, null);

        verify(adminFundManageFacade).listWalletAccounts(880100068483692100L, "ACTIVE", 1, 5);
        assertEquals(1, response.data().size());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        JsonNode row = json.at("/data/0");
        assertThat(row.at("/userId").asLong()).isEqualTo(880100068483692100L);
        assertThat(row.at("/userDisplayName").asText()).isEqualTo("顾郡");
        assertThat(row.at("/aipayUid").asText()).isEqualTo("gujun001");
        assertThat(row.at("/currencyCode").asText()).isEqualTo("CNY");
        assertThat(row.at("/availableBalance/amount").asText()).isEqualTo("77.00");
        assertThat(row.at("/availableBalance/currencyCode").asText()).isEqualTo("CNY");
        assertThat(row.at("/reservedBalance/amount").asText()).isEqualTo("8.00");
        assertThat(row.at("/accountStatus").asText()).isEqualTo("ACTIVE");
        assertThat(row.get("createdAt")).isNotNull();
        assertThat(row.get("updatedAt")).isNotNull();
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
