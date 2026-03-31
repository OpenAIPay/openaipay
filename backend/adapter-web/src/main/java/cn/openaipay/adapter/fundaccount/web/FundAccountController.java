package cn.openaipay.adapter.fundaccount.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.fundaccount.web.request.CreateFundAccountRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundFastRedeemRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundIncomeSettleRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundPayFreezeCompensateRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundPayFreezeConfirmRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundPayFreezeRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundRedeemCancelRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundRedeemConfirmRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundRedeemRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSubscribeCancelRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSubscribeConfirmRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSubscribeRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSwitchCancelRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSwitchConfirmRequest;
import cn.openaipay.adapter.fundaccount.web.request.FundSwitchRequest;
import cn.openaipay.adapter.fundaccount.web.request.PublishFundIncomeCalendarRequest;
import cn.openaipay.adapter.fundaccount.web.request.SettleFundIncomeCalendarRequest;
import cn.openaipay.adapter.fundaccount.web.request.UpsertFundProductRequest;
import cn.openaipay.application.fundaccount.command.CreateFundAccountCommand;
import cn.openaipay.application.fundaccount.command.FundFastRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundIncomeSettleCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCancelCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchConfirmCommand;
import cn.openaipay.application.fundaccount.command.PublishFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.SettleFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.UpsertFundProductCommand;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.dto.FundTradeBackfillResultDTO;
import cn.openaipay.application.fundaccount.dto.FundIncomeCalendarDTO;
import cn.openaipay.application.fundaccount.dto.FundProductDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDetailDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.fundaccount.facade.FundAccountFacade;
import cn.openaipay.application.fundaccount.service.FundIncomeDistributionService;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import jakarta.validation.Valid;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Arrays;
import java.util.List;

