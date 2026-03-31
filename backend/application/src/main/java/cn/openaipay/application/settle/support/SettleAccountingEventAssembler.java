package cn.openaipay.application.settle.support;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.settle.async.SettleAccountingEventRequestedPayload;
import cn.openaipay.domain.accounting.model.AccountingSubjectCodes;
import org.joda.money.Money;
import org.springframework.boot.json.JsonWriter;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责把结算成功事实组装成标准化会计事件。
 *
 * 第一版只对“待结算释放到钱包”的事实过账；手续费收入已在 pay 成功事件中确认，
 * 平台手续费钱包的产品内余额变动暂不在 settle 事件中重复确认收入。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class SettleAccountingEventAssembler {

    /** 事件ID */
    private static final String EVENT_ID_PREFIX = "AE-SETTLE-SUCCESS-";
    /** 键信息 */
    private static final String IDEMPOTENCY_KEY_PREFIX = "SETTLE:";

    /**
     * 处理业务数据。
     */
    public AcceptAccountingEventCommand assemble(SettleAccountingEventRequestedPayload payload) {
        if (payload == null) {
            return null;
        }
        Long creditedUserId = resolveCreditedUserId(payload);
        Money settledAmount = resolveSettledAmount(payload);
        if (creditedUserId == null || settledAmount == null || settledAmount.getAmount().signum() <= 0) {
            return null;
        }

        String tradeOrderNo = normalizeRequired(payload.tradeOrderNo(), "tradeOrderNo");
        String settleBizNo = normalizeRequired(payload.settleBizNo(), "settleBizNo");
        String eventType = resolveEventType(payload.tradeType());
        String businessDomainCode = resolveBusinessDomainCode(payload.tradeType());
        String ownerType = isPayeeCredited(payload) ? "PAYEE" : "USER";

        List<AcceptAccountingEventCommand.AccountingLegCommand> legs = List.of(
                new AcceptAccountingEventCommand.AccountingLegCommand(
                        1,
                        "SETTLEMENT",
                        "PENDING_SETTLEMENT",
                        settleBizNo,
                        ownerType,
                        creditedUserId,
                        settledAmount,
                        "OUT",
                        "SETTLEMENT_RELEASE",
                        AccountingSubjectCodes.LIABILITY_PENDING_SETTLEMENT,
                        settleBizNo,
                        null
                ),
                new AcceptAccountingEventCommand.AccountingLegCommand(
                        2,
                        "WALLET",
                        "USER_WALLET",
                        String.valueOf(creditedUserId),
                        "USER",
                        creditedUserId,
                        settledAmount,
                        "IN",
                        "SETTLEMENT_CREDIT",
                        AccountingSubjectCodes.LIABILITY_USER_WALLET_AVAILABLE,
                        settleBizNo,
                        null
                )
        );

        return new AcceptAccountingEventCommand(
                EVENT_ID_PREFIX + settleBizNo,
                eventType,
                1,
                "BOOK_DEFAULT",
                "SETTLE",
                "TRADE",
                tradeOrderNo,
                normalizeOptional(payload.settleBizNo()),
                normalizeOptional(payload.requestNo()),
                tradeOrderNo,
                payload.payOrderNo(),
                null,
                businessDomainCode,
                payload.payerUserId(),
                payload.payeeUserId(),
                settledAmount.getCurrencyUnit().getCode(),
                null,
                IDEMPOTENCY_KEY_PREFIX + settleBizNo + ":SUCCESS",
                null,
                null,
                buildPayload(payload, creditedUserId, settledAmount, businessDomainCode),
                legs
        );
    }

    private Long resolveCreditedUserId(SettleAccountingEventRequestedPayload payload) {
        String tradeType = normalizeUpper(payload.tradeType());
        if ("PAY".equals(tradeType)) {
            if (!payload.shouldCreditPayee()) {
                return null;
            }
            return normalizePositive(payload.payeeUserId());
        }
        if ("TRANSFER".equals(tradeType)) {
            return normalizePositive(payload.payeeUserId());
        }
        if ("DEPOSIT".equals(tradeType)) {
            Long payeeUserId = normalizePositive(payload.payeeUserId());
            return payeeUserId == null ? normalizePositive(payload.payerUserId()) : payeeUserId;
        }
        return null;
    }

    private Money resolveSettledAmount(SettleAccountingEventRequestedPayload payload) {
        Money amount = firstPositive(payload.settleAmount(), payload.payableAmount(), payload.originalAmount());
        return amount == null ? null : amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money firstPositive(Money... values) {
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

    private boolean isPayeeCredited(SettleAccountingEventRequestedPayload payload) {
        Long payeeUserId = normalizePositive(payload.payeeUserId());
        Long creditedUserId = resolveCreditedUserId(payload);
        return payeeUserId != null && payeeUserId.equals(creditedUserId);
    }

    private String resolveEventType(String tradeType) {
        String normalizedTradeType = normalizeUpper(tradeType);
        return switch (normalizedTradeType) {
            case "DEPOSIT" -> "DEPOSIT_SETTLED";
            case "TRANSFER" -> "TRANSFER_SETTLED";
            case "PAY" -> "PAY_SETTLED";
            default -> "SETTLE_SUCCEEDED";
        };
    }

    private String resolveBusinessDomainCode(String tradeType) {
        String normalizedTradeType = normalizeUpper(tradeType);
        return switch (normalizedTradeType) {
            case "DEPOSIT", "TRANSFER" -> "WALLET";
            case "PAY" -> "TRADE";
            default -> null;
        };
    }

    private String buildPayload(SettleAccountingEventRequestedPayload payload,
                                Long creditedUserId,
                                Money settledAmount,
                                String businessDomainCode) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("tradeType", payload.tradeType());
        content.put("tradeOrderNo", payload.tradeOrderNo());
        content.put("payOrderNo", payload.payOrderNo());
        content.put("settleBizNo", payload.settleBizNo());
        content.put("payerUserId", payload.payerUserId());
        content.put("payeeUserId", payload.payeeUserId());
        content.put("creditedUserId", creditedUserId);
        content.put("settledAmount", amountText(settledAmount));
        content.put("payableAmount", amountText(payload.payableAmount()));
        content.put("originalAmount", amountText(payload.originalAmount()));
        content.put("shouldCreditPayee", payload.shouldCreditPayee());
        content.put("businessDomainCode", businessDomainCode);
        return JsonWriter.standard().writeToString(content);
    }

    private Long normalizePositive(Long value) {
        return value == null || value <= 0 ? null : value;
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String raw) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? "" : normalized.toUpperCase();
    }

    private String amountText(Money amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
