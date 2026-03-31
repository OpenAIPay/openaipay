package cn.openaipay.domain.trade.service.impl;

import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.service.TradeRefundDomainService;
import cn.openaipay.domain.trade.service.TradeRefundPreparation;
import java.math.RoundingMode;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * 交易退款领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class TradeRefundDomainServiceImpl implements TradeRefundDomainService {

    /**
     * 处理退款信息。
     */
    @Override
    public TradeRefundPreparation prepareRefund(TradeOrder originalTrade,
                                                Money refundAmount,
                                                Money refundedAmount,
                                                Long requestedPayerUserId,
                                                Long requestedPayeeUserId) {
        if (originalTrade == null) {
            throw new IllegalArgumentException("originalTrade must not be null");
        }
        if (originalTrade.getStatus() != TradeStatus.SUCCEEDED) {
            throw new IllegalStateException(
                    "original trade status does not support refund: " + originalTrade.getStatus().name()
            );
        }
        Money normalizedAmount = normalizePositive(refundAmount, "amount");
        CurrencyUnit currencyUnit = originalTrade.getPayableAmount().getCurrencyUnit();
        if (!currencyUnit.equals(normalizedAmount.getCurrencyUnit())) {
            throw new IllegalArgumentException("refund amount currency must equal original trade currency");
        }
        Money normalizedRefundedAmount = refundedAmount == null
                ? Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP)
                : normalizeNonNegative(refundedAmount, "refundedAmount", currencyUnit);
        Money availableAmount = originalTrade.getPayableAmount()
                .minus(normalizedRefundedAmount)
                .rounded(2, RoundingMode.HALF_UP);
        if (normalizedAmount.compareTo(availableAmount) > 0) {
            throw new IllegalArgumentException("refund amount exceeds available amount: " + availableAmount);
        }
        Long payerUserId = requestedPayerUserId == null
                ? originalTrade.getPayeeUserId()
                : requirePositive(requestedPayerUserId, "payerUserId");
        Long payeeUserId = requestedPayeeUserId == null
                ? originalTrade.getPayerUserId()
                : requirePositive(requestedPayeeUserId, "payeeUserId");
        if (payerUserId == null) {
            throw new IllegalArgumentException("refund payerUserId must not be null");
        }
        return new TradeRefundPreparation(
                normalizedAmount,
                payerUserId,
                payeeUserId
        );
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private Money normalizePositive(Money value, String fieldName) {
        if (value == null || value.isLessThanOrEqual(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String fieldName, CurrencyUnit currencyUnit) {
        if (!currencyUnit.equals(value.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must equal original trade currency");
        }
        if (value.isLessThan(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroOf(Money value) {
        return Money.zero(value.getCurrencyUnit());
    }
}
