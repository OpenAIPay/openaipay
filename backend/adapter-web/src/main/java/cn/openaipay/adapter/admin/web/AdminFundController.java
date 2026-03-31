package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminfund.dto.AdminBankCardRowDTO;
import cn.openaipay.application.adminfund.dto.AdminCreditAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import cn.openaipay.application.adminfund.facade.AdminFundManageFacade;
import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import cn.openaipay.application.cashier.facade.CashierFacade;
import cn.openaipay.domain.shared.number.FundAmount;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台资金中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/fund")
public class AdminFundController {

    /** 资金中心门面。 */
    private final AdminFundManageFacade adminFundManageFacade;
    /** 收银台门面。 */
    private final CashierFacade cashierFacade;

    public AdminFundController(AdminFundManageFacade adminFundManageFacade,
                               CashierFacade cashierFacade) {
        this.adminFundManageFacade = adminFundManageFacade;
        this.cashierFacade = cashierFacade;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<OverviewResponse> overview() {
        return ApiResponse.success(toOverviewResponse(adminFundManageFacade.overview()));
    }

    /**
     * 查询钱包信息列表。
     */
    @GetMapping("/wallet-accounts")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<List<WalletAccountRow>> listWalletAccounts(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "accountStatus", required = false) String accountStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminFundManageFacade.listWalletAccounts(userId, accountStatus, pageNo, resolvedPageSize).stream()
                .map(this::toWalletAccountRow)
                .toList());
    }

    /**
     * 查询基金信息列表。
     */
    @GetMapping("/fund-accounts")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<List<FundAccountRow>> listFundAccounts(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "fundCode", required = false) String fundCode,
            @RequestParam(value = "accountStatus", required = false) String accountStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminFundManageFacade.listFundAccounts(userId, fundCode, accountStatus, pageNo, resolvedPageSize).stream()
                .map(this::toFundAccountRow)
                .toList());
    }

    /**
     * 查询信用信息列表。
     */
    @GetMapping("/credit-accounts")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<List<CreditAccountRow>> listCreditAccounts(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "accountStatus", required = false) String accountStatus,
            @RequestParam(value = "payStatus", required = false) String payStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminFundManageFacade.listCreditAccounts(userId, accountStatus, payStatus, pageNo, resolvedPageSize).stream()
                .map(this::toCreditAccountRow)
                .toList());
    }

    /**
     * 查询借款信息列表。
     */
    @GetMapping("/loan-accounts")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<List<CreditAccountRow>> listLoanAccounts(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "accountStatus", required = false) String accountStatus,
            @RequestParam(value = "payStatus", required = false) String payStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminFundManageFacade.listLoanAccounts(userId, accountStatus, payStatus, pageNo, resolvedPageSize).stream()
                .map(this::toCreditAccountRow)
                .toList());
    }

    /**
     * 查询银行信息列表。
     */
    @GetMapping("/bank-cards")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<List<BankCardRow>> listBankCards(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "cardStatus", required = false) String cardStatus,
            @RequestParam(value = "bankCode", required = false) String bankCode,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminFundManageFacade.listBankCards(userId, cardStatus, bankCode, pageNo, resolvedPageSize).stream()
                .map(this::toBankCardRow)
                .toList());
    }

    /**
     * 查询业务数据。
     */
    @GetMapping("/cashier/view")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<CashierViewDTO> queryCashierView(@RequestParam("userId") Long userId,
                                                        @RequestParam(value = "sceneCode", required = false) String sceneCode) {
        return ApiResponse.success(cashierFacade.queryCashier(requirePositive(userId, "userId"), normalizeKeyword(sceneCode)));
    }

    /**
     * 查询计费信息。
     */
    @GetMapping("/cashier/pricing-preview")
    @RequireAdminPermission("fund.center.view")
    public ApiResponse<CashierPricingPreviewDTO> queryCashierPricingPreview(@RequestParam("userId") Long userId,
                                                                            @RequestParam(value = "sceneCode", required = false) String sceneCode,
                                                                            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                                                                            @RequestParam("amount") BigDecimal amount,
                                                                            @RequestParam(value = "currencyCode", required = false, defaultValue = "CNY") String currencyCode) {
        CurrencyUnit currency = CurrencyUnit.of(normalizeCurrency(currencyCode));
        return ApiResponse.success(cashierFacade.previewPricing(
                requirePositive(userId, "userId"),
                normalizeKeyword(sceneCode),
                normalizeKeyword(paymentMethod),
                Money.of(currency, amount == null ? BigDecimal.ZERO : amount)
        ));
    }

    private OverviewResponse toOverviewResponse(AdminFundOverviewDTO dto) {
        return new OverviewResponse(
                dto.walletAccountCount(),
                dto.fundAccountCount(),
                dto.creditAccountCount(),
                dto.loanAccountCount(),
                dto.bankCardCount()
        );
    }

    private WalletAccountRow toWalletAccountRow(AdminWalletAccountRowDTO dto) {
        return new WalletAccountRow(
                dto.userId(),
                dto.userDisplayName(),
                dto.aipayUid(),
                dto.currencyCode(),
                dto.availableBalance(),
                dto.reservedBalance(),
                dto.accountStatus(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    private FundAccountRow toFundAccountRow(AdminFundAccountRowDTO dto) {
        return new FundAccountRow(
                dto.userId(),
                dto.userDisplayName(),
                dto.aipayUid(),
                dto.fundCode(),
                dto.currencyCode(),
                dto.availableShare(),
                dto.frozenShare(),
                dto.pendingSubscribeAmount(),
                dto.pendingRedeemShare(),
                dto.accumulatedIncome(),
                dto.yesterdayIncome(),
                dto.latestNav(),
                dto.accountStatus(),
                dto.updatedAt()
        );
    }

    private CreditAccountRow toCreditAccountRow(AdminCreditAccountRowDTO dto) {
        return new CreditAccountRow(
                dto.productCode(),
                dto.userId(),
                dto.userDisplayName(),
                dto.aipayUid(),
                dto.accountNo(),
                dto.totalLimit(),
                dto.availableLimit(),
                dto.principalBalance(),
                dto.principalUnreachAmount(),
                dto.overduePrincipalBalance(),
                dto.overduePrincipalUnreachAmount(),
                dto.interestBalance(),
                dto.fineBalance(),
                dto.accountStatus(),
                dto.payStatus(),
                dto.repayDayOfMonth(),
                dto.updatedAt()
        );
    }

    private BankCardRow toBankCardRow(AdminBankCardRowDTO dto) {
        return new BankCardRow(
                dto.userId(),
                dto.userDisplayName(),
                dto.aipayUid(),
                dto.cardNo(),
                dto.bankCode(),
                dto.bankName(),
                dto.cardType(),
                dto.cardHolderName(),
                dto.reservedMobile(),
                dto.phoneTailNo(),
                dto.cardStatus(),
                dto.isDefault(),
                dto.singleLimit(),
                dto.dailyLimit(),
                dto.updatedAt()
        );
    }

    private String normalizeKeyword(String raw) {
        String normalized = (raw == null ? "" : raw).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCurrency(String raw) {
        String normalized = normalizeKeyword(raw);
        return normalized == null ? "CNY" : normalized.toUpperCase();
    }

    private Long requirePositive(Long value, String label) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    /** 用户摘要。 */
    private record UserDigest(
            /** 用户ID */
            Long userId,
            /** 展示名称 */
            String displayName,
            /** 爱支付UID */
            String aipayUid
    ) {
    }

    /** 资金中心概览。 */
    public record OverviewResponse(
            /** 钱包账户数量 */
            long walletAccountCount,
            /** 基金账户数量 */
            long fundAccountCount,
            /** 信用账户数量 */
            long creditAccountCount,
            /** 借款账户数量 */
            long loanAccountCount,
            /** 银行卡数量 */
            long bankCardCount
    ) {
    }

    /** 钱包账户行。 */
    public record WalletAccountRow(
            /** 用户ID */
            Long userId,
            /** 用户展示名称 */
            String userDisplayName,
            /** 爱支付UID */
            String aipayUid,
            /** 币种编码 */
            String currencyCode,
            /** 可用余额 */
            Money availableBalance,
            /** 冻结余额 */
            Money reservedBalance,
            /** account状态 */
            String accountStatus,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    /** 基金账户行。 */
    public record FundAccountRow(
            /** 用户ID */
            Long userId,
            /** 用户展示名称 */
            String userDisplayName,
            /** 爱支付UID */
            String aipayUid,
            /** 资金编码 */
            String fundCode,
            /** 币种编码 */
            String currencyCode,
            /** 可用份额 */
            FundAmount availableShare,
            /** 冻结份额 */
            FundAmount frozenShare,
            /** pendingsubscribe金额 */
            FundAmount pendingSubscribeAmount,
            /** 待赎回份额 */
            FundAmount pendingRedeemShare,
            /** accumulated收益信息 */
            FundAmount accumulatedIncome,
            /** yesterday收益信息 */
            FundAmount yesterdayIncome,
            /** latestNAV信息 */
            FundAmount latestNav,
            /** account状态 */
            String accountStatus,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    /** 授信/借贷账户行。 */
    public record CreditAccountRow(
            /** 产品编码 */
            String productCode,
            /** 用户ID */
            Long userId,
            /** 用户展示名称 */
            String userDisplayName,
            /** 爱支付UID */
            String aipayUid,
            /** account单号 */
            String accountNo,
            /** 总限额信息 */
            Money totalLimit,
            /** 可用额度 */
            Money availableLimit,
            /** 本金余额 */
            Money principalBalance,
            /** 本金unreach金额 */
            Money principalUnreachAmount,
            /** 逾期本金余额 */
            Money overduePrincipalBalance,
            /** overdue本金unreach金额 */
            Money overduePrincipalUnreachAmount,
            /** 利息余额 */
            Money interestBalance,
            /** 罚息余额 */
            Money fineBalance,
            /** account状态 */
            String accountStatus,
            /** 支付状态 */
            String payStatus,
            /** repayDAYOFmonth信息 */
            Integer repayDayOfMonth,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    /** 银行卡行。 */
    public record BankCardRow(
            /** 用户ID */
            Long userId,
            /** 用户展示名称 */
            String userDisplayName,
            /** 爱支付UID */
            String aipayUid,
            /** 卡单号 */
            String cardNo,
            /** 银行编码 */
            String bankCode,
            /** 银行名称 */
            String bankName,
            /** 卡类型 */
            String cardType,
            /** 卡holder名称 */
            String cardHolderName,
            /** 冻结手机号 */
            String reservedMobile,
            /** 手机号tail单号 */
            String phoneTailNo,
            /** 卡状态 */
            String cardStatus,
            /** 默认信息标记 */
            Boolean isDefault,
            /** 单笔限额 */
            Money singleLimit,
            /** 单日限额 */
            Money dailyLimit,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }
}
