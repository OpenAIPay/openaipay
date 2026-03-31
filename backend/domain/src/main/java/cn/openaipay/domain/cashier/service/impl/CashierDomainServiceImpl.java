package cn.openaipay.domain.cashier.service.impl;

import cn.openaipay.domain.cashier.model.CashierPayTool;
import cn.openaipay.domain.cashier.model.CashierPayToolType;
import cn.openaipay.domain.cashier.model.CashierSceneConfiguration;
import cn.openaipay.domain.cashier.service.CashierBankCardProfile;
import cn.openaipay.domain.cashier.service.CashierDomainService;
import cn.openaipay.domain.cashier.service.CashierRecentPaymentHint;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.money.Money;

/**
 * 收银台领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class CashierDomainServiceImpl implements CashierDomainService {

    /**
     * 构建支付信息。
     */
    @Override
    public List<CashierPayTool> buildPayTools(CashierSceneConfiguration sceneConfiguration,
                                              List<CashierBankCardProfile> bankCardProfiles,
                                              List<CashierRecentPaymentHint> recentPaymentHints) {
        CashierSceneConfiguration normalizedSceneConfiguration = sceneConfiguration == null
                ? CashierSceneConfiguration.resolve(null)
                : sceneConfiguration;
        List<CashierPayTool> payTools = (bankCardProfiles == null ? List.<CashierBankCardProfile>of() : bankCardProfiles).stream()
                .filter(bankCardProfile -> normalizedSceneConfiguration.supportsBankCard(bankCardProfile.cardType()))
                .map(this::toBankCardPayTool)
                .toList();
        String recentToolCode = findRecentBankCardToolCode(recentPaymentHints);
        return ensureDefaultSelected(applyRecentBankCardPreference(payTools, recentToolCode));
    }

    /**
     * 规范化业务数据。
     */
    @Override
    public String normalizePaymentMethod(String paymentMethod) {
        String normalized = normalizeOptional(paymentMethod);
        if (normalized == null) {
            return "BANK_CARD";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * 解析计费场景编码。
     */
    @Override
    public String resolvePricingSceneCode(String sceneCode) {
        String normalizedSceneCode = CashierSceneConfiguration.normalizeSceneCode(sceneCode);
        return switch (normalizedSceneCode) {
            case "WITHDRAW" -> "APP_WITHDRAW";
            case "DEPOSIT" -> "APP_DEPOSIT";
            case "TRANSFER" -> "APP_INTERNAL_TRANSFER";
            default -> normalizedSceneCode;
        };
    }

    private List<CashierPayTool> ensureDefaultSelected(List<CashierPayTool> payTools) {
        if (payTools.isEmpty() || payTools.stream().anyMatch(CashierPayTool::isDefaultSelected)) {
            return payTools;
        }
        List<CashierPayTool> adjusted = new ArrayList<>(payTools);
        adjusted.set(0, adjusted.get(0).withDefaultSelected(true));
        return adjusted;
    }

    private List<CashierPayTool> applyRecentBankCardPreference(List<CashierPayTool> payTools, String recentToolCode) {
        if (payTools.isEmpty() || recentToolCode == null) {
            return payTools;
        }

        List<CashierPayTool> reordered = new ArrayList<>(payTools.size());
        CashierPayTool preferred = null;
        for (CashierPayTool payTool : payTools) {
            if (recentToolCode.equals(payTool.getToolCode())) {
                preferred = payTool.withDefaultSelected(true);
                break;
            }
        }
        if (preferred == null) {
            return payTools;
        }

        reordered.add(preferred);
        for (CashierPayTool payTool : payTools) {
            if (recentToolCode.equals(payTool.getToolCode())) {
                continue;
            }
            reordered.add(payTool.withDefaultSelected(false));
        }
        return reordered;
    }

    private String findRecentBankCardToolCode(List<CashierRecentPaymentHint> recentPaymentHints) {
        if (recentPaymentHints == null || recentPaymentHints.isEmpty()) {
            return null;
        }
        for (CashierRecentPaymentHint paymentHint : recentPaymentHints) {
            if (!isBankCardPaymentMethod(paymentHint.paymentMethod())) {
                continue;
            }
            String payToolCode = extractPaymentToolCode(paymentHint.metadata());
            if (payToolCode != null) {
                return payToolCode;
            }
        }
        return null;
    }

    private boolean isBankCardPaymentMethod(String paymentMethod) {
        String normalized = normalizeOptional(paymentMethod);
        if (normalized == null) {
            return false;
        }
        String upperMethod = normalized.toUpperCase(Locale.ROOT);
        return upperMethod.equals("BANK_CARD") || upperMethod.startsWith("BANK_CARD:");
    }

    private String extractPaymentToolCode(String metadata) {
        String normalizedMetadata = normalizeOptional(metadata);
        if (normalizedMetadata == null) {
            return null;
        }
        String[] segments = normalizedMetadata.split(";");
        for (String segment : segments) {
            String[] keyValue = segment.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            if (!"payToolCode".equalsIgnoreCase(keyValue[0].trim())) {
                continue;
            }
            return normalizeOptional(keyValue[1]);
        }
        return null;
    }

    private CashierPayTool toBankCardPayTool(CashierBankCardProfile bankCardProfile) {
        String cardTypeLabel = "CREDIT".equals(normalizePaymentMethod(bankCardProfile.cardType())) ? "信用卡" : "储蓄卡";
        String phoneTail = normalizeOptional(bankCardProfile.phoneTailNo()) == null
                ? defaultPhoneTail(bankCardProfile.reservedMobile())
                : bankCardProfile.phoneTailNo();
        String toolName = bankCardProfile.bankName() + cardTypeLabel + "(尾号" + defaultCardTail(bankCardProfile.cardNo()) + ")";
        String toolDescription = "预留手机尾号" + phoneTail
                + "，单笔限额" + formatMoney(bankCardProfile.singleLimit())
                + "，单日限额" + formatMoney(bankCardProfile.dailyLimit());
        return new CashierPayTool(
                CashierPayToolType.BANK_CARD,
                bankCardProfile.cardNo(),
                toolName,
                toolDescription,
                bankCardProfile.defaultCard(),
                bankCardProfile.singleLimit(),
                bankCardProfile.dailyLimit(),
                bankCardProfile.bankCode(),
                bankCardProfile.cardType(),
                phoneTail
        );
    }

    private String defaultCardTail(String cardNo) {
        if (cardNo == null || cardNo.length() < 4) {
            return "0000";
        }
        return cardNo.substring(cardNo.length() - 4);
    }

    private String defaultPhoneTail(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return "0000";
        }
        String normalized = mobile.trim().replaceAll("\\D", "");
        if (normalized.length() < 4) {
            return "0000";
        }
        return normalized.substring(normalized.length() - 4);
    }

    private String formatMoney(Money money) {
        if (money == null) {
            return "0.00";
        }
        return money.getAmount().toPlainString() + " " + money.getCurrencyUnit().getCode();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
