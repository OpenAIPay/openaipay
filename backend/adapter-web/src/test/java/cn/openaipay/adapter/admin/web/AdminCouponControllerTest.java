package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.config.JacksonMoneyConfig;
import cn.openaipay.adapter.admin.web.request.CreateCouponTemplateRequest;
import cn.openaipay.application.coupon.command.CreateCouponTemplateCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.dto.CouponTemplateDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台红包控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminCouponControllerTest {

    /** 红包门面。 */
    @Mock
    private CouponFacade couponFacade;

    /** 控制器。 */
    private AdminCouponController controller;
    /** 映射器。 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminCouponController(couponFacade);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JacksonMoneyConfig().moneyModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void createTemplateShouldDefaultBlankOperatorAndMapRequestIntoCommand() {
        when(couponFacade.createTemplate(any())).thenReturn(new CouponTemplateDTO(
                1001L,
                "TPL_TOPUP_50",
                "话费立减红包",
                "MOBILE_TOPUP",
                "FIXED",
                money("5.00"),
                null,
                null,
                money("50.00"),
                money("500.00"),
                100,
                0,
                1,
                LocalDateTime.of(2026, 3, 21, 0, 0),
                LocalDateTime.of(2026, 3, 31, 23, 59),
                LocalDateTime.of(2026, 3, 21, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59),
                "PLATFORM",
                "{\"carrier\":\"CMCC\"}",
                "ACTIVE",
                "admin",
                "admin",
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 21, 10, 0)
        ));

        ApiResponse<CouponTemplateDTO> response = controller.createTemplate(new CreateCouponTemplateRequest(
                "TPL_TOPUP_50",
                "话费立减红包",
                "MOBILE_TOPUP",
                "FIXED",
                money("5.00"),
                null,
                null,
                money("50.00"),
                money("500.00"),
                100,
                1,
                "2026-03-21 00:00:00",
                "2026-03-31 23:59:59",
                "2026-03-21 00:00:00",
                "2026-04-30 23:59:59",
                "PLATFORM",
                "{\"carrier\":\"CMCC\"}",
                "ACTIVE",
                "   "
        ));

        ArgumentCaptor<CreateCouponTemplateCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateCouponTemplateCommand.class);
        verify(couponFacade).createTemplate(commandCaptor.capture());

        CreateCouponTemplateCommand command = commandCaptor.getValue();
        assertEquals("TPL_TOPUP_50", command.templateCode());
        assertEquals("话费立减红包", command.templateName());
        assertEquals("MOBILE_TOPUP", command.sceneType());
        assertEquals("FIXED", command.valueType());
        assertEquals("PLATFORM", command.fundingSource());
        assertEquals("ACTIVE", command.initialStatus());
        assertEquals("admin", command.operator());
        assertEquals(true, response.success());
        assertEquals(1001L, response.data().templateId());
    }

    @Test
    void listIssuesShouldKeepExistingJsonFieldNames() throws Exception {
        when(couponFacade.listIssues(1001L, 880109000000000001L, "ISSUED", 20)).thenReturn(List.of(
                new CouponIssueDTO(
                        2001L,
                        "CPN202603210001",
                        1001L,
                        880109000000000001L,
                        money("5.00"),
                        "ISSUED",
                        "MOBILE_TOPUP",
                        "BIZ202603210001",
                        "ORD202603210001",
                        "BIZORDER202603210001",
                        "TRD202603210001",
                        "PAY202603210001",
                        LocalDateTime.of(2026, 3, 21, 10, 0),
                        LocalDateTime.of(2026, 4, 30, 23, 59),
                        null,
                        LocalDateTime.of(2026, 3, 21, 10, 0),
                        LocalDateTime.of(2026, 3, 21, 10, 0)
                )
        ));

        ApiResponse<List<CouponIssueDTO>> response =
                controller.listIssues(1001L, 880109000000000001L, "ISSUED", 20);

        verify(couponFacade).listIssues(1001L, 880109000000000001L, "ISSUED", 20);
        JsonNode row = objectMapper.readTree(objectMapper.writeValueAsString(response)).at("/data/0");
        assertThat(row.at("/couponNo").asText()).isEqualTo("CPN202603210001");
        assertThat(row.at("/couponAmount/amount").asText()).isEqualTo("5.00");
        assertThat(row.at("/status").asText()).isEqualTo("ISSUED");
        assertThat(row.at("/claimChannel").asText()).isEqualTo("MOBILE_TOPUP");
        assertThat(row.at("/businessNo").asText()).isEqualTo("BIZ202603210001");
        assertThat(row.at("/bizOrderNo").asText()).isEqualTo("BIZORDER202603210001");
        assertThat(row.at("/tradeOrderNo").asText()).isEqualTo("TRD202603210001");
        assertThat(row.at("/payOrderNo").asText()).isEqualTo("PAY202603210001");
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
