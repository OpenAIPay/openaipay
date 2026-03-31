package cn.openaipay.domain.pay.service.impl;

import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayParticipantType;
import cn.openaipay.domain.pay.model.PaySplitPlan;
import cn.openaipay.domain.pay.service.PayOrderDomainService;
import cn.openaipay.domain.pay.service.PayOrderSubmission;
import java.math.RoundingMode;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * 支付订单领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class PayOrderDomainServiceImpl implements PayOrderDomainService {

    private static final List<PayParticipantType> DEFAULT_PREPARATION_SEQUENCE = List.of(
            PayParticipantType.COUPON,
            PayParticipantType.WALLET_ACCOUNT,
            PayParticipantType.FUND_ACCOUNT,
            PayParticipantType.CREDIT_ACCOUNT,
            PayParticipantType.INBOUND
    );

    private static final List<PayParticipantType> WITHDRAW_PREPARATION_SEQUENCE = List.of(
            PayParticipantType.COUPON,
            PayParticipantType.WALLET_ACCOUNT,
            PayParticipantType.OUTBOUND,
            PayParticipantType.FUND_ACCOUNT,
            PayParticipantType.CREDIT_ACCOUNT
    );

    /**
     * 创建订单信息。
     */
    @Override
    public PayOrder createSubmittedOrder(PayOrderSubmission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("submission must not be null");
        }
        Money originalAmount = normalizePositive(submission.originalAmount(), "originalAmount");
        CurrencyUnit currencyUnit = originalAmount.getCurrencyUnit();
        PaySplitPlan splitPlan = PaySplitPlan.of(
                currencyUnit,
                submission.walletDebitAmount(),
                submission.fundDebitAmount(),
                submission.creditDebitAmount(),
                submission.inboundDebitAmount()
        );
        Money discountAmount = normalizeNonNegative(submission.discountAmount(), "discountAmount", currencyUnit);
        Money payableAmount = originalAmount.minus(discountAmount).rounded(2, RoundingMode.HALF_UP);
        if (payableAmount.isLessThan(Money.zero(currencyUnit))) {
            throw new IllegalArgumentException("payableAmount must be greater than or equal to 0");
        }
        if (splitPlan.totalDebitAmount().compareTo(payableAmount) != 0) {
            throw new IllegalArgumentException(
                    "walletDebitAmount + fundDebitAmount + creditDebitAmount + inboundDebitAmount must equal payableAmount"
            );
        }
        String payOrderNo = submission.payOrderNo();
        String tradeOrderNo = submission.tradeOrderNo();
        String bizOrderNo = submission.bizOrderNo();
        String sourceBizType = submission.sourceBizType();
        String sourceBizNo = submission.sourceBizNo();
        int attemptNo = submission.attemptNo();
        String sourceBizSnapshot = submission.sourceBizSnapshot();
        String businessSceneCode = submission.businessSceneCode();
        return PayOrder.createSubmitted(
                payOrderNo,
                tradeOrderNo,
                bizOrderNo,
                sourceBizType,
                sourceBizNo,
                attemptNo,
                sourceBizSnapshot,
                businessSceneCode,
                submission.payerUserId(),
                submission.payeeUserId(),
                originalAmount,
                discountAmount,
                payableAmount,
                splitPlan,
                submission.couponNo(),
                submission.settlementPlanSnapshot(),
                submission.globalTxId(),
                submission.occurredAt()
        );
    }

    /**
     * 解析业务数据。
     */
    @Override
    public List<PayParticipantType> resolvePreparationSequence(PayOrder payOrder) {
        if (payOrder == null) {
            throw new IllegalArgumentException("payOrder must not be null");
        }
        return isWithdrawScene(payOrder.getBusinessSceneCode())
                ? WITHDRAW_PREPARATION_SEQUENCE
                : DEFAULT_PREPARATION_SEQUENCE;
    }

    private boolean isWithdrawScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase();
        return upper.contains("WITHDRAW") || upper.endsWith("_WITHDRAW");
    }

    private Money normalizePositive(Money value, String fieldName) {
        if (value == null || value.isLessThanOrEqual(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String fieldName, CurrencyUnit currencyUnit) {
        if (value == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!currencyUnit.equals(value.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must equal " + currencyUnit.getCode());
        }
        if (value.isLessThan(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroOf(Money value) {
        return Money.zero(value.getCurrencyUnit());
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
