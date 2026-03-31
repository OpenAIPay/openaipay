package cn.openaipay.domain.trade.service.impl;

import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeType;
import cn.openaipay.domain.trade.service.TradeSplitDomainService;
import java.math.RoundingMode;
import java.util.Locale;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * 交易参与方拆分领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class TradeSplitDomainServiceImpl implements TradeSplitDomainService {

    /**
     * 解析计划信息。
     */
    @Override
    public TradeSplitPlan resolveSplitPlan(TradeType tradeType,
                                           String paymentMethod,
                                           Money payableAmount,
                                           Money walletDebitAmount,
                                           Money fundDebitAmount,
                                           Money creditDebitAmount,
                                           Money inboundDebitAmount) {
        if (tradeType == null) {
            throw new IllegalArgumentException("tradeType must not be null");
        }
        Money normalizedPayableAmount = normalizePositive(payableAmount, "payableAmount");
        CurrencyUnit currencyUnit = normalizedPayableAmount.getCurrencyUnit();
        boolean allEmpty = walletDebitAmount == null
                && fundDebitAmount == null
                && creditDebitAmount == null
                && inboundDebitAmount == null;
        if (allEmpty) {
            return defaultSplitByTradeTypeAndMethod(tradeType, paymentMethod, normalizedPayableAmount);
        }
        TradeSplitPlan splitPlan = TradeSplitPlan.of(
                currencyUnit,
                normalizeNonNegative(walletDebitAmount, "walletDebitAmount", currencyUnit),
                normalizeNonNegative(fundDebitAmount, "fundDebitAmount", currencyUnit),
                normalizeNonNegative(creditDebitAmount, "creditDebitAmount", currencyUnit),
                normalizeNonNegative(inboundDebitAmount, "inboundDebitAmount", currencyUnit)
        );
        if (splitPlan.totalDebitAmount().compareTo(normalizedPayableAmount) != 0) {
            throw new IllegalArgumentException(
                    "walletDebitAmount + fundDebitAmount + creditDebitAmount + inboundDebitAmount must equal payableAmount"
            );
        }
        assertTradeTypeSpecificRules(tradeType, splitPlan, normalizedPayableAmount);
        return splitPlan;
    }

    private TradeSplitPlan defaultSplitByTradeTypeAndMethod(TradeType tradeType, String paymentMethod, Money payableAmount) {
        Money zero = Money.zero(payableAmount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
        String normalizedMethod = normalizeOptional(paymentMethod);
        boolean bankCardMethod = isBankCardPaymentMethod(normalizedMethod);
        boolean fundMethod = isFundPaymentMethod(normalizedMethod);
        boolean creditMethod = isCreditPaymentMethod(normalizedMethod);
        return switch (tradeType) {
            case DEPOSIT -> TradeSplitPlan.of(payableAmount.getCurrencyUnit(), zero, zero, zero, payableAmount);
            case TRANSFER, PAY -> bankCardMethod
                    ? TradeSplitPlan.of(payableAmount.getCurrencyUnit(), zero, zero, zero, payableAmount)
                    : fundMethod
                    ? TradeSplitPlan.of(payableAmount.getCurrencyUnit(), zero, payableAmount, zero, zero)
                    : creditMethod
                    ? TradeSplitPlan.of(payableAmount.getCurrencyUnit(), zero, zero, payableAmount, zero)
                    : TradeSplitPlan.of(payableAmount.getCurrencyUnit(), payableAmount, zero, zero, zero);
            case WITHDRAW, REFUND -> TradeSplitPlan.of(payableAmount.getCurrencyUnit(), payableAmount, zero, zero, zero);
        };
    }

    private void assertTradeTypeSpecificRules(TradeType tradeType, TradeSplitPlan splitPlan, Money payableAmount) {
        Money zero = Money.zero(payableAmount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
        if (tradeType == TradeType.DEPOSIT) {
            if (splitPlan.getWalletDebitAmount().compareTo(zero) > 0
                    || splitPlan.getFundDebitAmount().compareTo(zero) > 0
                    || splitPlan.getCreditDebitAmount().compareTo(zero) > 0
                    || splitPlan.getInboundDebitAmount().compareTo(payableAmount) != 0) {
                throw new IllegalArgumentException("deposit trade must route full payableAmount to inboundDebitAmount");
            }
        }
        if (tradeType == TradeType.WITHDRAW) {
            if (splitPlan.getWalletDebitAmount().compareTo(payableAmount) != 0
                    || splitPlan.getFundDebitAmount().compareTo(zero) > 0
                    || splitPlan.getCreditDebitAmount().compareTo(zero) > 0
                    || splitPlan.getInboundDebitAmount().compareTo(zero) > 0) {
                throw new IllegalArgumentException("withdraw trade must route full payableAmount to walletDebitAmount");
            }
        }
    }

    private boolean isFundPaymentMethod(String normalizedMethod) {
        if (normalizedMethod == null) {
            return false;
        }
        String upperMethod = normalizedMethod.toUpperCase(Locale.ROOT);
        if (upperMethod.equals("FUND")
                || upperMethod.startsWith("FUND:")
                || upperMethod.equals("FUND_ACCOUNT")
                || upperMethod.startsWith("FUND_ACCOUNT:")) {
            return true;
        }
        int delimiterIndex = upperMethod.indexOf(':');
        String productCode = delimiterIndex >= 0 ? upperMethod.substring(0, delimiterIndex) : upperMethod;
        return FundProductCodes.isPrimaryFundCode(productCode);
    }

    private boolean isBankCardPaymentMethod(String normalizedMethod) {
        if (normalizedMethod == null) {
            return false;
        }
        String upperMethod = normalizedMethod.toUpperCase(Locale.ROOT);
        return upperMethod.equals("BANK_CARD") || upperMethod.startsWith("BANK_CARD:");
    }

    private boolean isCreditPaymentMethod(String normalizedMethod) {
        if (normalizedMethod == null) {
            return false;
        }
        String upperMethod = normalizedMethod.toUpperCase(Locale.ROOT);
        return upperMethod.equals(CreditProductCodes.AICREDIT)
                || upperMethod.startsWith(CreditProductCodes.AICREDIT + ":")
                || upperMethod.equals(CreditProductCodes.AILOAN)
                || upperMethod.startsWith(CreditProductCodes.AILOAN + ":")
                || upperMethod.equals("LOAN")
                || upperMethod.startsWith("LOAN:")
                || upperMethod.equals("LOAN_ACCOUNT")
                || upperMethod.startsWith("LOAN_ACCOUNT:")
                || upperMethod.equals("CREDIT_ACCOUNT")
                || upperMethod.startsWith("CREDIT_ACCOUNT:")
                || upperMethod.equals("CREDIT")
                || upperMethod.startsWith("CREDIT:");
    }

    private Money normalizePositive(Money value, String fieldName) {
        if (value == null || value.compareTo(zeroOf(value)) <= 0) {
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
        if (value.compareTo(zeroOf(value)) < 0) {
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
