package cn.openaipay.application.pay.service.impl;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxPublisher;
import cn.openaipay.application.pay.async.PayExecuteRequestedPayload;
import cn.openaipay.application.pay.async.PayAccountingEventRequestedPayload;
import cn.openaipay.application.pay.async.PayResultChangedPayload;
import cn.openaipay.application.pay.command.SettlementPlanSnapshot;
import cn.openaipay.application.pay.async.PayReconRequestedPayload;
import cn.openaipay.application.pay.dto.PayFundDetailSummaryDTO;
import cn.openaipay.application.pay.command.SubmitPayCommand;
import cn.openaipay.application.pay.dto.PayOrderDTO;
import cn.openaipay.application.pay.dto.PayParticipantBranchDTO;
import cn.openaipay.application.pay.dto.PaySplitPlanDTO;
import cn.openaipay.application.pay.dto.PaySubmitReceiptDTO;
import cn.openaipay.application.pay.service.PayService;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.pay.client.BankCardClient;
import cn.openaipay.domain.pay.client.CouponClient;
import cn.openaipay.domain.pay.client.FundAccountClient;
import cn.openaipay.domain.pay.client.InboundClient;
import cn.openaipay.domain.pay.client.OutboundClient;
import cn.openaipay.domain.pay.client.PayBankCardSnapshot;
import cn.openaipay.domain.pay.client.PayCreditRouteSnapshot;
import cn.openaipay.domain.pay.client.PayFundFreezeResultSnapshot;
import cn.openaipay.domain.pay.client.PayInboundOrderSnapshot;
import cn.openaipay.domain.pay.client.PayInboundSubmitRequest;
import cn.openaipay.domain.pay.client.PayOutboundOrderSnapshot;
import cn.openaipay.domain.pay.client.PayOutboundSubmitRequest;
import cn.openaipay.domain.pay.client.PayRouteClient;
import cn.openaipay.domain.pay.client.WalletAccountClient;
import cn.openaipay.domain.pay.model.PayBankCardFundDetail;
import cn.openaipay.domain.pay.model.PayCreditAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundDetailOwner;
import cn.openaipay.domain.pay.model.PayFundAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundDetailSummary;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayOrderStatus;
import cn.openaipay.domain.pay.model.PayParticipantBranch;
import cn.openaipay.domain.pay.model.PayParticipantStatus;
import cn.openaipay.domain.pay.model.PayParticipantType;
import cn.openaipay.domain.pay.model.PayRedPacketFundDetail;
import cn.openaipay.domain.pay.model.PaySplitPlan;
import cn.openaipay.domain.pay.model.PayWalletFundDetail;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import cn.openaipay.domain.pay.service.PayOrderDomainService;
import cn.openaipay.domain.pay.service.PayOrderSubmission;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 支付应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PayServiceImpl implements PayService {
    /** 默认信息 */
    private static final String DEFAULT_CURRENCY = "CNY";
    /** 状态 */
    private static final String STATUS_RECON_PENDING = "RECON_PENDING";
    /** 入金渠道后缀 */
    private static final String BANK_DEPOSIT_CHANNEL_SUFFIX = "101";
    /** 出金渠道后缀 */
    private static final String BANK_WITHDRAW_CHANNEL_SUFFIX = "001";

    /** PayOrderRepository组件 */
    private final PayOrderRepository payOrderRepository;
    /** 支付订单领域服务。 */
    private final PayOrderDomainService payOrderDomainService;
    /** 钱包账户客户端 */
    private final WalletAccountClient walletAccountClient;
    /** 支付路由客户端 */
    private final PayRouteClient payRouteClient;
    /** 基金账户客户端 */
    private final FundAccountClient fundAccountClient;
    /** 优惠券客户端 */
    private final CouponClient couponClient;
    /** 银行卡客户端 */
    private final BankCardClient bankCardClient;
    /** 入金客户端 */
    private final InboundClient inboundClient;
    /** 出金客户端 */
    private final OutboundClient outboundClient;
    /** 标准 Outbox 消息发布器 */
    private final OutboxPublisher outboxPublisher;
    /** AiPayIdGenerator组件 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AiPayBizTypeRegistry组件 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;
    /** 是否在转账场景跳过资金明细落库 */
    private final boolean skipFundDetailForTransfer;
    /** 是否在转账场景跳过TRY成功状态落库 */
    private final boolean skipTrySuccessBranchPersistForTransfer;

    public PayServiceImpl(PayOrderRepository payOrderRepository,
                                     PayOrderDomainService payOrderDomainService,
                                     WalletAccountClient walletAccountClient,
                                     PayRouteClient payRouteClient,
                                     FundAccountClient fundAccountClient,
                                     CouponClient couponClient,
                                     BankCardClient bankCardClient,
                                     InboundClient inboundClient,
                                     OutboundClient outboundClient,
                                     OutboxPublisher outboxPublisher,
                                     AiPayIdGenerator aiPayIdGenerator,
                                     AiPayBizTypeRegistry aiPayBizTypeRegistry,
                                     @Value("${aipay.pay.skip-fund-detail-for-transfer:false}") boolean skipFundDetailForTransfer,
                                     @Value("${aipay.pay.skip-try-success-branch-persist-for-transfer:true}") boolean skipTrySuccessBranchPersistForTransfer) {
        this.payOrderRepository = payOrderRepository;
        this.payOrderDomainService = payOrderDomainService;
        this.walletAccountClient = walletAccountClient;
        this.payRouteClient = payRouteClient;
        this.fundAccountClient = fundAccountClient;
        this.couponClient = couponClient;
        this.bankCardClient = bankCardClient;
        this.inboundClient = inboundClient;
        this.outboundClient = outboundClient;
        this.outboxPublisher = outboxPublisher;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
        this.skipFundDetailForTransfer = skipFundDetailForTransfer;
        this.skipTrySuccessBranchPersistForTransfer = skipTrySuccessBranchPersistForTransfer;
    }

    /**
     * 提交业务数据。
     */
    @Override
    @Transactional
    public PaySubmitReceiptDTO submit(SubmitPayCommand command) {
        validateSubmitCommand(command);
        String sourceBizType = normalizeRequired(command.sourceBizType(), "sourceBizType");
        String sourceBizNo = normalizeRequired(command.sourceBizNo(), "sourceBizNo");

        PayOrder existedBizOrder = payOrderRepository.findOrderByBizOrderNo(command.bizOrderNo())
                .orElse(null);
        if (existedBizOrder != null) {
            return toPaySubmitReceiptDTO(existedBizOrder);
        }

        PayOrder latestSourceOrder = payOrderRepository.findLatestOrderBySourceBiz(sourceBizType, sourceBizNo)
                .orElse(null);
        if (latestSourceOrder != null) {
            if (latestSourceOrder.getStatus() == PayOrderStatus.COMMITTED) {
                return toPaySubmitReceiptDTO(latestSourceOrder);
            }
            if (!latestSourceOrder.isTerminal()) {
                return toPaySubmitReceiptDTO(latestSourceOrder);
            }
        }
        int attemptNo = latestSourceOrder == null ? 1 : latestSourceOrder.getAttemptNo() + 1;

        SettlementPlanSnapshot settlementPlan = command.settlementPlan();

        LocalDateTime now = LocalDateTime.now();
        Money discountAmount = resolveDiscount(settlementPlan.couponNo());
        String payOrderNo = buildPayOrderNo(command.payerUserId());
        String globalTxId = buildGlobalTxId(payOrderNo);
        String tradeOrderNo = resolveTradeOrderNo(command.tradeOrderNo(), sourceBizType, sourceBizNo);
        PayOrder payOrder = payOrderDomainService.createSubmittedOrder(new PayOrderSubmission(
                payOrderNo,
                tradeOrderNo,
                command.bizOrderNo(),
                sourceBizType,
                sourceBizNo,
                attemptNo,
                command.sourceBizSnapshot() == null ? null : command.sourceBizSnapshot().toPayload(),
                command.businessSceneCode(),
                command.payerUserId(),
                command.payeeUserId(),
                settlementPlan.originalAmount(),
                settlementPlan.walletDebitAmount(),
                settlementPlan.fundDebitAmount(),
                settlementPlan.creditDebitAmount(),
                settlementPlan.inboundDebitAmount(),
                discountAmount,
                settlementPlan.couponNo(),
                settlementPlan.toPayload(),
                globalTxId,
                now
        ));
        payOrder = payOrderRepository.saveOrder(payOrder);
        publishExecuteRequested(payOrder);
        return toPaySubmitReceiptDTO(payOrder);
    }

    /**
     * 按支付订单单号查询记录。
     */
    @Override
    @Transactional(readOnly = true)
    public PayOrderDTO queryByPayOrderNo(String payOrderNo) {
        return toPayOrderDTO(mustGetOrder(normalizeRequired(payOrderNo, "payOrderNo")), true);
    }

    /**
     * 按业务查询记录列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<PayOrderDTO> listBySourceBiz(String sourceBizType, String sourceBizNo) {
        return payOrderRepository.findOrdersBySourceBiz(
                        normalizeRequired(sourceBizType, "sourceBizType"),
                        normalizeRequired(sourceBizNo, "sourceBizNo")
                )
                .stream()
                .map(payOrder -> toPayOrderDTO(payOrder, true))
                .toList();
    }

    /**
     * 查询业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public PayParticipantBranchDTO queryParticipantBranch(String payOrderNo, String participantType) {
        return payOrderRepository.findParticipantBranch(
                        normalizeRequired(payOrderNo, "payOrderNo"),
                        normalizeRequired(participantType, "participantType")
                )
                .map(this::toPayParticipantBranchDTO)
                .orElse(null);
    }

    /**
     * 处理业务数据。
     */
    @Transactional
    public void executeSubmittedPayment(String payOrderNo) {
        PayOrder payOrder = mustGetOrder(normalizeRequired(payOrderNo, "payOrderNo"));
        if (payOrder.isTerminal() || payOrder.getStatus() == PayOrderStatus.RECON_PENDING) {
            publishPostExecutionEvents(payOrder);
            return;
        }

        SettlementPlanSnapshot settlementPlan;
        try {
            settlementPlan = loadSettlementPlan(payOrder);
        } catch (RuntimeException ex) {
            payOrder.markFailed("invalid settlement plan: " + compactError(ex.getMessage()), LocalDateTime.now());
            payOrder = payOrderRepository.saveOrder(payOrder);
            publishPostExecutionEvents(payOrder);
            return;
        }

        if (payOrder.getStatus() == PayOrderStatus.SUBMITTED) {
            validateNonNegativeParticipantDebits(payOrder);
            payOrder = prepareSubmittedOrder(payOrder, settlementPlan);
        }

        if (payOrder.getStatus() == PayOrderStatus.PREPARED) {
            validateNonNegativeParticipantDebits(payOrder);
            payOrder = commitPreparedOrder(payOrder);
        }

        if (payOrder.isTerminal() || payOrder.getStatus() == PayOrderStatus.RECON_PENDING) {
            publishPostExecutionEvents(payOrder);
            return;
        }

        payOrder.markReconPending("unsupported async pay order status: " + payOrder.getStatus().name(), LocalDateTime.now());
        payOrder = payOrderRepository.saveOrder(payOrder);
        publishPostExecutionEvents(payOrder);
    }

    /**
     * 处理业务数据。
     */
    @Transactional
    public void reconcilePendingPayment(String payOrderNo) {
        PayOrder payOrder = mustGetOrder(normalizeRequired(payOrderNo, "payOrderNo"));
        if (payOrder.isTerminal()) {
            publishPostExecutionEvents(payOrder);
            return;
        }
        if (payOrder.getStatus() != PayOrderStatus.RECON_PENDING) {
            return;
        }

        List<PayParticipantBranch> branches = payOrderRepository.findParticipantBranches(payOrder.getPayOrderNo());
        if (branches.isEmpty()) {
            throw new IllegalStateException("recon pending pay order has no participant branches");
        }

        if (allBranchesCommitted(branches)) {
            payOrder.markCommitted(LocalDateTime.now());
            payOrder = payOrderRepository.saveOrder(payOrder);
            publishPostExecutionEvents(payOrder);
            return;
        }

        if (shouldReconcileByRollback(branches)) {
            payOrder = reconcileRollbackPendingOrder(payOrder, branches);
        } else {
            payOrder = commitPreparedOrder(payOrder);
        }

        if (payOrder.getStatus() == PayOrderStatus.RECON_PENDING) {
            throw new IllegalStateException(defaultValue(payOrder.getResultMessage(), "pay reconcile pending"));
        }

        publishPostExecutionEvents(payOrder);
    }

    private void prepareParticipant(PayOrder payOrder,
                                    PayParticipantType participantType,
                                    String fundCode,
                                    String paymentToolCode,
                                    String paymentMethod,
                                    Money outboundAmount,
                                    List<PayParticipantBranch> executedBranches) {
        Money amount = amountForParticipant(payOrder, participantType);
        if (participantType == PayParticipantType.COUPON && payOrder.getCouponNo() == null) {
            return;
        }
        if (participantType != PayParticipantType.COUPON && amount.isZero()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String branchId = buildBranchId(payOrder, participantType);
        PayCreditRouteSnapshot creditRoute = participantType == PayParticipantType.CREDIT_ACCOUNT
                ? resolveCreditRoute(payOrder, paymentMethod)
                : null;
        String resourceId = resolveParticipantResourceId(payOrder, participantType, fundCode, creditRoute);
        String requestPayload = buildParticipantPayload(
                payOrder,
                participantType,
                fundCode,
                amount,
                paymentToolCode,
                outboundAmount,
                creditRoute
        );

        PayParticipantBranch branch = PayParticipantBranch.create(
                payOrder.getPayOrderNo(),
                participantType,
                branchId,
                resourceId,
                requestPayload,
                now
        );
        branch = payOrderRepository.saveParticipantBranch(branch);
        String initialRequestPayload = branch.getRequestPayload();

        try {
            switch (participantType) {
                case COUPON -> prepareCoupon(payOrder);
                case WALLET_ACCOUNT -> prepareWallet(payOrder, branchId, amount);
                case OUTBOUND -> prepareOutbound(payOrder, branch, amount, paymentToolCode);
                case FUND_ACCOUNT -> prepareFund(payOrder, branch, fundCode, amount);
                case CREDIT_ACCOUNT -> prepareCredit(payOrder, branchId, amount, creditRoute);
                case INBOUND -> prepareInbound(payOrder, branch, amount, paymentToolCode);
            }
            branch.markTryOk("try success", LocalDateTime.now());
            boolean requestPayloadChanged = !Objects.equals(initialRequestPayload, branch.getRequestPayload());
            boolean walletTrySuccessUpdateSkippable = participantType == PayParticipantType.WALLET_ACCOUNT
                    && !requestPayloadChanged;
            boolean needPersistTrySuccess = requestPayloadChanged
                    || (!walletTrySuccessUpdateSkippable
                    && !(skipTrySuccessBranchPersistForTransfer && isTransferScene(payOrder.getBusinessSceneCode())));
            if (needPersistTrySuccess) {
                branch = payOrderRepository.saveParticipantBranch(branch);
            }
            if (participantType != PayParticipantType.INBOUND && participantType != PayParticipantType.OUTBOUND) {
                saveFundDetail(payOrder, participantType, branch, amount);
            }
            executedBranches.add(branch);
        } catch (RuntimeException ex) {
            branch.markTryFailed(compactError(ex.getMessage()), LocalDateTime.now());
            payOrderRepository.saveParticipantBranch(branch);
            throw ex;
        }
    }

    private PayOrder prepareSubmittedOrder(PayOrder payOrder, SettlementPlanSnapshot settlementPlan) {
        payOrder.markTrying(LocalDateTime.now());
        payOrder = payOrderRepository.saveOrder(payOrder);

        List<PayParticipantBranch> executedBranches = new ArrayList<>();
        try {
            for (PayParticipantType participantType : payOrderDomainService.resolvePreparationSequence(payOrder)) {
                prepareParticipant(
                        payOrder,
                        participantType,
                        settlementPlan.fundCode(),
                        settlementPlan.paymentToolCode(),
                        settlementPlan.paymentMethod(),
                        settlementPlan.outboundAmount(),
                        executedBranches
                );
            }
            payOrder.markPrepared(LocalDateTime.now());
            return payOrderRepository.saveOrder(payOrder);
        } catch (RuntimeException ex) {
            String errorMessage = compactError(ex.getMessage());
            rollbackExecutedBranches(payOrder, executedBranches, "prepare failed: " + errorMessage);
            payOrder.markRolledBack(errorMessage, LocalDateTime.now());
            return payOrderRepository.saveOrder(payOrder);
        }
    }

    private PayOrder commitPreparedOrder(PayOrder payOrder) {
        payOrder.markCommitting(LocalDateTime.now());
        payOrder = payOrderRepository.saveOrder(payOrder);

        List<PayParticipantBranch> branches = sortForCommit(payOrderRepository.findParticipantBranches(payOrder.getPayOrderNo()));
        try {
            for (PayParticipantBranch branch : branches) {
                if (branch.getStatus() == PayParticipantStatus.CONFIRM_OK || branch.getStatus() == PayParticipantStatus.SKIPPED) {
                    continue;
                }
                confirmBranch(payOrder, branch);
            }
            payOrder.markCommitted(LocalDateTime.now());
            payOrder = payOrderRepository.saveOrder(payOrder);
            publishAccountingEventRequested(payOrder);
            return payOrder;
        } catch (BranchReconPendingException ex) {
            payOrder.markReconPending(compactError(ex.getMessage()), LocalDateTime.now());
            return payOrderRepository.saveOrder(payOrder);
        } catch (RuntimeException ex) {
            String errorMessage = compactError(ex.getMessage());
            payOrder.markRollingBack(LocalDateTime.now());
            payOrder = payOrderRepository.saveOrder(payOrder);
            try {
                rollbackExecutedBranches(payOrder, branches, "commit failed: " + errorMessage);
                payOrder.markRolledBack("commit failed: " + errorMessage, LocalDateTime.now());
                return payOrderRepository.saveOrder(payOrder);
            } catch (RuntimeException rollbackEx) {
                payOrder.markReconPending(
                        "commit failed: " + errorMessage + "; rollback failed: " + compactError(rollbackEx.getMessage()),
                        LocalDateTime.now()
                );
                return payOrderRepository.saveOrder(payOrder);
            }
        }
    }

    private void prepareCoupon(PayOrder payOrder) {
        couponClient.reserveCoupon(payOrder.getCouponNo());
    }

    private void prepareWallet(PayOrder payOrder, String branchId, Money amount) {
        walletAccountClient.tccTry(
                payOrder.getGlobalTxId(),
                branchId,
                payOrder.getPayerUserId(),
                "DEBIT",
                "PAY_HOLD",
                amount,
                payOrder.getPayOrderNo()
        );
    }

    private void prepareFund(PayOrder payOrder,
                             PayParticipantBranch branch,
                             String fundCode,
                             Money amount) {
        PayFundFreezeResultSnapshot freezeResult = fundAccountClient.freezeShareForPay(
                branch.getBranchId(),
                payOrder.getPayerUserId(),
                fundCode,
                amount,
                payOrder.getPayOrderNo()
        );

        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        payload.put("fundTradeOrderNo", branch.getBranchId());
        payload.put("fundCode", freezeResult.fundCode());
        payload.put("share", freezeResult.share().toPlainString());
        payload.put("nav", freezeResult.nav().toPlainString());
        branch.updateRequestPayload(toPayload(payload), LocalDateTime.now());
    }

    private void prepareCredit(PayOrder payOrder, String branchId, Money amount, PayCreditRouteSnapshot creditRoute) {
        payRouteClient.tccTryCredit(
                payOrder.getGlobalTxId(),
                branchId,
                normalizeRequired(creditRoute.accountNo(), "accountNo"),
                normalizeRequired(creditRoute.operationType(), "operationType"),
                normalizeRequired(creditRoute.assetCategory(), "assetCategory"),
                amount,
                payOrder.getPayOrderNo()
        );
    }

    private void prepareInbound(PayOrder payOrder, PayParticipantBranch branch, Money amount, String paymentToolCode) {
        PayBankCardSnapshot selectedBankCard = resolveSelectedChannelBankCard(payOrder.getPayerUserId(), paymentToolCode);
        String payerAccountNo = normalizeRequired(selectedBankCard.cardNo(), "cardNo");
        BankChannelCodes channelCodes = resolveBankChannelCodes(selectedBankCard.bankCode(), false);

        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        payload.put("payerAccountNo", payerAccountNo);
        payload.put("payChannelCode", channelCodes.payChannelCode());
        payload.put("instChannelCode", channelCodes.instChannelCode());
        payload.put("instId", channelCodes.instId());
        payload.put("bankCode", channelCodes.instId());
        payload.put("requestBizNo", buildInboundRequestBizNo(payOrder));
        payload.put("bizOrderNo", payOrder.getBizOrderNo());
        if (normalizeOptional(payOrder.getTradeOrderNo()) != null) {
            payload.put("tradeOrderNo", payOrder.getTradeOrderNo());
        }
        payload.put("payOrderNo", payOrder.getPayOrderNo());
        payload.put("requestIdentify", buildInboundRequestIdentify(payOrder));
        payload.put("bizIdentity", "OPENAIPAY");
        if (normalizeOptional(selectedBankCard.bankName()) != null) {
            payload.put("bankName", selectedBankCard.bankName().trim());
        }
        if (normalizeOptional(selectedBankCard.cardType()) != null) {
            payload.put("cardType", selectedBankCard.cardType().trim());
        }
        if (normalizeOptional(selectedBankCard.cardHolderName()) != null) {
            payload.put("cardHolderName", selectedBankCard.cardHolderName().trim());
        }
        String cardTailNo = resolveCardTailNo(payerAccountNo);
        if (cardTailNo != null) {
            payload.put("cardTailNo", cardTailNo);
        }
        payload.put("toolSnapshot", buildBankToolSnapshot(
                channelCodes.instId(),
                channelCodes.instChannelCode(),
                channelCodes.payChannelCode(),
                channelCodes.instId(),
                selectedBankCard.bankName(),
                selectedBankCard.cardType(),
                selectedBankCard.cardHolderName(),
                cardTailNo,
                payerAccountNo
        ));
        if (normalizeOptional(paymentToolCode) != null) {
            payload.put("paymentToolCode", normalizeOptional(paymentToolCode));
        }
        branch.updateRequestPayload(toPayload(payload), LocalDateTime.now());
    }

    private void prepareOutbound(PayOrder payOrder, PayParticipantBranch branch, Money amount, String paymentToolCode) {
        PayBankCardSnapshot selectedBankCard = resolveSelectedChannelBankCard(payOrder.getPayerUserId(), paymentToolCode);
        String payeeAccountNo = normalizeRequired(selectedBankCard.cardNo(), "cardNo");
        BankChannelCodes channelCodes = resolveBankChannelCodes(selectedBankCard.bankCode(), true);

        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        payload.put("payeeAccountNo", payeeAccountNo);
        payload.put("payChannelCode", channelCodes.payChannelCode());
        payload.put("instChannelCode", channelCodes.instChannelCode());
        payload.put("instId", channelCodes.instId());
        payload.put("bankCode", channelCodes.instId());
        payload.put("requestBizNo", buildOutboundRequestBizNo(payOrder));
        payload.put("bizOrderNo", payOrder.getBizOrderNo());
        if (normalizeOptional(payOrder.getTradeOrderNo()) != null) {
            payload.put("tradeOrderNo", payOrder.getTradeOrderNo());
        }
        payload.put("payOrderNo", payOrder.getPayOrderNo());
        payload.put("requestIdentify", buildOutboundRequestIdentify(payOrder));
        payload.put("bizIdentity", "OPENAIPAY");
        if (normalizeOptional(selectedBankCard.bankName()) != null) {
            payload.put("bankName", selectedBankCard.bankName().trim());
        }
        if (normalizeOptional(selectedBankCard.cardType()) != null) {
            payload.put("cardType", selectedBankCard.cardType().trim());
        }
        if (normalizeOptional(selectedBankCard.cardHolderName()) != null) {
            payload.put("cardHolderName", selectedBankCard.cardHolderName().trim());
        }
        String cardTailNo = resolveCardTailNo(payeeAccountNo);
        if (cardTailNo != null) {
            payload.put("cardTailNo", cardTailNo);
        }
        payload.put("toolSnapshot", buildBankToolSnapshot(
                channelCodes.instId(),
                channelCodes.instChannelCode(),
                channelCodes.payChannelCode(),
                channelCodes.instId(),
                selectedBankCard.bankName(),
                selectedBankCard.cardType(),
                selectedBankCard.cardHolderName(),
                cardTailNo,
                payeeAccountNo
        ));
        if (normalizeOptional(paymentToolCode) != null) {
            payload.put("paymentToolCode", normalizeOptional(paymentToolCode));
        }
        branch.updateRequestPayload(toPayload(payload), LocalDateTime.now());
    }

    private void saveFundDetail(PayOrder payOrder,
                                PayParticipantType participantType,
                                PayParticipantBranch branch,
                                Money amount) {
        if (amount.isZero()) {
            return;
        }
        if (skipFundDetailForTransfer && isTransferScene(payOrder.getBusinessSceneCode())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        switch (participantType) {
            case COUPON -> {
                if (payOrder.getCouponNo() == null) {
                    return;
                }
                PayRedPacketFundDetail fundDetail = PayRedPacketFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYER,
                        amount,
                        payOrder.getCouponNo(),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            case WALLET_ACCOUNT -> {
                PayWalletFundDetail fundDetail = PayWalletFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYER,
                        amount,
                        branch.getParticipantResourceId(),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            case FUND_ACCOUNT -> {
                Map<String, String> payload = parsePayload(branch.getRequestPayload());
                String fundCode = firstNonBlank(payload.get("fundCode"), branch.getParticipantResourceId());
                if (fundCode == null) {
                    fundCode = resolveFundCode(payOrder.getPayerUserId(), null);
                }
                PayFundAccountFundDetail fundDetail = PayFundAccountFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYER,
                        amount,
                        fundCode,
                        resolveFundProductCode(fundCode),
                        buildFundAccountIdentity(payOrder.getPayerUserId(), fundCode),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            case CREDIT_ACCOUNT -> {
                String accountNo = normalizeRequired(branch.getParticipantResourceId(), "participantResourceId");
                CreditAccountType accountType = CreditAccountType.fromAccountNo(accountNo);
                PayCreditAccountFundDetail fundDetail = PayCreditAccountFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYER,
                        amount,
                        accountNo,
                        accountType,
                        resolveCreditProductCode(accountType),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            case INBOUND -> {
                Map<String, String> payload = parsePayload(branch.getRequestPayload());
                PayBankCardFundDetail fundDetail = PayBankCardFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYER,
                        amount,
                        payload.get("payChannelCode"),
                        payload.get("instId"),
                        payload.get("instChannelCode"),
                        payload.get("payChannelCode"),
                        payload.get("bankCode"),
                        payload.get("bankName"),
                        payload.get("cardType"),
                        payload.get("cardHolderName"),
                        payload.get("cardTailNo"),
                        payload.get("toolSnapshot"),
                        payload.get("inboundOrderNo"),
                        payload.get("payerAccountNo"),
                        Money.zero(amount.getCurrencyUnit()),
                        payload.get("inboundId"),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            case OUTBOUND -> {
                Map<String, String> payload = parsePayload(branch.getRequestPayload());
                PayBankCardFundDetail fundDetail = PayBankCardFundDetail.create(
                        payOrder.getPayOrderNo(),
                        PayFundDetailOwner.PAYEE,
                        amount,
                        payload.get("payChannelCode"),
                        payload.get("instId"),
                        payload.get("instChannelCode"),
                        payload.get("payChannelCode"),
                        payload.get("bankCode"),
                        payload.get("bankName"),
                        payload.get("cardType"),
                        payload.get("cardHolderName"),
                        payload.get("cardTailNo"),
                        payload.get("toolSnapshot"),
                        payload.get("outboundOrderNo"),
                        payload.get("payeeAccountNo"),
                        Money.zero(amount.getCurrencyUnit()),
                        payload.get("outboundId"),
                        now
                );
                payOrderRepository.saveFundDetail(fundDetail);
            }
            default -> {
                // 无其他资金明细
            }
        }
    }

    private void rollbackExecutedBranches(PayOrder payOrder,
                                          List<PayParticipantBranch> branches,
                                          String reason) {
        List<PayParticipantBranch> sorted = sortForRollback(branches);
        for (PayParticipantBranch branch : sorted) {
            if (branch.getStatus() == PayParticipantStatus.CANCEL_OK || branch.getStatus() == PayParticipantStatus.SKIPPED) {
                continue;
            }
            try {
                cancelBranch(payOrder, branch);
            } catch (RuntimeException ex) {
                branch.markTryFailed("cancel failed: " + compactError(ex.getMessage()), LocalDateTime.now());
                payOrderRepository.saveParticipantBranch(branch);
            }
        }
        if (reason != null && !reason.isBlank()) {
            payOrder.markFailed(compactError(reason), LocalDateTime.now());
            payOrderRepository.saveOrder(payOrder);
        }
    }

    private void cancelBranch(PayOrder payOrder, PayParticipantBranch branch) {
        Money amount = amountForParticipant(payOrder, branch.getParticipantType());
        switch (branch.getParticipantType()) {
            case COUPON -> cancelCoupon(payOrder);
            case WALLET_ACCOUNT -> walletAccountClient.tccCancel(
                    payOrder.getGlobalTxId(),
                    branch.getBranchId(),
                    payOrder.getPayerUserId(),
                    "DEBIT",
                    "PAY_HOLD",
                    amount,
                    payOrder.getPayOrderNo()
            );
            case FUND_ACCOUNT -> cancelFund(payOrder, branch);
            case CREDIT_ACCOUNT -> cancelCredit(payOrder, branch, amount);
            case INBOUND -> cancelInbound(branch);
            case OUTBOUND -> cancelOutbound(branch);
        }
        branch.markCancelOk("cancel success", LocalDateTime.now());
        payOrderRepository.saveParticipantBranch(branch);
    }

    private void cancelCoupon(PayOrder payOrder) {
        couponClient.releaseCoupon(payOrder.getCouponNo());
    }

    private void cancelFund(PayOrder payOrder, PayParticipantBranch branch) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        String fundCode = payload.get("fundCode");
        if (fundCode == null || fundCode.isBlank()) {
            fundCode = resolveFundCode(payOrder.getPayerUserId(), null);
        }
        fundAccountClient.compensateFrozenShareForPay(
                payOrder.getPayerUserId(),
                branch.getBranchId(),
                fundCode,
                payOrder.getPayOrderNo()
        );
    }

    private void cancelCredit(PayOrder payOrder, PayParticipantBranch branch, Money amount) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        String accountNo = branch.getParticipantResourceId();
        payRouteClient.tccCancelCredit(
                payOrder.getGlobalTxId(),
                branch.getBranchId(),
                normalizeRequired(accountNo, "participantResourceId"),
                normalizeRequired(payload.get("operationType"), "operationType"),
                normalizeRequired(payload.get("assetCategory"), "assetCategory"),
                amount,
                payOrder.getPayOrderNo()
        );
    }

    private void cancelInbound(PayParticipantBranch branch) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        String inboundId = normalizeOptional(payload.get("inboundId"));
        if (inboundId == null) {
            String requestBizNo = normalizeOptional(payload.get("requestBizNo"));
            if (requestBizNo == null) {
                return;
            }
            try {
                inboundId = inboundClient.queryByRequestBizNo(requestBizNo).inboundId();
            } catch (NoSuchElementException ex) {
                return;
            }
        }
        PayInboundOrderSnapshot canceled = inboundClient.cancelDeposit(inboundId, "pay rollback");
        if (!"CANCELED".equals(canceled.inboundStatus())) {
            throw new IllegalStateException("inbound cancel status is not CANCELED: " + canceled.inboundStatus());
        }
    }

    private void cancelOutbound(PayParticipantBranch branch) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        String outboundId = normalizeOptional(payload.get("outboundId"));
        if (outboundId == null) {
            String requestBizNo = normalizeOptional(payload.get("requestBizNo"));
            if (requestBizNo == null) {
                return;
            }
            try {
                outboundId = outboundClient.queryByRequestBizNo(requestBizNo).outboundId();
            } catch (NoSuchElementException ex) {
                return;
            }
        }
        PayOutboundOrderSnapshot canceled = outboundClient.cancelWithdraw(outboundId, "pay rollback");
        if (!"CANCELED".equals(canceled.outboundStatus())) {
            throw new IllegalStateException("outbound cancel status is not CANCELED: " + canceled.outboundStatus());
        }
    }

    private void confirmBranch(PayOrder payOrder, PayParticipantBranch branch) {
        Money amount = amountForParticipant(payOrder, branch.getParticipantType());
        switch (branch.getParticipantType()) {
            case COUPON -> confirmCoupon(payOrder);
            case WALLET_ACCOUNT -> walletAccountClient.tccConfirm(payOrder.getGlobalTxId(), branch.getBranchId());
            case FUND_ACCOUNT -> confirmFund(payOrder, branch);
            case CREDIT_ACCOUNT -> confirmCreditBranch(payOrder.getGlobalTxId(), branch);
            case INBOUND -> confirmInbound(payOrder, branch, amount);
            case OUTBOUND -> confirmOutbound(payOrder, branch, amount);
        }
        branch.markConfirmOk("confirm success", LocalDateTime.now());
        payOrderRepository.saveParticipantBranch(branch);
    }

    private void confirmCoupon(PayOrder payOrder) {
        couponClient.consumeCoupon(
                payOrder.getCouponNo(),
                payOrder.getBizOrderNo(),
                payOrder.getTradeOrderNo(),
                payOrder.getPayOrderNo()
        );
    }

    private void confirmFund(PayOrder payOrder, PayParticipantBranch branch) {
        fundAccountClient.confirmFrozenShareForPay(payOrder.getPayerUserId(), branch.getBranchId());
    }

    private void confirmInbound(PayOrder payOrder, PayParticipantBranch branch, Money amount) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        PayInboundOrderSnapshot confirmed = inboundClient.submitDeposit(new PayInboundSubmitRequest(
                normalizeRequired(payload.get("requestBizNo"), "requestBizNo"),
                payOrder.getBizOrderNo(),
                payOrder.getTradeOrderNo(),
                normalizeRequired(payload.get("payOrderNo"), "payOrderNo"),
                payOrder.getPayerUserId(),
                normalizeRequired(payload.get("payerAccountNo"), "payerAccountNo"),
                amount,
                normalizeRequired(payload.get("payChannelCode"), "payChannelCode"),
                normalizeRequired(payload.get("instChannelCode"), "instChannelCode"),
                normalizeRequired(payload.get("requestIdentify"), "requestIdentify"),
                defaultValue(payload.get("bizIdentity"), "OPENAIPAY")
        ));
        String inboundStatus = normalizeRequired(confirmed.inboundStatus(), "inboundStatus");
        if (STATUS_RECON_PENDING.equalsIgnoreCase(inboundStatus)) {
            throw new BranchReconPendingException("inbound pending reconcile: "
                    + compactError(defaultValue(confirmed.resultDescription(), confirmed.resultCode())));
        }
        if (!"SUCCEEDED".equals(inboundStatus)) {
            throw new IllegalStateException("inbound submit status is not SUCCEEDED: " + confirmed.inboundStatus());
        }
        payload.put("inboundId", confirmed.inboundId());
        if (confirmed.instId() != null) {
            payload.put("instId", confirmed.instId());
        }
        if (confirmed.instChannelCode() != null) {
            payload.put("instChannelCode", confirmed.instChannelCode());
        }
        if (confirmed.inboundOrderNo() != null) {
            payload.put("inboundOrderNo", confirmed.inboundOrderNo());
        }
        branch.updateRequestPayload(toPayload(payload), LocalDateTime.now());
        saveFundDetail(payOrder, PayParticipantType.INBOUND, branch, amount);
    }

    private void confirmOutbound(PayOrder payOrder, PayParticipantBranch branch, Money amount) {
        Map<String, String> payload = parsePayload(branch.getRequestPayload());
        Money outboundAmount = parsePayloadMoney(payload.get("outboundAmount"), amount);
        PayOutboundOrderSnapshot confirmed = outboundClient.submitWithdraw(new PayOutboundSubmitRequest(
                normalizeRequired(payload.get("requestBizNo"), "requestBizNo"),
                payOrder.getBizOrderNo(),
                payOrder.getTradeOrderNo(),
                normalizeRequired(payload.get("payOrderNo"), "payOrderNo"),
                payOrder.getPayerUserId(),
                normalizeRequired(payload.get("payeeAccountNo"), "payeeAccountNo"),
                outboundAmount,
                normalizeRequired(payload.get("payChannelCode"), "payChannelCode"),
                normalizeRequired(payload.get("instChannelCode"), "instChannelCode"),
                normalizeRequired(payload.get("requestIdentify"), "requestIdentify"),
                defaultValue(payload.get("bizIdentity"), "OPENAIPAY")
        ));
        String outboundStatus = normalizeRequired(confirmed.outboundStatus(), "outboundStatus");
        if (STATUS_RECON_PENDING.equalsIgnoreCase(outboundStatus)) {
            throw new BranchReconPendingException("outbound pending reconcile: "
                    + compactError(defaultValue(confirmed.resultDescription(), confirmed.resultCode())));
        }
        if (!"SUCCEEDED".equals(outboundStatus)) {
            throw new IllegalStateException("outbound submit status is not SUCCEEDED: " + confirmed.outboundStatus());
        }
        payload.put("outboundId", confirmed.outboundId());
        if (confirmed.instId() != null) {
            payload.put("instId", confirmed.instId());
        }
        if (confirmed.instChannelCode() != null) {
            payload.put("instChannelCode", confirmed.instChannelCode());
        }
        if (confirmed.outboundOrderNo() != null) {
            payload.put("outboundOrderNo", confirmed.outboundOrderNo());
        }
        branch.updateRequestPayload(toPayload(payload), LocalDateTime.now());
        saveFundDetail(payOrder, PayParticipantType.OUTBOUND, branch, amount);
    }

    private PayOrder mustGetOrder(String payOrderNo) {
        return payOrderRepository.findOrderByPayOrderNo(payOrderNo)
                .orElseThrow(() -> new NoSuchElementException("pay order not found: " + payOrderNo));
    }

    private SettlementPlanSnapshot loadSettlementPlan(PayOrder payOrder) {
        String payload = normalizeRequired(payOrder.getSettlementPlanSnapshot(), "settlementPlanSnapshot");
        return SettlementPlanSnapshot.fromPayload(payload);
    }

    private Money resolveDiscount(String couponNo) {
        return couponClient.resolveDiscountAmount(couponNo);
    }

    private String resolveFundCode(Long userId, String fundCodeFromCommand) {
        return fundAccountClient.resolveFundCode(userId, fundCodeFromCommand);
    }

    private void validateSubmitCommand(SubmitPayCommand command) {
        normalizeRequired(command.sourceBizType(), "sourceBizType");
        normalizeRequired(command.sourceBizNo(), "sourceBizNo");
        normalizeRequired(command.bizOrderNo(), "bizOrderNo");
        normalizeRequired(command.businessSceneCode(), "businessSceneCode");
        if (command.payerUserId() == null || command.payerUserId() <= 0) {
            throw new IllegalArgumentException("payerUserId must be greater than 0");
        }
        if (command.payeeUserId() != null && command.payeeUserId() <= 0) {
            throw new IllegalArgumentException("payeeUserId must be greater than 0");
        }
        if (command.settlementPlan() == null) {
            throw new IllegalArgumentException("settlementPlan must not be null");
        }
        if ("TRADE".equalsIgnoreCase(normalizeOptional(command.sourceBizType())) && command.sourceBizSnapshot() == null) {
            throw new IllegalArgumentException("sourceBizSnapshot must not be null for TRADE source");
        }
    }

    private String buildPayOrderNo(Long payerUserId) {
        return aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_PAY,
                aiPayBizTypeRegistry.payOrderCreateBizType(),
                String.valueOf(requirePositive(payerUserId, "payerUserId"))
        );
    }

    private String buildGlobalTxId(String payOrderNo) {
        return "pay-global:" + payOrderNo;
    }

    private String resolveTradeOrderNo(String tradeOrderNo, String sourceBizType, String sourceBizNo) {
        String normalizedTradeOrderNo = normalizeOptional(tradeOrderNo);
        if (normalizedTradeOrderNo != null) {
            return normalizedTradeOrderNo;
        }
        if ("TRADE".equalsIgnoreCase(normalizeOptional(sourceBizType))) {
            return normalizeOptional(sourceBizNo);
        }
        return null;
    }

    private void publishExecuteRequested(PayOrder payOrder) {
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.PAY_EXECUTE_REQUESTED,
                payOrder.getPayOrderNo(),
                new PayExecuteRequestedPayload(payOrder.getPayOrderNo()).toPayload(),
                20
        );
    }

    private void publishAccountingEventRequested(PayOrder payOrder) {
        if (payOrder.getStatus() != PayOrderStatus.COMMITTED) {
            return;
        }
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.PAY_ACCOUNTING_EVENT_REQUESTED,
                payOrder.getPayOrderNo() + ":COMMITTED",
                new PayAccountingEventRequestedPayload(payOrder.getPayOrderNo()).toPayload(),
                20
        );
    }

    private void publishPayResultChanged(PayOrder payOrder) {
        String messageKey = payOrder.getPayOrderNo() + ":" + payOrder.getStatusVersion();
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.PAY_RESULT_CHANGED,
                messageKey,
                new PayResultChangedPayload(
                        payOrder.getPayOrderNo(),
                        payOrder.getSourceBizType(),
                        payOrder.getSourceBizNo(),
                        payOrder.getStatus().name(),
                        payOrder.getStatusVersion(),
                        payOrder.getResultCode(),
                        payOrder.getResultMessage()
                ).toPayload(),
                20
        );
    }

    private void publishPostExecutionEvents(PayOrder payOrder) {
        publishAccountingEventRequested(payOrder);
        publishPayResultChanged(payOrder);
        publishPayReconRequested(payOrder);
    }

    private void publishPayReconRequested(PayOrder payOrder) {
        if (payOrder.getStatus() != PayOrderStatus.RECON_PENDING) {
            return;
        }
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.PAY_RECON_REQUESTED,
                payOrder.getPayOrderNo() + ":RECON",
                new PayReconRequestedPayload(payOrder.getPayOrderNo()).toPayload(),
                120
        );
    }

    private String buildInboundRequestBizNo(PayOrder payOrder) {
        return "PAY-" + payOrder.getPayOrderNo();
    }

    private String buildInboundRequestIdentify(PayOrder payOrder) {
        return buildInboundRequestBizNo(payOrder);
    }

    private String buildOutboundRequestBizNo(PayOrder payOrder) {
        return "PAY-WITHDRAW-" + payOrder.getPayOrderNo();
    }

    private String buildOutboundRequestIdentify(PayOrder payOrder) {
        return "PAY-WITHDRAW-" + payOrder.getBusinessSceneCode() + "-" + payOrder.getPayOrderNo();
    }

    private PayOrder reconcileRollbackPendingOrder(PayOrder payOrder, List<PayParticipantBranch> branches) {
        rollbackExecutedBranches(payOrder, branches, null);
        List<PayParticipantBranch> refreshedBranches = payOrderRepository.findParticipantBranches(payOrder.getPayOrderNo());
        if (hasRollbackPendingBranch(refreshedBranches)) {
            payOrder.markReconPending(
                    defaultValue(payOrder.getFailureReason(), "pay rollback reconcile pending"),
                    LocalDateTime.now()
            );
            return payOrderRepository.saveOrder(payOrder);
        }
        payOrder.markRolledBack(defaultValue(payOrder.getFailureReason(), "pay rollback reconciled"), LocalDateTime.now());
        return payOrderRepository.saveOrder(payOrder);
    }

    private boolean shouldReconcileByRollback(List<PayParticipantBranch> branches) {
        for (PayParticipantBranch branch : branches) {
            if (branch.getStatus() == PayParticipantStatus.CANCEL_OK
                    || branch.getStatus() == PayParticipantStatus.TRY_FAILED) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRollbackPendingBranch(List<PayParticipantBranch> branches) {
        for (PayParticipantBranch branch : branches) {
            if (branch.getStatus() != PayParticipantStatus.CANCEL_OK
                    && branch.getStatus() != PayParticipantStatus.SKIPPED) {
                return true;
            }
        }
        return false;
    }

    private boolean allBranchesCommitted(List<PayParticipantBranch> branches) {
        for (PayParticipantBranch branch : branches) {
            if (branch.getStatus() != PayParticipantStatus.CONFIRM_OK
                    && branch.getStatus() != PayParticipantStatus.SKIPPED) {
                return false;
            }
        }
        return true;
    }

    private Money normalizeOptionalOutboundAmount(Money value, CurrencyUnit currencyUnit) {
        if (value == null) {
            return null;
        }
        return normalizeNonNegative(value, "outboundAmount", currencyUnit);
    }

    private Money resolveOutboundAmount(Money requestedOutboundAmount, Money fallbackAmount) {
        if (requestedOutboundAmount == null || requestedOutboundAmount.isZero()) {
            return fallbackAmount;
        }
        return requestedOutboundAmount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money parsePayloadMoney(String amountText, Money fallbackAmount) {
        String normalizedAmountText = normalizeOptional(amountText);
        if (normalizedAmountText == null) {
            return fallbackAmount;
        }
        CurrencyUnit currencyUnit = fallbackAmount == null
                ? CurrencyUnit.of("CNY")
                : fallbackAmount.getCurrencyUnit();
        try {
            return Money.of(currencyUnit, new BigDecimal(normalizedAmountText)).rounded(2, RoundingMode.HALF_UP);
        } catch (RuntimeException ex) {
            return fallbackAmount;
        }
    }

    private String buildBranchId(PayOrder payOrder, PayParticipantType participantType) {
        String domain = switch (participantType) {
            case COUPON -> AiPayIdGenerator.DOMAIN_COUPON;
            case WALLET_ACCOUNT -> AiPayIdGenerator.DOMAIN_WALLET_ACCOUNT;
            case FUND_ACCOUNT -> AiPayIdGenerator.DOMAIN_FUND_ACCOUNT;
            case CREDIT_ACCOUNT -> AiPayIdGenerator.DOMAIN_CREDIT_ACCOUNT;
            case INBOUND -> AiPayIdGenerator.DOMAIN_INBOUND;
            case OUTBOUND -> AiPayIdGenerator.DOMAIN_OUTBOUND;
        };
        return aiPayIdGenerator.generate(
                domain,
                aiPayBizTypeRegistry.payBranchBizType(participantType),
                String.valueOf(requirePositive(payOrder.getPayerUserId(), "payerUserId"))
        );
    }

    private String resolveParticipantResourceId(PayOrder payOrder,
                                                PayParticipantType participantType,
                                                String fundCode,
                                                PayCreditRouteSnapshot creditRoute) {
        return switch (participantType) {
            case COUPON -> payOrder.getCouponNo();
            case WALLET_ACCOUNT -> String.valueOf(payOrder.getPayerUserId());
            case FUND_ACCOUNT -> resolveFundCode(payOrder.getPayerUserId(), fundCode);
            case CREDIT_ACCOUNT -> normalizeRequired(requireCreditRoute(creditRoute).accountNo(), "accountNo");
            case INBOUND -> payOrder.getPayOrderNo();
            case OUTBOUND -> payOrder.getPayOrderNo();
        };
    }

    private String buildParticipantPayload(PayOrder payOrder,
                                           PayParticipantType participantType,
                                           String fundCode,
                                           Money amount,
                                           String paymentToolCode,
                                           Money outboundAmount,
                                           PayCreditRouteSnapshot creditRoute) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("participant", participantType.name());
        payload.put("amount", amount.rounded(2, RoundingMode.HALF_UP).getAmount().toPlainString());
        payload.put("currency", amount.getCurrencyUnit().getCode());
        if (participantType == PayParticipantType.FUND_ACCOUNT) {
            payload.put("fundCode", resolveFundCode(payOrder.getPayerUserId(), fundCode));
        }
        if (participantType == PayParticipantType.CREDIT_ACCOUNT) {
            payload.put("assetCategory", normalizeRequired(requireCreditRoute(creditRoute).assetCategory(), "assetCategory"));
            payload.put("operationType", normalizeRequired(requireCreditRoute(creditRoute).operationType(), "operationType"));
        }
        if (participantType == PayParticipantType.WALLET_ACCOUNT) {
            payload.put("operationType", "DEBIT");
        }
        if (participantType == PayParticipantType.INBOUND) {
            payload.put("operationType", "DEPOSIT");
            payload.put("payerAccountNo", String.valueOf(payOrder.getPayerUserId()));
            if (normalizeOptional(paymentToolCode) != null) {
                payload.put("paymentToolCode", normalizeOptional(paymentToolCode));
            }
        }
        if (participantType == PayParticipantType.OUTBOUND) {
            payload.put("operationType", "WITHDRAW");
            payload.put("payeeAccountNo", String.valueOf(payOrder.getPayerUserId()));
            Money effectiveOutboundAmount = resolveOutboundAmount(outboundAmount, amount);
            payload.put("outboundAmount", effectiveOutboundAmount.getAmount().toPlainString());
            if (normalizeOptional(paymentToolCode) != null) {
                payload.put("paymentToolCode", normalizeOptional(paymentToolCode));
            }
        }
        return toPayload(payload);
    }

    private Money amountForParticipant(PayOrder payOrder, PayParticipantType participantType) {
        PaySplitPlan splitPlan = payOrder.getSplitPlan();
        if (participantType == PayParticipantType.CREDIT_ACCOUNT && shouldApplyCreditRepayBranch(payOrder)) {
            return payOrder.getPayableAmount();
        }
        return switch (participantType) {
            case COUPON -> payOrder.getDiscountAmount();
            case WALLET_ACCOUNT -> splitPlan.getWalletDebitAmount();
            case FUND_ACCOUNT -> splitPlan.getFundDebitAmount();
            case CREDIT_ACCOUNT -> splitPlan.getCreditDebitAmount();
            case INBOUND -> splitPlan.getInboundDebitAmount();
            case OUTBOUND -> isWithdrawScene(payOrder.getBusinessSceneCode())
                    ? splitPlan.getWalletDebitAmount()
                    : Money.zero(payOrder.getPayableAmount().getCurrencyUnit());
        };
    }

    private void validateNonNegativeParticipantDebits(PayOrder payOrder) {
        for (PayParticipantType participantType : PayParticipantType.values()) {
            Money amount = amountForParticipant(payOrder, participantType);
            if (amount == null) {
                continue;
            }
            if (amount.compareTo(Money.zero(amount.getCurrencyUnit())) < 0) {
                throw new IllegalStateException(participantType.name() + " debit amount must not be less than 0");
            }
        }
    }

    private List<PayParticipantBranch> sortForCommit(List<PayParticipantBranch> branches) {
        Map<PayParticipantType, Integer> order = Map.of(
                PayParticipantType.COUPON, 1,
                PayParticipantType.INBOUND, 2,
                PayParticipantType.OUTBOUND, 3,
                PayParticipantType.WALLET_ACCOUNT, 4,
                PayParticipantType.FUND_ACCOUNT, 5,
                PayParticipantType.CREDIT_ACCOUNT, 6
        );
        return branches.stream()
                .sorted(Comparator.comparingInt(branch -> order.getOrDefault(branch.getParticipantType(), 99)))
                .toList();
    }

    private List<PayParticipantBranch> sortForRollback(List<PayParticipantBranch> branches) {
        Map<PayParticipantType, Integer> order = Map.of(
                PayParticipantType.COUPON, 5,
                PayParticipantType.WALLET_ACCOUNT, 4,
                PayParticipantType.FUND_ACCOUNT, 3,
                PayParticipantType.CREDIT_ACCOUNT, 2,
                PayParticipantType.INBOUND, 1,
                PayParticipantType.OUTBOUND, 1
        );
        return branches.stream()
                .sorted(Comparator.comparingInt(branch -> order.getOrDefault(branch.getParticipantType(), 99)))
                .toList();
    }

    private PayOrderDTO toPayOrderDTO(PayOrder payOrder, boolean includeDetails) {
        List<PayParticipantBranchDTO> participants = List.of();
        List<PayFundDetailSummaryDTO> fundDetails = List.of();
        if (includeDetails) {
            participants = toPayParticipantBranchDTOs(payOrderRepository.findParticipantBranches(payOrder.getPayOrderNo()));
            fundDetails = payOrderRepository.findFundDetails(payOrder.getPayOrderNo())
                    .stream()
                    .map(this::toPayFundDetailDTO)
                    .toList();
        }
        return toPayOrderDTOWithParticipantDTOs(payOrder, participants, fundDetails);
    }

    private PaySubmitReceiptDTO toPaySubmitReceiptDTO(PayOrder payOrder) {
        return new PaySubmitReceiptDTO(
                payOrder.getPayOrderNo(),
                payOrder.getTradeOrderNo(),
                payOrder.getBizOrderNo(),
                payOrder.getSourceBizType(),
                payOrder.getSourceBizNo(),
                payOrder.getAttemptNo(),
                payOrder.getStatus().name(),
                payOrder.getStatusVersion(),
                payOrder.getResultCode(),
                payOrder.getResultMessage(),
                payOrder.getCreatedAt(),
                payOrder.getUpdatedAt()
        );
    }

    private PayOrderDTO toPayOrderDTOWithParticipantBranches(PayOrder payOrder,
                                                             List<PayParticipantBranch> participantBranches,
                                                             List<PayFundDetailSummaryDTO> fundDetails) {
        return toPayOrderDTOWithParticipantDTOs(payOrder, toPayParticipantBranchDTOs(participantBranches), fundDetails);
    }

    private PayOrderDTO toPayOrderDTOWithParticipantDTOs(PayOrder payOrder,
                                                         List<PayParticipantBranchDTO> participants,
                                                         List<PayFundDetailSummaryDTO> fundDetails) {
        return new PayOrderDTO(
                payOrder.getPayOrderNo(),
                payOrder.getTradeOrderNo(),
                payOrder.getBizOrderNo(),
                payOrder.getSourceBizType(),
                payOrder.getSourceBizNo(),
                payOrder.getAttemptNo(),
                payOrder.getBusinessSceneCode(),
                payOrder.getPayerUserId(),
                payOrder.getPayeeUserId(),
                payOrder.getOriginalAmount(),
                payOrder.getDiscountAmount(),
                payOrder.getPayableAmount(),
                payOrder.getActualPaidAmount(),
                toPaySplitPlanDTO(payOrder.getSplitPlan()),
                payOrder.getCouponNo(),
                payOrder.getGlobalTxId(),
                payOrder.getStatus().name(),
                payOrder.getStatusVersion(),
                payOrder.getResultCode(),
                payOrder.getResultMessage(),
                payOrder.getFailureReason(),
                payOrder.getCreatedAt(),
                payOrder.getUpdatedAt(),
                participants,
                fundDetails
        );
    }

    private PaySplitPlanDTO toPaySplitPlanDTO(PaySplitPlan splitPlan) {
        return new PaySplitPlanDTO(
                splitPlan.getWalletDebitAmount(),
                splitPlan.getFundDebitAmount(),
                splitPlan.getCreditDebitAmount(),
                splitPlan.getInboundDebitAmount()
        );
    }

    private List<PayParticipantBranchDTO> toPayParticipantBranchDTOs(List<PayParticipantBranch> branches) {
        if (branches == null || branches.isEmpty()) {
            return List.of();
        }
        return branches.stream()
                .map(this::toPayParticipantBranchDTO)
                .toList();
    }

    private PayParticipantBranchDTO toPayParticipantBranchDTO(PayParticipantBranch branch) {
        return new PayParticipantBranchDTO(
                branch.getPayOrderNo(),
                branch.getParticipantType().name(),
                branch.getBranchId(),
                branch.getParticipantResourceId(),
                branch.getRequestPayload(),
                branch.getStatus().name(),
                branch.getResponseMessage(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }

    private PayFundDetailSummaryDTO toPayFundDetailDTO(PayFundDetailSummary fundDetail) {
        if (fundDetail instanceof PayBankCardFundDetail bankDetail) {
            return new PayFundDetailSummaryDTO(
                    fundDetail.getPayOrderNo(),
                    fundDetail.getPayTool().name(),
                    fundDetail.getDetailOwner().name(),
                    fundDetail.getAmount(),
                    fundDetail.getCumulativeRefundAmount(),
                    bankDetail.getChannel(),
                    bankDetail.getBankOrderNo(),
                    bankDetail.getBankCardNo(),
                    bankDetail.getChannelFeeAmount(),
                    bankDetail.getDepositOrderNo(),
                    bankDetail.getInstId(),
                    bankDetail.getInstChannelCode(),
                    bankDetail.getPayChannelCode(),
                    bankDetail.getBankCode(),
                    bankDetail.getBankName(),
                    bankDetail.getCardType(),
                    bankDetail.getCardHolderName(),
                    bankDetail.getCardTailNo(),
                    bankDetail.getToolSnapshot(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    fundDetail.getCreatedAt(),
                    fundDetail.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayRedPacketFundDetail redPacketDetail) {
            return new PayFundDetailSummaryDTO(
                    fundDetail.getPayOrderNo(),
                    fundDetail.getPayTool().name(),
                    fundDetail.getDetailOwner().name(),
                    fundDetail.getAmount(),
                    fundDetail.getCumulativeRefundAmount(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    redPacketDetail.getRedPacketId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    fundDetail.getCreatedAt(),
                    fundDetail.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayWalletFundDetail walletDetail) {
            return new PayFundDetailSummaryDTO(
                    fundDetail.getPayOrderNo(),
                    fundDetail.getPayTool().name(),
                    fundDetail.getDetailOwner().name(),
                    fundDetail.getAmount(),
                    fundDetail.getCumulativeRefundAmount(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    walletDetail.getAccountNo(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    fundDetail.getCreatedAt(),
                    fundDetail.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayFundAccountFundDetail fundAccountDetail) {
            return new PayFundDetailSummaryDTO(
                    fundDetail.getPayOrderNo(),
                    fundDetail.getPayTool().name(),
                    fundDetail.getDetailOwner().name(),
                    fundDetail.getAmount(),
                    fundDetail.getCumulativeRefundAmount(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    fundAccountDetail.getFundCode(),
                    fundAccountDetail.getFundProductCode(),
                    fundAccountDetail.getAccountIdentity(),
                    null,
                    null,
                    null,
                    fundDetail.getCreatedAt(),
                    fundDetail.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayCreditAccountFundDetail creditDetail) {
            return new PayFundDetailSummaryDTO(
                    fundDetail.getPayOrderNo(),
                    fundDetail.getPayTool().name(),
                    fundDetail.getDetailOwner().name(),
                    fundDetail.getAmount(),
                    fundDetail.getCumulativeRefundAmount(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    creditDetail.getAccountNo(),
                    creditDetail.getCreditAccountType().name(),
                    creditDetail.getCreditProductCode(),
                    fundDetail.getCreatedAt(),
                    fundDetail.getUpdatedAt()
            );
        }
        throw new IllegalStateException("unsupported pay fund detail type: " + fundDetail.getClass().getName());
    }

    private Map<String, String> parsePayload(String payloadRaw) {
        Map<String, String> payload = new LinkedHashMap<>();
        String normalized = normalizeOptional(payloadRaw);
        if (normalized == null) {
            return payload;
        }
        String[] parts = normalized.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && !kv[0].isBlank()) {
                payload.put(kv[0].trim(), kv[1].trim());
            }
        }
        return payload;
    }

    private String toPayload(Map<String, String> payload) {
        if (payload.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String key = normalizeOptional(entry.getKey());
            String value = normalizeOptional(entry.getValue());
            if (key == null || value == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(key).append('=').append(value);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String firstNonBlank(String preferred, String fallback) {
        String preferredValue = normalizeOptional(preferred);
        if (preferredValue != null) {
            return preferredValue;
        }
        return normalizeOptional(fallback);
    }

    private String resolveFundProductCode(String fundCode) {
        return normalizeRequired(fundCode, "fundCode").toUpperCase(Locale.ROOT);
    }

    private String buildFundAccountIdentity(Long payerUserId, String fundCode) {
        return requirePositive(payerUserId, "payerUserId") + ":" + normalizeRequired(fundCode, "fundCode");
    }

    private String resolveCreditProductCode(CreditAccountType accountType) {
        return accountType == CreditAccountType.LOAN_ACCOUNT
                ? CreditProductCodes.AILOAN
                : CreditProductCodes.AICREDIT;
    }

    private PayCreditRouteSnapshot resolveCreditRoute(PayOrder payOrder, String paymentMethod) {
        PayCreditRouteSnapshot payRoute = payRouteClient.routeCreditForPay(
                payOrder.getBusinessSceneCode(),
                paymentMethod,
                payOrder.getPayerUserId(),
                payOrder.getPayeeUserId()
        );
        return requireCreditRoute(payRoute);
    }

    private String parseInboundId(PayParticipantBranch branch) {
        String inboundId = parsePayload(branch.getRequestPayload()).get("inboundId");
        return normalizeRequired(inboundId, "inboundId");
    }

    private String parseOutboundId(PayParticipantBranch branch) {
        String outboundId = parsePayload(branch.getRequestPayload()).get("outboundId");
        return normalizeRequired(outboundId, "outboundId");
    }

    private boolean isWithdrawScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.equals("WITHDRAW") || upper.contains("WITHDRAW");
    }

    private boolean isTransferScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.equals("TRANSFER") || upper.contains("TRANSFER");
    }

    private boolean shouldApplyCreditRepayBranch(PayOrder payOrder) {
        return isCreditRepayScene(payOrder.getBusinessSceneCode())
                && payOrder.getSplitPlan().getCreditDebitAmount().isZero()
                && payOrder.getPayableAmount().isGreaterThan(zeroOf(payOrder.getPayableAmount()));
    }

    private void confirmCreditBranch(String xid, PayParticipantBranch branch) {
        String accountNo = normalizeRequired(branch.getParticipantResourceId(), "participantResourceId");
        payRouteClient.tccConfirmCredit(xid, branch.getBranchId(), accountNo);
    }

    private boolean isCreditRepayScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.equals("APP_CREDIT_REPAY")
                || (upper.contains("CREDIT") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AICREDIT) && upper.contains("REPAY"))
                || (upper.contains("AICREDIT") && upper.contains("REPAY"))
                || (upper.contains("LOAN") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AILOAN) && upper.contains("REPAY"))
                || (upper.contains("AILOAN") && upper.contains("REPAY"));
    }

    private PayCreditRouteSnapshot requireCreditRoute(PayCreditRouteSnapshot payRoute) {
        if (payRoute == null) {
            throw new IllegalStateException("credit pay route must not be null");
        }
        return payRoute;
    }

    private PayBankCardSnapshot resolveSelectedChannelBankCard(Long payerUserId, String paymentToolCode) {
        List<PayBankCardSnapshot> activeCards = bankCardClient.listUserActiveBankCards(requirePositive(payerUserId, "payerUserId"));
        if (activeCards.isEmpty()) {
            throw new IllegalStateException("no active bank card for payerUserId: " + payerUserId);
        }

        String normalizedToolCode = normalizeCardNo(paymentToolCode);
        if (normalizedToolCode != null) {
            for (PayBankCardSnapshot card : activeCards) {
                if (normalizedToolCode.equals(normalizeCardNo(card.cardNo()))) {
                    return card;
                }
            }
            throw new IllegalArgumentException("paymentToolCode is not an active bank card for payerUserId: " + payerUserId);
        }

        for (PayBankCardSnapshot card : activeCards) {
            if (card.defaultCard()) {
                return card;
            }
        }
        return activeCards.get(0);
    }

    private String normalizeCardNo(String cardNo) {
        String normalized = normalizeOptional(cardNo);
        if (normalized == null) {
            return null;
        }
        return normalized.replace(" ", "");
    }

    private BankChannelCodes resolveBankChannelCodes(String rawBankCode, boolean withdraw) {
        String instId = normalizeBankInstId(rawBankCode);
        String channelSuffix = withdraw ? BANK_WITHDRAW_CHANNEL_SUFFIX : BANK_DEPOSIT_CHANNEL_SUFFIX;
        String instChannelCode = resolveBankChannelCode(instId, channelSuffix);
        return new BankChannelCodes(instId, instChannelCode, instChannelCode);
    }

    private String normalizeBankInstId(String rawBankCode) {
        String normalized = normalizeRequired(rawBankCode, "bankCode").toUpperCase(Locale.ROOT);
        String compact = normalized.replaceAll("[^A-Z0-9]", "");
        if (compact.isBlank()) {
            throw new IllegalArgumentException("bankCode must not be blank");
        }
        int firstDigitIndex = -1;
        for (int i = 0; i < compact.length(); i++) {
            if (Character.isDigit(compact.charAt(i))) {
                firstDigitIndex = i;
                break;
            }
        }
        String instId = firstDigitIndex <= 0 ? compact : compact.substring(0, firstDigitIndex);
        if (instId.isBlank()) {
            throw new IllegalArgumentException("bankCode must contain institution code");
        }
        return instId;
    }

    private String resolveBankChannelCode(String instId, String channelSuffix) {
        return normalizeRequired(instId, "instId").toUpperCase(Locale.ROOT)
                + normalizeRequired(channelSuffix, "channelSuffix");
    }

    private String buildBankToolSnapshot(String instId,
                                         String instChannelCode,
                                         String payChannelCode,
                                         String bankCode,
                                         String bankName,
                                         String cardType,
                                         String cardHolderName,
                                         String cardTailNo,
                                         String cardNo) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("instId", normalizeOptional(instId));
        payload.put("instChannelCode", normalizeOptional(instChannelCode));
        payload.put("payChannelCode", normalizeOptional(payChannelCode));
        payload.put("bankCode", normalizeOptional(bankCode));
        payload.put("bankName", normalizeOptional(bankName));
        payload.put("cardType", normalizeOptional(cardType));
        payload.put("cardHolderName", normalizeOptional(cardHolderName));
        payload.put("cardTailNo", normalizeOptional(cardTailNo));
        payload.put("cardNo", normalizeOptional(cardNo));
        return toPayload(payload);
    }

    private String resolveCardTailNo(String cardNo) {
        String normalized = normalizeCardNo(cardNo);
        if (normalized == null) {
            return null;
        }
        String digitsOnly = normalized.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return null;
        }
        if (digitsOnly.length() <= 4) {
            return digitsOnly;
        }
        return digitsOnly.substring(digitsOnly.length() - 4);
    }

    private Money firstPositiveMoney(Money... candidates) {
        CurrencyUnit currency = CurrencyUnit.of(DEFAULT_CURRENCY);
        if (candidates == null || candidates.length == 0) {
            return zeroMoney(currency).rounded(2, RoundingMode.HALF_UP);
        }
        for (Money candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            currency = candidate.getCurrencyUnit();
            if (candidate.compareTo(zeroOf(candidate)) > 0) {
                return candidate.rounded(2, RoundingMode.HALF_UP);
            }
        }
        return zeroMoney(currency).rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeAmount(Money value, String fieldName) {
        if (value == null || value.isLessThanOrEqual(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String fieldName, CurrencyUnit currencyUnit) {
        if (value == null) {
            return zeroMoney(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!value.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(fieldName + " currency must equal originalAmount currency");
        }
        if (value.isLessThan(zeroOf(value))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroMoney(CurrencyUnit currencyUnit) {
        return Money.zero(currencyUnit == null ? CurrencyUnit.of("CNY") : currencyUnit);
    }

    private Money zeroOf(Money value) {
        return Money.zero(value.getCurrencyUnit());
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

    private String defaultValue(String raw, String fallback) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? fallback : normalized;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String compactError(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return "unknown error";
        }
        int maxLength = 180;
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private record BankChannelCodes(
            /** 机构ID */
            String instId,
            /** 机构渠道编码 */
            String instChannelCode,
            /** 支付渠道编码 */
            String payChannelCode
    ) {
    }

    private static final class BranchReconPendingException extends RuntimeException {
        private BranchReconPendingException(String message) {
            super(message);
        }
    }

}