/**
 * 基金账户控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/fund-accounts")
public class FundAccountController {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(FundAccountController.class);
    /** 资金编码 */
    private static final String DEFAULT_FUND_CODE = FundProductCodes.DEFAULT_FUND_CODE;

    /** FundAccountFacade组件 */
    private final FundAccountFacade fundAccountFacade;
    /** 收益兜底发放应用服务 */
    private final FundIncomeDistributionService fundIncomeDistributionService;

    public FundAccountController(FundAccountFacade fundAccountFacade,
                                 FundIncomeDistributionService fundIncomeDistributionService) {
        this.fundAccountFacade = fundAccountFacade;
        this.fundIncomeDistributionService = fundIncomeDistributionService;
    }

    /**
     * 创建基金信息。
     */
    @PostMapping
    public ApiResponse<Long> createFundAccount(@Valid @RequestBody CreateFundAccountRequest request) {
        Long userId = fundAccountFacade.createFundAccount(
                new CreateFundAccountCommand(request.userId(), request.fundCode(), request.currencyCode())
        );
        return ApiResponse.success(userId);
    }

    /**
     * 获取基金信息。
     */
    @GetMapping("/{userId}")
    public ApiResponse<FundAccountDTO> getFundAccount(@PathVariable("userId") Long userId,
                                                      @RequestParam(value = "fundCode", required = false) String fundCode) {
        String normalizedFundCode = normalizeFundCode(fundCode);
        if (DEFAULT_FUND_CODE.equals(normalizedFundCode)) {
            trySettleIncomeFallback(userId, normalizedFundCode);
        }
        if (fundCode == null || fundCode.isBlank()) {
            return ApiResponse.success(fundAccountFacade.getFundAccount(userId));
        }
        return ApiResponse.success(fundAccountFacade.getFundAccount(userId, fundCode));
    }

    /**
     * 获取基金明细信息。
     */
    @GetMapping("/trades/{fundTradeNo}")
    public ApiResponse<FundTransactionDetailDTO> getFundTransactionDetail(@PathVariable("fundTradeNo") String fundTradeNo) {
        return ApiResponse.success(fundAccountFacade.getFundTransactionDetail(fundTradeNo));
    }

    /**
     * 处理交易基金订单信息。
     */
    @PostMapping("/trades/backfill")
    public ApiResponse<FundTradeBackfillResultDTO> backfillTradeFundOrders(
            @RequestParam(value = "fundCode", required = false) String fundCode,
            @RequestParam(value = "types", required = false) String types,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<String> transactionTypes = parseTypes(types);
        return ApiResponse.success(fundAccountFacade.backfillTradeFundOrders(fundCode, transactionTypes, limit));
    }

    /**
     * 保存或更新业务数据。
     */
    @PostMapping("/products")
    public ApiResponse<FundProductDTO> upsertProduct(@Valid @RequestBody UpsertFundProductRequest request) {
        FundProductDTO result = fundAccountFacade.upsertFundProduct(
                new UpsertFundProductCommand(
                        request.fundCode(),
                        request.productName(),
                        request.currencyCode(),
                        request.productStatus(),
                        request.singleSubscribeMinAmount(),
                        request.singleSubscribeMaxAmount(),
                        request.dailySubscribeMaxAmount(),
                        request.singleRedeemMinShare(),
                        request.singleRedeemMaxShare(),
                        request.dailyRedeemMaxShare(),
                        request.fastRedeemDailyQuota(),
                        request.fastRedeemPerUserDailyQuota(),
                        request.switchEnabled()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 获取业务数据。
     */
    @GetMapping("/products/{fundCode}")
    public ApiResponse<FundProductDTO> getProduct(@PathVariable("fundCode") String fundCode) {
        return ApiResponse.success(fundAccountFacade.getFundProduct(fundCode));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/subscribe")
    public ApiResponse<FundTransactionDTO> subscribe(@Valid @RequestBody FundSubscribeRequest request) {
        FundTransactionDTO result = fundAccountFacade.subscribe(
                new FundSubscribeCommand(
                        request.orderNo(),
                        request.userId(),
                        request.fundCode(),
                        request.amount(),
                        request.businessNo()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 确认业务数据。
     */
    @PostMapping("/subscribe/confirm")
    public ApiResponse<FundTransactionDTO> confirmSubscribe(@Valid @RequestBody FundSubscribeConfirmRequest request) {
        FundTransactionDTO result = fundAccountFacade.confirmSubscribe(
                new FundSubscribeConfirmCommand(request.orderNo(), request.confirmedShare(), request.nav())
        );
        return ApiResponse.success(result);
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/subscribe/cancel")
    public ApiResponse<FundTransactionDTO> cancelSubscribe(@Valid @RequestBody FundSubscribeCancelRequest request) {
        FundTransactionDTO result = fundAccountFacade.cancelSubscribe(
                new FundSubscribeCancelCommand(request.orderNo())
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/redeem")
    public ApiResponse<FundTransactionDTO> redeem(@Valid @RequestBody FundRedeemRequest request) {
        FundTransactionDTO result = fundAccountFacade.redeem(
                new FundRedeemCommand(
                        request.orderNo(),
                        request.userId(),
                        request.fundCode(),
                        request.share(),
                        request.redeemMode(),
                        request.businessNo(),
                        request.redeemDestination(),
                        request.bankName()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 确认业务数据。
     */
    @PostMapping("/redeem/confirm")
    public ApiResponse<FundTransactionDTO> confirmRedeem(@Valid @RequestBody FundRedeemConfirmRequest request) {
        FundTransactionDTO result = fundAccountFacade.confirmRedeem(
                new FundRedeemConfirmCommand(request.orderNo())
        );
        return ApiResponse.success(result);
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/redeem/cancel")
    public ApiResponse<FundTransactionDTO> cancelRedeem(@Valid @RequestBody FundRedeemCancelRequest request) {
        FundTransactionDTO result = fundAccountFacade.cancelRedeem(
                new FundRedeemCancelCommand(request.orderNo())
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/fast-redeem")
    public ApiResponse<FundTransactionDTO> fastRedeem(@Valid @RequestBody FundFastRedeemRequest request) {
        FundTransactionDTO result = fundAccountFacade.fastRedeem(
                new FundFastRedeemCommand(
                        request.orderNo(),
                        request.userId(),
                        request.fundCode(),
                        request.share(),
                        request.businessNo(),
                        request.redeemDestination(),
                        request.bankName()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理支付信息。
     */
    @PostMapping("/pay-freeze")
    public ApiResponse<FundTransactionDTO> payFreeze(@Valid @RequestBody FundPayFreezeRequest request) {
        Money amount = toMoney(request.amount(), request.currencyCode());
        fundAccountFacade.freezeShareForPay(
                request.fundTradeNo(),
                request.userId(),
                request.fundCode(),
                amount,
                request.businessNo()
        );
        return ApiResponse.success(new FundTransactionDTO(request.fundTradeNo(), "FREEZE", "PENDING", "pay freeze accepted"));
    }

    /**
     * 确认支付信息。
     */
    @PostMapping("/pay-freeze/confirm")
    public ApiResponse<FundTransactionDTO> confirmPayFreeze(@Valid @RequestBody FundPayFreezeConfirmRequest request) {
        fundAccountFacade.confirmFrozenShareForPay(request.userId(), request.fundTradeNo());
        return ApiResponse.success(new FundTransactionDTO(request.fundTradeNo(), "FREEZE", "CONFIRMED", "pay freeze confirmed"));
    }

    /**
     * 处理支付信息。
     */
    @PostMapping("/pay-freeze/compensate")
    public ApiResponse<FundTransactionDTO> compensatePayFreeze(@Valid @RequestBody FundPayFreezeCompensateRequest request) {
        fundAccountFacade.compensateFrozenShareForPay(
                request.userId(),
                request.fundTradeNo(),
                request.fundCode(),
                request.businessNo()
        );
        return ApiResponse.success(new FundTransactionDTO(request.fundTradeNo(), "FREEZE", "COMPENSATED", "pay freeze compensated"));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/switch")
    public ApiResponse<FundTransactionDTO> switchProduct(@Valid @RequestBody FundSwitchRequest request) {
        FundTransactionDTO result = fundAccountFacade.switchProduct(
                new FundSwitchCommand(
                        request.orderNo(),
                        request.userId(),
                        request.sourceFundCode(),
                        request.targetFundCode(),
                        request.sourceShare(),
                        request.businessNo()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 确认业务数据。
     */
    @PostMapping("/switch/confirm")
    public ApiResponse<FundTransactionDTO> confirmSwitch(@Valid @RequestBody FundSwitchConfirmRequest request) {
        FundTransactionDTO result = fundAccountFacade.confirmSwitch(
                new FundSwitchConfirmCommand(request.orderNo(), request.sourceNav(), request.targetNav())
        );
        return ApiResponse.success(result);
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/switch/cancel")
    public ApiResponse<FundTransactionDTO> cancelSwitch(@Valid @RequestBody FundSwitchCancelRequest request) {
        FundTransactionDTO result = fundAccountFacade.cancelSwitch(
                new FundSwitchCancelCommand(request.orderNo())
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理结算收益信息。
     */
    @PostMapping("/income/settle")
    public ApiResponse<FundTransactionDTO> settleIncome(@Valid @RequestBody FundIncomeSettleRequest request) {
        FundTransactionDTO result = fundAccountFacade.settleIncome(
                new FundIncomeSettleCommand(
                        request.orderNo(),
                        request.userId(),
                        request.fundCode(),
                        request.incomeAmount(),
                        request.nav(),
                        request.businessNo()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 发布收益日历信息。
     */
    @PostMapping("/income/calendar/publish")
    public ApiResponse<FundIncomeCalendarDTO> publishIncomeCalendar(@Valid @RequestBody PublishFundIncomeCalendarRequest request) {
        FundIncomeCalendarDTO result = fundAccountFacade.publishIncomeCalendar(
                new PublishFundIncomeCalendarCommand(
                        request.fundCode(),
                        request.bizDate(),
                        request.nav(),
                        request.incomePer10k()
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理结算收益日历信息。
     */
    @PostMapping("/income/calendar/settle")
    public ApiResponse<FundIncomeCalendarDTO> settleIncomeCalendar(@Valid @RequestBody SettleFundIncomeCalendarRequest request) {
        FundIncomeCalendarDTO result = fundAccountFacade.settleIncomeCalendar(
                new SettleFundIncomeCalendarCommand(request.fundCode(), request.bizDate())
        );
        return ApiResponse.success(result);
    }

    private void trySettleIncomeFallback(Long userId, String fundCode) {
        try {
            fundIncomeDistributionService.settleTodayIncomeForUserIfNeeded(userId, fundCode);
        } catch (Exception ex) {
            log.warn("fund income fallback settle failed, userId={}, fundCode={}", userId, fundCode, ex);
        }
    }

    private String normalizeFundCode(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return DEFAULT_FUND_CODE;
        }
        return FundProductCodes.normalizeOrDefault(fundCode);
    }

    private Money toMoney(java.math.BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        String normalizedCurrencyCode = currencyCode == null || currencyCode.isBlank()
                ? "CNY"
                : currencyCode.trim().toUpperCase(Locale.ROOT);
        return Money.of(CurrencyUnit.of(normalizedCurrencyCode), amount);
    }

    private List<String> parseTypes(String types) {
        if (types == null || types.isBlank()) {
            return List.of();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
