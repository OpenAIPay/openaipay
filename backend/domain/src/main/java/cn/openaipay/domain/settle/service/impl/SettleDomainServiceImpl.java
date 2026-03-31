package cn.openaipay.domain.settle.service.impl;

import cn.openaipay.domain.settle.service.SettleDomainService;
import cn.openaipay.domain.settle.service.SettlePlan;
import cn.openaipay.domain.settle.service.SettleRequest;
import cn.openaipay.domain.settle.service.SettleWalletAction;
import cn.openaipay.domain.trade.model.TradeType;
import org.joda.money.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 结算入账领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class SettleDomainServiceImpl implements SettleDomainService {

    /** 信用信息 */
    private static final String OPERATION_CREDIT = "CREDIT";
    /** 操作信息 */
    private static final String OPERATION_DEBIT = "DEBIT";
    /** 手续费收款方信息 */
    private static final String FEE_BEARER_PAYEE = "PAYEE";
    /** 手续费付款方信息 */
    private static final String FEE_BEARER_PAYER = "PAYER";
    /** 手续费信息 */
    private static final String FEE_BEARER_PLATFORM = "PLATFORM";

    /**
     * 解析交易计划信息。
     */
    @Override
    public SettlePlan resolveCommittedTradePlan(SettleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        TradeType tradeType = TradeType.from(request.tradeType());
        return switch (tradeType) {
            case WITHDRAW, REFUND -> SettlePlan.noAction();
            case DEPOSIT -> resolveDepositPlan(request);
            case TRANSFER -> resolveTransferPlan(request);
            case PAY -> resolvePayPlan(request);
        };
    }

    private SettlePlan resolveDepositPlan(SettleRequest request) {
        Long creditedUserId = normalizePositive(request.payeeUserId());
        if (creditedUserId == null) {
            creditedUserId = normalizePositive(request.payerUserId());
        }
        if (creditedUserId == null) {
            return SettlePlan.reconPending("deposit trade requires valid credited userId");
        }

        Money amount = firstPositiveMoney(
                request.settleAmount(),
                request.originalAmount(),
                request.payableAmount()
        );
        if (amount == null) {
            return SettlePlan.reconPending("deposit credit amount missing");
        }

        List<SettleWalletAction> primaryActions = new ArrayList<>();
        primaryActions.add(new SettleWalletAction(
                creditedUserId,
                amount,
                OPERATION_CREDIT,
                "DEPOSIT",
                request.settleBizNo(),
                "deposit credit failed"
        ));
        return appendFeeActions(primaryActions, request);
    }

    private SettlePlan resolveTransferPlan(SettleRequest request) {
        Long payeeUserId = normalizePositive(request.payeeUserId());
        if (payeeUserId == null) {
            return SettlePlan.reconPending("transfer trade requires valid payeeUserId");
        }

        Money amount = firstPositiveMoney(request.settleAmount(), request.originalAmount(), request.payableAmount());
        if (amount == null) {
            return SettlePlan.reconPending("transfer credit amount missing");
        }

        Long payerUserId = normalizePositive(request.payerUserId());
        if (payerUserId == null) {
            return SettlePlan.reconPending("transfer trade requires valid payerUserId");
        }

        List<SettleWalletAction> primaryActions = new ArrayList<>();
        primaryActions.add(new SettleWalletAction(
                payeeUserId,
                amount,
                OPERATION_CREDIT,
                "TRANSFER",
                request.settleBizNo(),
                "transfer credit failed"
        ));

        SettlePlan primaryPlan = appendFeeActions(primaryActions, request);
        if (primaryPlan.status() != cn.openaipay.domain.settle.service.SettlePlanStatus.EXECUTE) {
            return primaryPlan;
        }

        return SettlePlan.executeWithCompensation(
                primaryPlan.primaryActions(),
                List.of(new SettleWalletAction(
                        payerUserId,
                        amount,
                        OPERATION_CREDIT,
                        "TRANSFER",
                        request.settleBizNo() + "-COMPENSATE",
                        "transfer compensation failed"
                )),
                true,
                "transfer credit failed, payer compensated"
        );
    }

    private SettlePlan resolvePayPlan(SettleRequest request) {
        if (!request.shouldCreditPayee()) {
            return SettlePlan.noAction();
        }

        Long payeeUserId = normalizePositive(request.payeeUserId());
        if (payeeUserId == null) {
            return SettlePlan.reconPending("pay trade requires valid payeeUserId");
        }

        Money amount = firstPositiveMoney(
                request.settleAmount(),
                request.payableAmount(),
                request.originalAmount()
        );
        if (amount == null) {
            return SettlePlan.reconPending("payee credit amount missing");
        }

        List<SettleWalletAction> primaryActions = new ArrayList<>();
        primaryActions.add(new SettleWalletAction(
                payeeUserId,
                amount,
                OPERATION_CREDIT,
                "PAY",
                request.settleBizNo(),
                "payee credit failed"
        ));
        return appendFeeActions(primaryActions, request);
    }

    private Money firstPositiveMoney(Money... values) {
        if (values == null) {
            return null;
        }
        for (Money value : values) {
            if (value != null && value.getAmount().signum() > 0) {
                return value;
            }
        }
        return null;
    }

    private Long normalizePositive(Long value) {
        return value == null || value <= 0 ? null : value;
    }

    private SettlePlan appendFeeActions(List<SettleWalletAction> primaryActions, SettleRequest request) {
        Money feeAmount = normalizePositiveMoney(request.feeAmount());
        if (feeAmount == null) {
            return SettlePlan.execute(primaryActions);
        }

        Long platformFeeUserId = normalizePositive(request.platformFeeUserId());
        if (platformFeeUserId == null) {
            return SettlePlan.reconPending("platform fee userId missing");
        }

        String feeBearer = normalizeUpper(request.feeBearer());
        if (FEE_BEARER_PAYER.equals(feeBearer) || FEE_BEARER_PAYEE.equals(feeBearer)) {
            primaryActions.add(new SettleWalletAction(
                    platformFeeUserId,
                    feeAmount,
                    OPERATION_CREDIT,
                    request.tradeType(),
                    request.settleBizNo() + "-FEE-INCOME",
                    "platform fee income settle failed"
            ));
            return SettlePlan.execute(primaryActions);
        }
        if (FEE_BEARER_PLATFORM.equals(feeBearer)) {
            primaryActions.add(new SettleWalletAction(
                    platformFeeUserId,
                    feeAmount,
                    OPERATION_DEBIT,
                    request.tradeType(),
                    request.settleBizNo() + "-FEE-EXPENSE",
                    "platform fee expense settle failed"
            ));
            return SettlePlan.execute(primaryActions);
        }
        return SettlePlan.execute(primaryActions);
    }

    private Money normalizePositiveMoney(Money value) {
        if (value == null || value.getAmount().signum() <= 0) {
            return null;
        }
        return value;
    }

    private String normalizeUpper(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
