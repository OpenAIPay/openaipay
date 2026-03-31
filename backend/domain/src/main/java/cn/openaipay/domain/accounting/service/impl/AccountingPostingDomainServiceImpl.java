package cn.openaipay.domain.accounting.service.impl;

import cn.openaipay.domain.accounting.model.AccountingAmountDirection;
import cn.openaipay.domain.accounting.model.AccountingEntry;
import cn.openaipay.domain.accounting.model.AccountingEvent;
import cn.openaipay.domain.accounting.model.AccountingLeg;
import cn.openaipay.domain.accounting.model.AccountingSubjectCodes;
import cn.openaipay.domain.accounting.model.AccountingVoucher;
import cn.openaipay.domain.accounting.model.DebitCreditFlag;
import cn.openaipay.domain.accounting.model.VoucherType;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.accounting.service.AccountingPostingDomainService;
import org.joda.money.Money;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 会计过账领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingPostingDomainServiceImpl implements AccountingPostingDomainService {

    /** 单号 */
    private static final DateTimeFormatter VOUCHER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 构建凭证信息。
     */
    @Override
    public AccountingVoucher buildVoucher(AccountingEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        List<AccountingEntry> entries = new ArrayList<>();
        int lineNo = 1;
        for (AccountingLeg leg : event.getLegs()) {
            entries.add(new AccountingEntry(
                    null,
                    generateVoucherNo(event.getEventId()),
                    lineNo++,
                    resolveSubjectCode(leg),
                    resolveDebitCreditFlag(leg),
                    leg.getAmount(),
                    leg.getOwnerType(),
                    leg.getOwnerId(),
                    leg.getAccountDomain(),
                    leg.getAccountType(),
                    leg.getAccountNo(),
                    leg.getBizRole(),
                    event.getBizOrderNo(),
                    event.getTradeOrderNo(),
                    event.getPayOrderNo(),
                    event.getSourceBizType(),
                    event.getSourceBizNo(),
                    leg.getReferenceNo(),
                    resolveEntryMemo(event, leg),
                    event.getOccurredAt(),
                    event.getOccurredAt()
            ));
        }
        String voucherNo = entries.get(0).getVoucherNo();
        List<AccountingEntry> normalizedEntries = entries.stream()
                .map(entry -> new AccountingEntry(
                        entry.getId(),
                        voucherNo,
                        entry.getLineNo(),
                        entry.getSubjectCode(),
                        entry.getDcFlag(),
                        entry.getAmount(),
                        entry.getOwnerType(),
                        entry.getOwnerId(),
                        entry.getAccountDomain(),
                        entry.getAccountType(),
                        entry.getAccountNo(),
                        entry.getBizRole(),
                        entry.getBizOrderNo(),
                        entry.getTradeOrderNo(),
                        entry.getPayOrderNo(),
                        entry.getSourceBizType(),
                        entry.getSourceBizNo(),
                        entry.getReferenceNo(),
                        entry.getEntryMemo(),
                        entry.getCreatedAt(),
                        entry.getUpdatedAt()
                ))
                .toList();
        return AccountingVoucher.create(
                voucherNo,
                event.getBookId(),
                VoucherType.NORMAL,
                event.getEventId(),
                event.getSourceBizType(),
                event.getSourceBizNo(),
                event.getBizOrderNo(),
                event.getTradeOrderNo(),
                event.getPayOrderNo(),
                event.getBusinessSceneCode(),
                event.getBusinessDomainCode(),
                event.getCurrencyUnit(),
                normalizedEntries,
                event.getOccurredAt()
        );
    }

    /**
     * 构建凭证信息。
     */
    @Override
    public AccountingVoucher buildReverseVoucher(AccountingVoucher originalVoucher,
                                                 String reverseEventId,
                                                 String reverseReason,
                                                 String operator) {
        if (originalVoucher == null) {
            throw new IllegalArgumentException("originalVoucher must not be null");
        }
        String reverseVoucherNo = generateVoucherNo(reverseEventId);
        LocalDateTime now = LocalDateTime.now();
        List<AccountingEntry> reversedEntries = originalVoucher.getEntries().stream()
                .map(entry -> new AccountingEntry(
                        null,
                        reverseVoucherNo,
                        entry.getLineNo(),
                        entry.getSubjectCode(),
                        entry.getDcFlag() == DebitCreditFlag.DEBIT ? DebitCreditFlag.CREDIT : DebitCreditFlag.DEBIT,
                        Money.of(entry.getAmount().getCurrencyUnit(), entry.getAmount().getAmount()),
                        entry.getOwnerType(),
                        entry.getOwnerId(),
                        entry.getAccountDomain(),
                        entry.getAccountType(),
                        entry.getAccountNo(),
                        entry.getBizRole(),
                        entry.getBizOrderNo(),
                        entry.getTradeOrderNo(),
                        entry.getPayOrderNo(),
                        entry.getSourceBizType(),
                        entry.getSourceBizNo(),
                        entry.getReferenceNo(),
                        buildReverseMemo(entry.getEntryMemo(), reverseReason, operator),
                        now,
                        now
                ))
                .toList();
        return AccountingVoucher.create(
                reverseVoucherNo,
                originalVoucher.getBookId(),
                VoucherType.REVERSE,
                reverseEventId,
                originalVoucher.getSourceBizType(),
                originalVoucher.getSourceBizNo(),
                originalVoucher.getBizOrderNo(),
                originalVoucher.getTradeOrderNo(),
                originalVoucher.getPayOrderNo(),
                originalVoucher.getBusinessSceneCode(),
                originalVoucher.getBusinessDomainCode(),
                originalVoucher.getCurrencyUnit(),
                reversedEntries,
                now
        );
    }

    private String generateVoucherNo(String eventId) {
        String suffix = String.valueOf(Math.abs((eventId == null ? "AE" : eventId).hashCode()));
        if (suffix.length() > 6) {
            suffix = suffix.substring(0, 6);
        }
        return "V" + LocalDateTime.now().format(VOUCHER_NO_FORMATTER) + suffix;
    }

    private DebitCreditFlag resolveDebitCreditFlag(AccountingLeg leg) {
        return leg.getDirection() == AccountingAmountDirection.OUT ? DebitCreditFlag.DEBIT : DebitCreditFlag.CREDIT;
    }

    private String resolveSubjectCode(AccountingLeg leg) {
        if (leg.getSubjectHint() != null && leg.getSubjectHint().matches("\\d{4,}")) {
            return leg.getSubjectHint();
        }
        String domain = String.valueOf(leg.getAccountDomain()).toUpperCase();
        if ("FUND".equals(domain) || FundProductCodes.isPrimaryFundCode(domain)) {
            return AccountingSubjectCodes.LIABILITY_AICASH_SHARE;
        }
        return switch (domain) {
            case "WALLET" -> AccountingSubjectCodes.LIABILITY_USER_WALLET_AVAILABLE;
            case "SETTLEMENT" -> AccountingSubjectCodes.LIABILITY_PENDING_SETTLEMENT;
            case CreditProductCodes.AILOAN -> AccountingSubjectCodes.ASSET_AILOAN_RECEIVABLE;
            case "CREDIT", CreditProductCodes.AICREDIT -> AccountingSubjectCodes.ASSET_AICREDIT_RECEIVABLE;
            case "INBOUND" -> AccountingSubjectCodes.ASSET_INBOUND_CHANNEL_CLEARING;
            case "OUTBOUND", "BANK" -> AccountingSubjectCodes.ASSET_RESERVE_BANK_DEPOSIT;
            case "FEE" -> AccountingSubjectCodes.INCOME_PAYMENT_SERVICE_FEE;
            default -> AccountingSubjectCodes.MEMO_UNMAPPED;
        };
    }

    private String resolveEntryMemo(AccountingEvent event, AccountingLeg leg) {
        StringBuilder builder = new StringBuilder();
        builder.append(event.getEventType());
        if (event.getBusinessSceneCode() != null) {
            builder.append("/").append(event.getBusinessSceneCode());
        }
        if (leg.getAccountDomain() != null) {
            builder.append("/").append(leg.getAccountDomain());
        }
        return builder.toString();
    }

    private String buildReverseMemo(String originalMemo, String reverseReason, String operator) {
        StringBuilder builder = new StringBuilder();
        builder.append("REVERSE");
        if (originalMemo != null && !originalMemo.isBlank()) {
            builder.append(":").append(originalMemo.trim());
        }
        if (reverseReason != null && !reverseReason.isBlank()) {
            builder.append("/").append(reverseReason.trim());
        }
        if (operator != null && !operator.isBlank()) {
            builder.append("/").append(operator.trim());
        }
        return builder.toString();
    }
}
