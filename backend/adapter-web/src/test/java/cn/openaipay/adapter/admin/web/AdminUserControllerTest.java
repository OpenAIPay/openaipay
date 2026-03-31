package cn.openaipay.adapter.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminuser.dto.AdminFundAccountSnapshotDTO;
import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;
import cn.openaipay.application.adminuser.facade.AdminUserManageFacade;
import cn.openaipay.domain.shared.number.FundAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 后台用户控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    /** 用户门面。 */
    @Mock
    private AdminUserManageFacade adminUserManageFacade;

    /** 控制器。 */
    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(adminUserManageFacade);
    }

    @Test
    void summaryShouldMapCenterMetricsIntoResponse() {
        when(adminUserManageFacade.summary()).thenReturn(new AdminUserCenterSummaryDTO(120, 100, 10, 10, 88, 64));

        ApiResponse<cn.openaipay.adapter.admin.web.response.AdminUserCenterSummaryResponse> response = controller.summary();

        verify(adminUserManageFacade).summary();
        assertEquals(true, response.success());
        assertEquals(120, response.data().totalUserCount());
        assertEquals(100, response.data().activeUserCount());
        assertEquals(10, response.data().frozenUserCount());
        assertEquals(88, response.data().walletActiveUserCount());
        assertEquals(64, response.data().creditNormalUserCount());
    }

    @Test
    void getUserDetailShouldMapNestedAccountAndFundSnapshot() {
        when(adminUserManageFacade.getUserDetail(880109000000000001L)).thenReturn(new AdminUserDetailDTO(
                account("ACTIVE"),
                "/api/media/gujun.png",
                "86",
                "顾**",
                "440101199001010011",
                "F",
                "广州",
                LocalDate.of(1990, 1, 1),
                true,
                "SMS",
                "LOW",
                true,
                false,
                true,
                true,
                false,
                true,
                money("1.20"),
                money("0.30"),
                fa("123.4567"),
                fa("8.9000"),
                List.of(new AdminFundAccountSnapshotDTO(
                        "AICASH",
                        "CNY",
                        fa("123.4567"),
                        fa("0.0000"),
                        fa("0.0000"),
                        fa("0.0000"),
                        fa("8.9000"),
                        fa("0.1200"),
                        fa("1.0000"),
                        "ACTIVE",
                        LocalDateTime.of(2026, 3, 21, 10, 0)
                )),
                LocalDateTime.of(2026, 3, 21, 10, 5)
        ));

        ApiResponse<cn.openaipay.adapter.admin.web.response.AdminUserDetailResponse> response =
                controller.getUserDetail(880109000000000001L);

        verify(adminUserManageFacade).getUserDetail(880109000000000001L);
        assertEquals("顾郡", response.data().account().realName());
        assertEquals("顾小郡", response.data().account().nickname());
        assertEquals("ACTIVE", response.data().account().accountStatus());
        assertEquals("/api/media/gujun.png", response.data().avatarUrl());
        assertEquals("顾**", response.data().maskedRealName());
        assertEquals("SMS", response.data().twoFactorMode());
        assertEquals("123.4567", response.data().fundTotalAvailableShare().toPlainString());
        assertEquals("8.9000", response.data().fundTotalAccumulatedIncome().toPlainString());
        assertEquals(1, response.data().fundAccounts().size());
        assertEquals("AICASH", response.data().fundAccounts().get(0).fundCode());
        assertEquals("1.0000", response.data().fundAccounts().get(0).latestNav().toPlainString());
    }

    @Test
    void changeUserStatusShouldPassPathUserIdAndStatus() {
        when(adminUserManageFacade.changeUserStatus(880109000000000001L, "FROZEN")).thenReturn(account("FROZEN"));

        ApiResponse<cn.openaipay.adapter.admin.web.response.AdminUserSummaryResponse> response =
                controller.changeUserStatus(
                        880109000000000001L,
                        new cn.openaipay.adapter.admin.web.request.ChangeUserAccountStatusRequest("FROZEN", null)
                );

        verify(adminUserManageFacade).changeUserStatus(880109000000000001L, "FROZEN");
        assertEquals(true, response.success());
        assertEquals("FROZEN", response.data().accountStatus());
    }

    private AdminUserSummaryDTO account(String accountStatus) {
        return new AdminUserSummaryDTO(
                "880109000000000001",
                "顾郡",
                "880109000000000001",
                "13920000002",
                "顾小郡",
                "138****0088",
                accountStatus,
                "L2",
                true,
                true,
                "ACTIVE",
                money("88.66"),
                money("3.21"),
                "NORMAL",
                money("20000.00"),
                money("1200.00"),
                money("1188.80"),
                1,
                LocalDateTime.of(2026, 3, 21, 9, 0),
                LocalDateTime.of(2026, 3, 21, 10, 0)
        );
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }

    private FundAmount fa(String amount) {
        return FundAmount.of(new BigDecimal(amount));
    }
}
