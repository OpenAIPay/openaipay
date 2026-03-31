package cn.openaipay.adapter.trade.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.trade.web.request.CreatePayTradeRequest;
import cn.openaipay.adapter.trade.web.request.CreateDepositTradeRequest;
import cn.openaipay.adapter.trade.web.request.CreateRefundTradeRequest;
import cn.openaipay.adapter.trade.web.request.CreateTransferTradeRequest;
import cn.openaipay.adapter.trade.web.request.CreateWithdrawTradeRequest;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.command.CreateDepositTradeCommand;
import cn.openaipay.application.trade.command.CreateRefundTradeCommand;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.command.CreateWithdrawTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.dto.TradeWalletFlowDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 交易控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/trade")
public class TradeController {
    /** 交易应用门面。 */
    private final TradeFacade tradeFacade;

    public TradeController(TradeFacade tradeFacade) {
        this.tradeFacade = tradeFacade;
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/deposit")
    public ApiResponse<TradeOrderDTO> deposit(@Valid @RequestBody CreateDepositTradeRequest request) {
        TradeOrderDTO result = tradeFacade.deposit(new CreateDepositTradeCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.payerUserId(),
                request.payeeUserId(),
                request.paymentMethod(),
                request.amount(),
                request.walletDebitAmount(),
                request.fundDebitAmount(),
                request.creditDebitAmount(),
                request.inboundDebitAmount(),
                request.paymentToolCode(),
                request.metadata()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/withdraw")
    public ApiResponse<TradeOrderDTO> withdraw(@Valid @RequestBody CreateWithdrawTradeRequest request) {
        TradeOrderDTO result = tradeFacade.withdraw(new CreateWithdrawTradeCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.payerUserId(),
                request.payeeUserId(),
                request.paymentMethod(),
                request.amount(),
                request.walletDebitAmount(),
                request.fundDebitAmount(),
                request.creditDebitAmount(),
                request.paymentToolCode(),
                request.metadata()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理支付信息。
     */
    @PostMapping("/pay")
    public ApiResponse<TradeOrderDTO> pay(@Valid @RequestBody CreatePayTradeRequest request) {
        TradeOrderDTO result = tradeFacade.pay(new CreatePayTradeCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.payerUserId(),
                request.payeeUserId(),
                request.paymentMethod(),
                request.amount(),
                request.walletDebitAmount(),
                request.fundDebitAmount(),
                request.creditDebitAmount(),
                request.inboundDebitAmount(),
                request.couponNo(),
                request.paymentToolCode(),
                request.metadata()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理转账信息。
     */
    @PostMapping("/transfer")
    public ApiResponse<TradeOrderDTO> transfer(@Valid @RequestBody CreateTransferTradeRequest request) {
        CurrencyUnit currency = resolveCurrencyUnit(request.currencyCode());
        TradeOrderDTO result = tradeFacade.transfer(new CreateTransferTradeCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.payerUserId(),
                request.payeeUserId(),
                request.paymentMethod(),
                toRequiredMoney(currency, request.amount(), "amount"),
                toOptionalMoney(currency, request.walletDebitAmount()),
                toOptionalMoney(currency, request.fundDebitAmount()),
                toOptionalMoney(currency, request.creditDebitAmount()),
                toOptionalMoney(currency, request.inboundDebitAmount()),
                request.paymentToolCode(),
                request.metadata()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理退款信息。
     */
    @PostMapping("/refund")
    public ApiResponse<TradeOrderDTO> refund(@Valid @RequestBody CreateRefundTradeRequest request) {
        TradeOrderDTO result = tradeFacade.refund(new CreateRefundTradeCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.originalTradeOrderNo(),
                request.payerUserId(),
                request.payeeUserId(),
                request.paymentMethod(),
                request.amount(),
                request.walletDebitAmount(),
                request.fundDebitAmount(),
                request.creditDebitAmount(),
                request.metadata()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 按交易订单单号查询记录。
     */
    @GetMapping("/{tradeOrderNo}")
    public ApiResponse<TradeOrderDTO> queryByTradeOrderNo(@PathVariable("tradeOrderNo") String tradeOrderNo) {
        return ApiResponse.success(tradeFacade.queryByTradeOrderNo(tradeOrderNo));
    }

    /**
     * 按请求单号查询记录。
     */
    @GetMapping("/by-request/{requestNo}")
    public ApiResponse<TradeOrderDTO> queryByRequestNo(@PathVariable("requestNo") String requestNo) {
        return ApiResponse.success(tradeFacade.queryByRequestNo(requestNo));
    }

    /**
     * 查询钱包流程信息。
     */
    @GetMapping("/users/{userId}/recent-wallet-flows")
    public ApiResponse<List<TradeWalletFlowDTO>> queryRecentWalletFlows(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(tradeFacade.queryRecentWalletFlows(userId, limit));
    }

    private CurrencyUnit resolveCurrencyUnit(String currencyCode) {
        String normalized = currencyCode == null || currencyCode.isBlank()
                ? "CNY"
                : currencyCode.trim().toUpperCase();
        try {
            return CurrencyUnit.of(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("currencyCode 非法: " + normalized);
        }
    }

    private Money toRequiredMoney(CurrencyUnit currency, BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return Money.of(currency, amount);
    }

    private Money toOptionalMoney(CurrencyUnit currency, BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return Money.of(currency, amount);
    }
}
