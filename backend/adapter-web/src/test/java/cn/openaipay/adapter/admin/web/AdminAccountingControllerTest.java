package cn.openaipay.adapter.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.web.request.ReverseAccountingVoucherRequest;
import cn.openaipay.adapter.admin.web.request.SaveAccountingSubjectRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.accounting.command.ReverseAccountingVoucherCommand;
import cn.openaipay.application.accounting.command.SaveAccountingSubjectCommand;
import cn.openaipay.application.accounting.dto.AccountingEntryDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectDTO;
import cn.openaipay.application.accounting.dto.AccountingVoucherDTO;
import cn.openaipay.application.accounting.facade.AccountingFacade;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 后台会计控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminAccountingControllerTest {

    /** 会计门面。 */
    @Mock
    private AccountingFacade accountingFacade;
    /** 请求上下文。 */
    @Mock
    private AdminRequestContext adminRequestContext;

    /** 控制器。 */
    private AdminAccountingController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAccountingController(accountingFacade, adminRequestContext);
    }

    @Test
    void reverseVoucherShouldFallbackOperatorFromAdminId() {
        when(adminRequestContext.currentAdminUsername()).thenReturn("   ");
        when(adminRequestContext.requiredAdminId()).thenReturn(9001L);
        when(accountingFacade.reverseVoucher(any())).thenReturn(voucher("VCH_REV_001"));

        ApiResponse<AccountingVoucherDTO> response = controller.reverseVoucher(
                "VCH202603210001",
                new ReverseAccountingVoucherRequest("重复记账冲正")
        );

        ArgumentCaptor<ReverseAccountingVoucherCommand> commandCaptor =
                ArgumentCaptor.forClass(ReverseAccountingVoucherCommand.class);
        verify(accountingFacade).reverseVoucher(commandCaptor.capture());

        ReverseAccountingVoucherCommand command = commandCaptor.getValue();
        assertEquals("VCH202603210001", command.voucherNo());
        assertEquals("重复记账冲正", command.reverseReason());
        assertEquals("admin#9001", command.operator());
        assertEquals(true, response.success());
        assertEquals("VCH_REV_001", response.data().voucherNo());
    }

    @Test
    void listVoucherEntriesShouldUseFixedScopeAndLimit() {
        when(accountingFacade.listEntries("VCH202603210001", null, null, null, null, null, null, null, null, 500))
                .thenReturn(List.of(new AccountingEntryDTO(
                        "VCH202603210001",
                        1,
                        "1001",
                        "用户钱包可用余额",
                        "DEBIT",
                        money("88.88"),
                        "USER",
                        880109000000000001L,
                        "WALLET",
                        "USER_WALLET",
                        "WALLET-880109000000000001",
                        "PAYER_DEBIT",
                        "BIZ202603210001",
                        "TRD202603210001",
                        "PAY202603210001",
                        "TRADE",
                        "BIZ202603210001",
                        "REF202603210001",
                        "钱包扣款",
                        LocalDateTime.of(2026, 3, 21, 10, 0),
                        LocalDateTime.of(2026, 3, 21, 10, 0)
                )));

        ApiResponse<List<AccountingEntryDTO>> response = controller.listVoucherEntries("VCH202603210001");

        verify(accountingFacade).listEntries("VCH202603210001", null, null, null, null, null, null, null, null, 500);
        assertEquals(1, response.data().size());
        assertEquals("1001", response.data().get(0).subjectCode());
        assertEquals("88.88", response.data().get(0).amount().getAmount().toPlainString());
    }

    @Test
    void saveSubjectShouldMapPathAndRequestBodyIntoCommand() {
        when(accountingFacade.saveSubject(any())).thenReturn(new AccountingSubjectDTO(
                "1001",
                "用户钱包可用余额",
                "LIABILITY",
                "CREDIT",
                "1000",
                2,
                true,
                "用户钱包主科目",
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 21, 10, 5)
        ));

        ApiResponse<AccountingSubjectDTO> response = controller.saveSubject(
                "1001",
                new SaveAccountingSubjectRequest("用户钱包可用余额", "LIABILITY", "CREDIT", "1000", 2, true, "用户钱包主科目")
        );

        ArgumentCaptor<SaveAccountingSubjectCommand> commandCaptor =
                ArgumentCaptor.forClass(SaveAccountingSubjectCommand.class);
        verify(accountingFacade).saveSubject(commandCaptor.capture());

        SaveAccountingSubjectCommand command = commandCaptor.getValue();
        assertEquals("1001", command.subjectCode());
        assertEquals("用户钱包可用余额", command.subjectName());
        assertEquals("LIABILITY", command.subjectType());
        assertEquals("CREDIT", command.balanceDirection());
        assertEquals("1000", command.parentSubjectCode());
        assertEquals(2, command.levelNo());
        assertEquals(true, command.enabled());
        assertEquals("用户钱包主科目", command.remark());
        assertEquals(true, response.success());
        assertEquals("1001", response.data().subjectCode());
    }

    private AccountingVoucherDTO voucher(String voucherNo) {
        return new AccountingVoucherDTO(
                voucherNo,
                "BOOK_DEFAULT",
                "REVERSAL",
                "AE202603210001",
                "TRADE",
                "BIZ202603210001",
                "BIZ202603210001",
                "TRD202603210001",
                "PAY202603210001",
                "CHAT_TRANSFER",
                "WALLET",
                "POSTED",
                "CNY",
                money("88.88"),
                money("88.88"),
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDate.of(2026, 3, 21),
                null,
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 21, 10, 5),
                List.of()
        );
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
