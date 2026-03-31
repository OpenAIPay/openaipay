package cn.openaipay.application.pay.support;

import cn.openaipay.application.pay.command.SettlementPlanSnapshot;
import cn.openaipay.application.pay.command.SourceBizSnapshot;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.accounting.model.AccountingSubjectCodes;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.pay.client.PayAccountingEventRequest;
import cn.openaipay.domain.pay.model.PayBankCardFundDetail;
import cn.openaipay.domain.pay.model.PayCreditAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundDetailSummary;
import cn.openaipay.domain.pay.model.PayFundDetailTool;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayOrderStatus;
import cn.openaipay.domain.pay.model.PayParticipantBranch;
import cn.openaipay.domain.pay.model.PayParticipantType;
import cn.openaipay.domain.pay.model.PaySplitPlan;
import cn.openaipay.domain.pay.model.PayWalletFundDetail;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import org.joda.money.Money;
import org.springframework.boot.json.JsonWriter;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 负责把支付成功事实组装成标准化会计事件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class PayAccountingEventAssembler {

    /** 事件ID */
    private static final String EVENT_ID_PREFIX = "AE-PAY-COMMIT-";
    /** 键信息 */
    private static final String IDEMPOTENCY_KEY_PREFIX = "PAY_ORDER:";

    /** 支付订单信息 */
    private final PayOrderRepository payOrderRepository;

    public PayAccountingEventAssembler(PayOrderRepository payOrderRepository) {
        this.payOrderRepository = payOrderRepository;
    }

    /**
     * 处理业务数据。
     */
    public PayAccountingEventRequest assembleCommittedPayment(String payOrderNo) {
        String normalizedPayOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        PayOrder payOrder = payOrderRepository.findOrderByPayOrderNo(normalizedPayOrderNo)
                .orElseThrow(() -> new NoSuchElementException("pay order not found: " + normalizedPayOrderNo));
        if (payOrder.getStatus() != PayOrderStatus.COMMITTED) {
            return null;
        }

        SettlementPlanSnapshot settlementPlan = loadSettlementPlan(payOrder);
        SourceBizSnapshot sourceBizSnapshot = loadSourceBizSnapshot(payOrder);
        List<PayParticipantBranch> branches = payOrderRepository.findParticipantBranches(payOrder.getPayOrderNo());
        List<PayFundDetailSummary> fundDetails = payOrderRepository.findFundDetails(payOrder.getPayOrderNo());
        String businessDomainCode = resolveBusinessDomainCode(payOrder, settlementPlan, fundDetails);

        List<PayAccountingEventRequest.PayAccountingLegRequest> legs = new ArrayList<>();
        int nextLegNo = 1;

        nextLegNo = appendWalletDebitLeg(legs, nextLegNo, payOrder, settlementPlan, fundDetails, branches);
        nextLegNo = appendFundDebitLeg(legs, nextLegNo, payOrder, settlementPlan, fundDetails, businessDomainCode, branches);
        nextLegNo = appendCreditDebitLeg(legs, nextLegNo, payOrder, settlementPlan, fundDetails, branches);
        nextLegNo = appendInboundDebitLeg(legs, nextLegNo, payOrder, settlementPlan, fundDetails, branches);

        Money totalDebitAmount = sumAmounts(legs, payOrder.getPayableAmount());
        if (!isPositive(totalDebitAmount)) {
            return null;
        }

        PrimaryCreditSpec primaryCreditSpec = resolvePrimaryCreditSpec(
                payOrder,
                settlementPlan,
                sourceBizSnapshot,
                businessDomainCode,
                totalDebitAmount,
                branches
        );
        if (isPositive(primaryCreditSpec.amount())) {
            legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                    nextLegNo++,
                    primaryCreditSpec.accountDomain(),
                    primaryCreditSpec.accountType(),
                    primaryCreditSpec.accountNo(),
                    primaryCreditSpec.ownerType(),
                    primaryCreditSpec.ownerId(),
                    primaryCreditSpec.amount(),
                    "IN",
                    primaryCreditSpec.bizRole(),
                    primaryCreditSpec.subjectHint(),
                    primaryCreditSpec.referenceNo(),
                    primaryCreditSpec.metadata()
            ));
        }

        Money feeIncomeAmount = totalDebitAmount.minus(primaryCreditSpec.amount()).rounded(2, RoundingMode.HALF_UP);
        if (isPositive(feeIncomeAmount)) {
            legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                    nextLegNo,
                    "FEE",
                    "PLATFORM_FEE_INCOME",
                    "PLATFORM_FEE_INCOME",
                    "PLATFORM",
                    null,
                    feeIncomeAmount,
                    "IN",
                    "PLATFORM_FEE",
                    resolveFeeIncomeSubjectCode(payOrder),
                    payOrder.getPayOrderNo(),
                    null
            ));
        }

        return new PayAccountingEventRequest(
                EVENT_ID_PREFIX + payOrder.getPayOrderNo(),
                resolveEventType(payOrder, businessDomainCode),
                1,
                "BOOK_DEFAULT",
                "PAY",
                payOrder.getSourceBizType(),
                payOrder.getSourceBizNo(),
                payOrder.getBizOrderNo(),
                null,
                resolveTradeOrderNo(payOrder),
                payOrder.getPayOrderNo(),
                payOrder.getBusinessSceneCode(),
                businessDomainCode,
                payOrder.getPayerUserId(),
                payOrder.getPayeeUserId(),
                payOrder.getPayableAmount().getCurrencyUnit().getCode(),
                payOrder.getUpdatedAt(),
                IDEMPOTENCY_KEY_PREFIX + payOrder.getPayOrderNo() + ":COMMITTED",
                payOrder.getGlobalTxId(),
                null,
                buildPayload(payOrder, settlementPlan, sourceBizSnapshot, businessDomainCode, branches, fundDetails, totalDebitAmount,
                        primaryCreditSpec.amount(), feeIncomeAmount),
                legs
        );
    }

    private int appendWalletDebitLeg(List<PayAccountingEventRequest.PayAccountingLegRequest> legs,
                                     int nextLegNo,
                                     PayOrder payOrder,
                                     SettlementPlanSnapshot settlementPlan,
                                     List<PayFundDetailSummary> fundDetails,
                                     List<PayParticipantBranch> branches) {
        PayWalletFundDetail walletDetail = findWalletDetail(fundDetails);
        Money amount = walletDetail == null
                ? normalizeMoney(settlementPlan.walletDebitAmount(), payOrder.getSplitPlan().getWalletDebitAmount())
                : walletDetail.getAmount().rounded(2, RoundingMode.HALF_UP);
        if (!isPositive(amount)) {
            return nextLegNo;
        }
        String accountNo = walletDetail == null
                ? findBranchResource(branches, PayParticipantType.WALLET_ACCOUNT, String.valueOf(payOrder.getPayerUserId()))
                : walletDetail.getAccountNo();
        legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                nextLegNo,
                "WALLET",
                "USER_WALLET",
                accountNo,
                "USER",
                payOrder.getPayerUserId(),
                amount,
                "OUT",
                "PAYER_DEBIT",
                AccountingSubjectCodes.LIABILITY_USER_WALLET_AVAILABLE,
                findBranchId(branches, PayParticipantType.WALLET_ACCOUNT, payOrder.getPayOrderNo()),
                null
        ));
        return nextLegNo + 1;
    }

    private int appendFundDebitLeg(List<PayAccountingEventRequest.PayAccountingLegRequest> legs,
                                   int nextLegNo,
                                   PayOrder payOrder,
                                   SettlementPlanSnapshot settlementPlan,
                                   List<PayFundDetailSummary> fundDetails,
                                   String businessDomainCode,
                                   List<PayParticipantBranch> branches) {
        PayFundAccountFundDetail fundDetail = findFundDetail(fundDetails);
        Money amount = fundDetail == null
                ? normalizeMoney(settlementPlan.fundDebitAmount(), payOrder.getSplitPlan().getFundDebitAmount())
                : fundDetail.getAmount().rounded(2, RoundingMode.HALF_UP);
        if (!isPositive(amount)) {
            return nextLegNo;
        }
        String fundCode = fundDetail == null ? findFundCodeFromBranch(branches) : fundDetail.getFundCode();
        String accountDomain = FundProductCodes.isPrimaryFundCode(defaultValue(fundCode, businessDomainCode)) ? "AICASH" : "FUND";
        String accountType = fundDetail == null ? defaultValue(fundCode, "USER_FUND_SHARE") : fundDetail.getFundProductCode();
        String accountNo = fundDetail == null ? fundCode : defaultValue(fundDetail.getAccountIdentity(), fundCode);
        legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                nextLegNo,
                accountDomain,
                accountType,
                accountNo,
                "USER",
                payOrder.getPayerUserId(),
                amount,
                "OUT",
                "PAYER_DEBIT",
                AccountingSubjectCodes.LIABILITY_AICASH_SHARE,
                findBranchId(branches, PayParticipantType.FUND_ACCOUNT, payOrder.getPayOrderNo()),
                null
        ));
        return nextLegNo + 1;
    }

    private int appendCreditDebitLeg(List<PayAccountingEventRequest.PayAccountingLegRequest> legs,
                                     int nextLegNo,
                                     PayOrder payOrder,
                                     SettlementPlanSnapshot settlementPlan,
                                     List<PayFundDetailSummary> fundDetails,
                                     List<PayParticipantBranch> branches) {
        PayCreditAccountFundDetail creditDetail = findCreditDetail(fundDetails);
        Money amount = creditDetail == null
                ? normalizeMoney(settlementPlan.creditDebitAmount(), payOrder.getSplitPlan().getCreditDebitAmount())
                : creditDetail.getAmount().rounded(2, RoundingMode.HALF_UP);
        if (!isPositive(amount)) {
            return nextLegNo;
        }
        String accountDomain = CreditProductCodes.AICREDIT;
        String accountType = CreditProductCodes.AICREDIT;
        String accountNo = findBranchResource(branches, PayParticipantType.CREDIT_ACCOUNT, null);
        if (creditDetail != null) {
            accountNo = creditDetail.getAccountNo();
            if (creditDetail.getCreditAccountType() == CreditAccountType.LOAN_ACCOUNT) {
                accountDomain = CreditProductCodes.AILOAN;
                accountType = CreditProductCodes.AILOAN;
            }
        }
        legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                nextLegNo,
                accountDomain,
                accountType,
                accountNo,
                "USER",
                payOrder.getPayerUserId(),
                amount,
                "OUT",
                "PAYER_DEBIT",
                resolveCreditSubjectCode(accountDomain),
                findBranchId(branches, PayParticipantType.CREDIT_ACCOUNT, payOrder.getPayOrderNo()),
                null
        ));
        return nextLegNo + 1;
    }

    private int appendInboundDebitLeg(List<PayAccountingEventRequest.PayAccountingLegRequest> legs,
                                     int nextLegNo,
                                     PayOrder payOrder,
                                     SettlementPlanSnapshot settlementPlan,
                                     List<PayFundDetailSummary> fundDetails,
                                     List<PayParticipantBranch> branches) {
        PayBankCardFundDetail bankCardDetail = findInboundDetail(fundDetails);
        Money amount = bankCardDetail == null
                ? normalizeMoney(settlementPlan.inboundDebitAmount(), payOrder.getSplitPlan().getInboundDebitAmount())
                : bankCardDetail.getAmount().rounded(2, RoundingMode.HALF_UP);
        if (!isPositive(amount)) {
            return nextLegNo;
        }
        Map<String, String> branchPayload = parsePayload(findBranchPayload(branches, PayParticipantType.INBOUND));
        String bankCardNo = bankCardDetail == null
                ? defaultValue(branchPayload.get("payerAccountNo"), payOrder.getPayOrderNo())
                : defaultValue(bankCardDetail.getBankCardNo(), branchPayload.get("payerAccountNo"));
        legs.add(new PayAccountingEventRequest.PayAccountingLegRequest(
                nextLegNo,
                "INBOUND",
                "BANK_CARD",
                bankCardNo,
                "PLATFORM",
                null,
                amount,
                "OUT",
                "CHANNEL_INFLOW",
                AccountingSubjectCodes.ASSET_INBOUND_CHANNEL_CLEARING,
                findBranchId(branches, PayParticipantType.INBOUND, payOrder.getPayOrderNo()),
                branchPayload.get("bankCode")
        ));
        return nextLegNo + 1;
    }

    private PrimaryCreditSpec resolvePrimaryCreditSpec(PayOrder payOrder,
                                                       SettlementPlanSnapshot settlementPlan,
                                                       SourceBizSnapshot sourceBizSnapshot,
                                                       String businessDomainCode,
                                                       Money totalDebitAmount,
                                                       List<PayParticipantBranch> branches) {
        boolean withdrawScene = hasOutboundBranch(branches) || isWithdrawScene(payOrder.getBusinessSceneCode());
        if (withdrawScene) {
            Money targetAmount = normalizeMoney(settlementPlan.outboundAmount(), totalDebitAmount);
            targetAmount = capAtTotalDebit(targetAmount, totalDebitAmount);
            Map<String, String> payload = parsePayload(findBranchPayload(branches, PayParticipantType.OUTBOUND));
            return new PrimaryCreditSpec(
                    "OUTBOUND",
                    "BANK_SETTLEMENT",
                    defaultValue(payload.get("payeeAccountNo"), payOrder.getPayOrderNo()),
                    "PLATFORM",
                    null,
                    targetAmount,
                    "WITHDRAW_SETTLEMENT",
                    AccountingSubjectCodes.ASSET_RESERVE_BANK_DEPOSIT,
                    findBranchId(branches, PayParticipantType.OUTBOUND, payOrder.getPayOrderNo()),
                    payload.get("bankCode")
            );
        }

        if (isCreditRepayScene(payOrder.getBusinessSceneCode())) {
            Money targetAmount = capAtTotalDebit(
                    normalizeMoney(sourceBizSnapshot == null ? null : sourceBizSnapshot.settleAmount(), totalDebitAmount),
                    totalDebitAmount
            );
            String normalizedDomainCode = normalizeUpper(businessDomainCode);
            String domain = normalizedDomainCode.equals(CreditProductCodes.AILOAN)
                    ? CreditProductCodes.AILOAN
                    : CreditProductCodes.AICREDIT;
            return new PrimaryCreditSpec(
                    domain,
                    "CREDIT_RECEIVABLE",
                    payOrder.getPayerUserId() + ":" + domain,
                    "USER",
                    payOrder.getPayerUserId(),
                    targetAmount,
                    "CREDIT_REPAY",
                    resolveCreditSubjectCode(domain),
                    payOrder.getPayOrderNo(),
                    null
            );
        }

        if (FundProductCodes.isPrimaryFundCode(businessDomainCode)) {
            Money targetAmount = capAtTotalDebit(
                    normalizeMoney(sourceBizSnapshot == null ? null : sourceBizSnapshot.settleAmount(), totalDebitAmount),
                    totalDebitAmount
            );
            return new PrimaryCreditSpec(
                    "AICASH",
                    "FUND_SHARE_PENDING",
                    payOrder.getPayerUserId() + ":AICASH",
                    "USER",
                    payOrder.getPayerUserId(),
                    targetAmount,
                    "FUND_CREDIT",
                    AccountingSubjectCodes.LIABILITY_AICASH_SHARE,
                    payOrder.getPayOrderNo(),
                    null
            );
        }

        Long creditedOwnerId = payOrder.getPayeeUserId() == null ? payOrder.getPayerUserId() : payOrder.getPayeeUserId();
        String ownerType = payOrder.getPayeeUserId() == null ? "USER" : "PAYEE";
        Money targetAmount = capAtTotalDebit(
                normalizeMoney(sourceBizSnapshot == null ? null : sourceBizSnapshot.settleAmount(), totalDebitAmount),
                totalDebitAmount
        );
        return new PrimaryCreditSpec(
                "SETTLEMENT",
                "PENDING_SETTLEMENT",
                payOrder.getSourceBizNo(),
                ownerType,
                creditedOwnerId,
                targetAmount,
                "SETTLEMENT_PENDING",
                AccountingSubjectCodes.LIABILITY_PENDING_SETTLEMENT,
                payOrder.getSourceBizNo(),
                null
        );
    }

    private String resolveBusinessDomainCode(PayOrder payOrder,
                                             SettlementPlanSnapshot settlementPlan,
                                             List<PayFundDetailSummary> fundDetails) {
        PayFundAccountFundDetail fundDetail = findFundDetail(fundDetails);
        String paymentMethod = settlementPlan == null ? null : settlementPlan.paymentMethod();
        if (fundDetail != null && FundProductCodes.isPrimaryFundCode(fundDetail.getFundProductCode())) {
            return TradeBusinessDomainCode.AICASH.name();
        }
        if (fundDetail != null && FundProductCodes.isPrimaryFundCode(fundDetail.getFundCode())) {
            return TradeBusinessDomainCode.AICASH.name();
        }
        return TradeBusinessDomainCode.detect(payOrder.getBusinessSceneCode(), paymentMethod).name();
    }

    private String resolveEventType(PayOrder payOrder, String businessDomainCode) {
        if (isWithdrawScene(payOrder.getBusinessSceneCode())) {
            return "WITHDRAW_SUCCEEDED";
        }
        if (FundProductCodes.isPrimaryFundCode(businessDomainCode)) {
            return isTransferOutScene(payOrder.getBusinessSceneCode())
                    ? "AICASH_TRANSFER_OUT_SUCCEEDED"
                    : "AICASH_TRANSFER_IN_SUCCEEDED";
        }
        if (isDepositScene(payOrder.getBusinessSceneCode())) {
            return "RECHARGE_SUCCEEDED";
        }
        return "PAY_SUCCEEDED";
    }

    private String resolveTradeOrderNo(PayOrder payOrder) {
        if ("TRADE".equalsIgnoreCase(payOrder.getSourceBizType())) {
            return payOrder.getSourceBizNo();
        }
        return null;
    }

    private String buildPayload(PayOrder payOrder,
                                SettlementPlanSnapshot settlementPlan,
                                SourceBizSnapshot sourceBizSnapshot,
                                String businessDomainCode,
                                List<PayParticipantBranch> branches,
                                List<PayFundDetailSummary> fundDetails,
                                Money totalDebitAmount,
                                Money primaryCreditAmount,
                                Money feeIncomeAmount) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderNo", payOrder.getPayOrderNo());
        payload.put("payStatus", payOrder.getStatus().name());
        payload.put("businessDomainCode", businessDomainCode);
        payload.put("payerUserId", payOrder.getPayerUserId());
        if (payOrder.getPayeeUserId() != null) {
            payload.put("payeeUserId", payOrder.getPayeeUserId());
        }
        payload.put("payableAmount", amountText(payOrder.getPayableAmount()));
        payload.put("actualPaidAmount", amountText(payOrder.getActualPaidAmount()));
        payload.put("totalDebitAmount", amountText(totalDebitAmount));
        payload.put("primaryCreditAmount", amountText(primaryCreditAmount));
        if (isPositive(feeIncomeAmount)) {
            payload.put("feeIncomeAmount", amountText(feeIncomeAmount));
        }
        if (settlementPlan != null) {
            payload.put("paymentMethod", settlementPlan.paymentMethod());
            payload.put("outboundAmount", settlementPlan.outboundAmount() == null ? null : amountText(settlementPlan.outboundAmount()));
            payload.put("fundCode", settlementPlan.fundCode());
        }
        if (sourceBizSnapshot != null) {
            payload.put("sourceTradeType", sourceBizSnapshot.sourceTradeType());
            payload.put("sourceSettleAmount", sourceBizSnapshot.settleAmount() == null ? null : amountText(sourceBizSnapshot.settleAmount()));
            payload.put("requiresPayeeCredit", sourceBizSnapshot.requiresPayeeCredit());
        }
        payload.put("branches", branches.stream().map(branch -> Map.of(
                "participantType", branch.getParticipantType().name(),
                "branchId", branch.getBranchId(),
                "resourceId", branch.getParticipantResourceId(),
                "status", branch.getStatus().name()
        )).toList());
        payload.put("fundDetails", fundDetails.stream().map(this::toFundDetailPayload).toList());
        return JsonWriter.standard().writeToString(payload);
    }

    private Map<String, Object> toFundDetailPayload(PayFundDetailSummary fundDetail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payTool", fundDetail.getPayTool().name());
        payload.put("detailOwner", fundDetail.getDetailOwner().name());
        payload.put("amount", amountText(fundDetail.getAmount()));
        if (fundDetail instanceof PayWalletFundDetail walletFundDetail) {
            payload.put("accountNo", walletFundDetail.getAccountNo());
        } else if (fundDetail instanceof PayFundAccountFundDetail fundAccountFundDetail) {
            payload.put("fundCode", fundAccountFundDetail.getFundCode());
            payload.put("fundProductCode", fundAccountFundDetail.getFundProductCode());
            payload.put("accountIdentity", fundAccountFundDetail.getAccountIdentity());
        } else if (fundDetail instanceof PayCreditAccountFundDetail creditAccountFundDetail) {
            payload.put("accountNo", creditAccountFundDetail.getAccountNo());
            payload.put("creditAccountType", creditAccountFundDetail.getCreditAccountType().name());
        } else if (fundDetail instanceof PayBankCardFundDetail bankCardFundDetail) {
            payload.put("bankCardNo", bankCardFundDetail.getBankCardNo());
            payload.put("channel", bankCardFundDetail.getChannel());
            payload.put("depositOrderNo", bankCardFundDetail.getDepositOrderNo());
        }
        return payload;
    }

    private PayWalletFundDetail findWalletDetail(List<PayFundDetailSummary> fundDetails) {
        return fundDetails.stream()
                .filter(detail -> detail.getPayTool() == PayFundDetailTool.WALLET)
                .filter(PayWalletFundDetail.class::isInstance)
                .map(PayWalletFundDetail.class::cast)
                .findFirst()
                .orElse(null);
    }

    private PayFundAccountFundDetail findFundDetail(List<PayFundDetailSummary> fundDetails) {
        return fundDetails.stream()
                .filter(detail -> detail.getPayTool() == PayFundDetailTool.FUND)
                .filter(PayFundAccountFundDetail.class::isInstance)
                .map(PayFundAccountFundDetail.class::cast)
                .findFirst()
                .orElse(null);
    }

    private PayCreditAccountFundDetail findCreditDetail(List<PayFundDetailSummary> fundDetails) {
        return fundDetails.stream()
                .filter(detail -> detail.getPayTool() == PayFundDetailTool.CREDIT)
                .filter(PayCreditAccountFundDetail.class::isInstance)
                .map(PayCreditAccountFundDetail.class::cast)
                .findFirst()
                .orElse(null);
    }

    private PayBankCardFundDetail findInboundDetail(List<PayFundDetailSummary> fundDetails) {
        return fundDetails.stream()
                .filter(detail -> detail.getPayTool() == PayFundDetailTool.BANK_CARD)
                .filter(PayBankCardFundDetail.class::isInstance)
                .map(PayBankCardFundDetail.class::cast)
                .findFirst()
                .orElse(null);
    }

    private Money sumAmounts(List<PayAccountingEventRequest.PayAccountingLegRequest> legs, Money fallbackAmount) {
        Money total = zeroOf(fallbackAmount);
        for (PayAccountingEventRequest.PayAccountingLegRequest leg : legs) {
            if (leg != null && leg.amount() != null) {
                total = total.plus(leg.amount());
            }
        }
        return total.rounded(2, RoundingMode.HALF_UP);
    }

    private Money capAtTotalDebit(Money requestedAmount, Money totalDebitAmount) {
        Money normalizedRequestedAmount = normalizeMoney(requestedAmount, totalDebitAmount);
        if (normalizedRequestedAmount.isGreaterThan(totalDebitAmount)) {
            return totalDebitAmount.rounded(2, RoundingMode.HALF_UP);
        }
        return normalizedRequestedAmount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeMoney(Money preferredAmount, Money fallbackAmount) {
        if (preferredAmount != null) {
            return preferredAmount.rounded(2, RoundingMode.HALF_UP);
        }
        return fallbackAmount == null ? null : fallbackAmount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroOf(Money amount) {
        Money normalizedAmount = Objects.requireNonNull(amount, "amount must not be null");
        return Money.zero(normalizedAmount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
    }

    private boolean isPositive(Money amount) {
        return amount != null && amount.getAmount().signum() > 0;
    }

    private SettlementPlanSnapshot loadSettlementPlan(PayOrder payOrder) {
        String payload = normalizeRequired(payOrder.getSettlementPlanSnapshot(), "settlementPlanSnapshot");
        return SettlementPlanSnapshot.fromPayload(payload);
    }

    private SourceBizSnapshot loadSourceBizSnapshot(PayOrder payOrder) {
        String payload = normalizeOptional(payOrder.getSourceBizSnapshot());
        if (payload == null) {
            return null;
        }
        return SourceBizSnapshot.fromPayload(payload);
    }

    private boolean hasOutboundBranch(List<PayParticipantBranch> branches) {
        return branches.stream().anyMatch(branch -> branch.getParticipantType() == PayParticipantType.OUTBOUND);
    }

    private String findBranchId(List<PayParticipantBranch> branches,
                                PayParticipantType participantType,
                                String defaultValue) {
        return branches.stream()
                .filter(branch -> branch.getParticipantType() == participantType)
                .map(PayParticipantBranch::getBranchId)
                .filter(this::hasText)
                .findFirst()
                .orElse(defaultValue);
    }

    private String findBranchResource(List<PayParticipantBranch> branches,
                                      PayParticipantType participantType,
                                      String defaultValue) {
        return branches.stream()
                .filter(branch -> branch.getParticipantType() == participantType)
                .map(PayParticipantBranch::getParticipantResourceId)
                .filter(this::hasText)
                .findFirst()
                .orElse(defaultValue);
    }

    private String findBranchPayload(List<PayParticipantBranch> branches, PayParticipantType participantType) {
        return branches.stream()
                .filter(branch -> branch.getParticipantType() == participantType)
                .map(PayParticipantBranch::getRequestPayload)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String findFundCodeFromBranch(List<PayParticipantBranch> branches) {
        Map<String, String> payload = parsePayload(findBranchPayload(branches, PayParticipantType.FUND_ACCOUNT));
        return defaultValue(payload.get("fundCode"), findBranchResource(branches, PayParticipantType.FUND_ACCOUNT, null));
    }

    private Map<String, String> parsePayload(String payload) {
        Map<String, String> values = new LinkedHashMap<>();
        String normalizedPayload = normalizeOptional(payload);
        if (normalizedPayload == null) {
            return values;
        }
        for (String part : normalizedPayload.split(";")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2 && hasText(keyValue[0])) {
                values.put(keyValue[0].trim(), normalizeOptional(keyValue[1]));
            }
        }
        return values;
    }

    private boolean isWithdrawScene(String businessSceneCode) {
        String upper = normalizeUpper(businessSceneCode);
        return upper.equals("WITHDRAW") || upper.contains("WITHDRAW");
    }

    private boolean isDepositScene(String businessSceneCode) {
        String upper = normalizeUpper(businessSceneCode);
        return upper.equals("DEPOSIT") || upper.contains("DEPOSIT");
    }

    private boolean isTransferOutScene(String businessSceneCode) {
        String upper = normalizeUpper(businessSceneCode);
        return upper.contains("TRANSFER_OUT")
                || upper.contains("REDEEM")
                || upper.contains("FAST_REDEEM")
                || upper.contains("WITHDRAW");
    }

    private boolean isCreditRepayScene(String businessSceneCode) {
        String upper = normalizeUpper(businessSceneCode);
        return upper.equals("APP_CREDIT_REPAY")
                || (upper.contains("CREDIT") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AICREDIT) && upper.contains("REPAY"))
                || (upper.contains("AICREDIT") && upper.contains("REPAY"))
                || (upper.contains("LOAN") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AILOAN) && upper.contains("REPAY"))
                || (upper.contains("AILOAN") && upper.contains("REPAY"));
    }

    private String resolveCreditSubjectCode(String accountDomain) {
        String domain = normalizeUpper(accountDomain);
        if (domain.equals(CreditProductCodes.AILOAN)) {
            return AccountingSubjectCodes.ASSET_AILOAN_RECEIVABLE;
        }
        return AccountingSubjectCodes.ASSET_AICREDIT_RECEIVABLE;
    }

    private String resolveFeeIncomeSubjectCode(PayOrder payOrder) {
        if (payOrder != null && isWithdrawScene(payOrder.getBusinessSceneCode())) {
            return AccountingSubjectCodes.INCOME_WITHDRAW_SERVICE_FEE;
        }
        return AccountingSubjectCodes.INCOME_PAYMENT_SERVICE_FEE;
    }

    private String normalizeUpper(String raw) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
    }

    private String amountText(Money amount) {
        return amount == null
                ? "0.00"
                : amount.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String defaultValue(String value, String defaultValue) {
        String normalized = normalizeOptional(value);
        return normalized == null ? defaultValue : normalized;
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

    private boolean hasText(String raw) {
        return normalizeOptional(raw) != null;
    }

    private record PrimaryCreditSpec(
            /** account域信息 */
            String accountDomain,
            /** account类型 */
            String accountType,
            /** account单号 */
            String accountNo,
            /** 所属类型 */
            String ownerType,
            /** 所属ID */
            Long ownerId,
            /** 金额 */
            Money amount,
            /** 业务角色信息 */
            String bizRole,
            /** 科目hint信息 */
            String subjectHint,
            /** reference单号 */
            String referenceNo,
            /** 扩展信息 */
            String metadata
    ) {
    }
}
