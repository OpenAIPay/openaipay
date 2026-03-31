package cn.openaipay.application.trade.service.impl;

import cn.openaipay.application.adminrisk.facade.AdminRiskManageFacade;
import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.application.pay.async.PayResultChangedPayload;
import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.facade.PricingFacade;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.command.CreateDepositTradeCommand;
import cn.openaipay.application.trade.command.CreateRefundTradeCommand;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.command.CreateWithdrawTradeCommand;
import cn.openaipay.application.trade.dto.TradeFlowStepDTO;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.dto.TradePayAttemptDTO;
import cn.openaipay.application.trade.dto.TradePayParticipantDTO;
import cn.openaipay.application.trade.dto.TradeSplitPlanDTO;
import cn.openaipay.application.trade.dto.TradeWalletFlowDTO;
import cn.openaipay.application.trade.service.TradeService;
import cn.openaipay.application.trade.support.OperatorMerchantRoutingService;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.facade.UserFacade;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.trade.client.PayClient;
import cn.openaipay.domain.trade.client.TradeCreditRouteSnapshot;
import cn.openaipay.domain.trade.client.TradePayOrderSnapshot;
import cn.openaipay.domain.trade.client.TradePayFundDetailSnapshot;
import cn.openaipay.domain.trade.client.TradePayParticipantSnapshot;
import cn.openaipay.domain.trade.client.TradePayRouteClient;
import cn.openaipay.domain.trade.client.TradePaySubmitRequest;
import cn.openaipay.domain.trade.client.TradePaySubmitResult;
import cn.openaipay.domain.trade.client.TradeSettleClient;
import cn.openaipay.domain.trade.client.TradeSettleRequest;
import cn.openaipay.domain.trade.client.TradeSettleResult;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeBusinessIndex;
import cn.openaipay.domain.trade.model.TradeCreditOrder;
import cn.openaipay.domain.trade.model.TradeCreditProductType;
import cn.openaipay.domain.trade.model.TradeCreditTradeType;
import cn.openaipay.domain.trade.model.TradeFlowStep;
import cn.openaipay.domain.trade.model.TradeFlowStepCode;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.model.TradeType;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.trade.service.TradePayResultDecision;
import cn.openaipay.domain.trade.service.TradePayResultDomainService;
import cn.openaipay.domain.trade.service.TradePayResultHandling;
import cn.openaipay.domain.trade.service.TradeRefundDomainService;
import cn.openaipay.domain.trade.service.TradeRefundPreparation;
import cn.openaipay.domain.trade.service.TradeSplitDomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 交易应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class TradeServiceImpl implements TradeService {
    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);
    /** JSON序列化器 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 支付工具快照版本 */
    private static final String PAYMENT_TOOL_SNAPSHOT_VERSION = "1.0";
    /** 默认币种编码。 */
    private static final String DEFAULT_CURRENCY = "CNY";
    /** 默认充值场景编码。 */
    private static final String DEFAULT_DEPOSIT_SCENE = "TRADE_DEPOSIT";
    /** 默认提现场景编码。 */
    private static final String DEFAULT_WITHDRAW_SCENE = "TRADE_WITHDRAW";
    /** 默认支付场景编码。 */
    private static final String DEFAULT_PAY_SCENE = "TRADE_PAY";
    /** 手机营业厅充值场景编码。 */
    private static final String MOBILE_HALL_TOP_UP_SCENE = "APP_MOBILE_HALL_TOP_UP";
    /** 默认转账场景编码。 */
    private static final String DEFAULT_TRANSFER_SCENE = "TRADE_TRANSFER";
    /** 默认退款场景编码。 */
    private static final String DEFAULT_REFUND_SCENE = "TRADE_REFUND";
    /** 默认查询的余额账变条数 */
    private static final int DEFAULT_RECENT_WALLET_FLOW_LIMIT = 3;
    /** 单次查询允许的最大余额账变条数 */
    private static final int MAX_RECENT_WALLET_FLOW_LIMIT = 20;
    /** 账变筛选时的放大扫描倍率，避免过滤后条数不足 */
    private static final int RECENT_WALLET_FLOW_SCAN_MULTIPLIER = 4;
    /** 账变筛选时的扫描上限，防止一次扫描过多历史交易 */
    private static final int MAX_RECENT_WALLET_FLOW_SCAN_LIMIT = 80;
    /** 聊天红包发送场景编码。 */
    private static final String RED_PACKET_SEND_SCENE = "CHAT_RED_PACKET_SEND";
    /** 聊天红包领取场景编码。 */
    private static final String RED_PACKET_CLAIM_SCENE = "CHAT_RED_PACKET_CLAIM";
    /** 银行名称提取正则。 */
    private static final Pattern BANK_NAME_PATTERN = Pattern.compile("([\\p{IsHan}A-Za-z0-9]{2,20}银行)");
    /** Trade 作为支付来源业务类型。 */
    private static final String PAY_SOURCE_BIZ_TYPE_TRADE = "TRADE";
    /** 交易仓储。 */
    private final TradeRepository tradeRepository;
    /** 红包订单仓储。 */
    private final RedPacketOrderRepository redPacketOrderRepository;
    /** 交易退款领域服务。 */
    private final TradeRefundDomainService tradeRefundDomainService;
    /** 交易支付拆分领域服务。 */
    private final TradeSplitDomainService tradeSplitDomainService;
    /** 交易支付结果决策领域服务。 */
    private final TradePayResultDomainService tradePayResultDomainService;
    /** 定价门面。 */
    private final PricingFacade pricingFacade;
    /** 支付客户端。 */
    private final PayClient payClient;
    /** 支付产品路由客户端。 */
    private final TradePayRouteClient tradePayRouteClient;
    /** 结算客户端。 */
    private final TradeSettleClient tradeSettleClient;
    /** 优惠券门面。 */
    private final CouponFacade couponFacade;
    /** AiPay ID 生成器。 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AiPay 业务类型码注册表。 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;
    /** 用户门面。 */
    private final UserFacade userFacade;
    /** 成功步骤是否持久化流程明细 */
    private final boolean persistSuccessFlowSteps;
    /** 运营商商户路由服务 */
    private final OperatorMerchantRoutingService operatorMerchantRoutingService;
    /** 风控规则门面。 */
    private final AdminRiskManageFacade adminRiskManageFacade;

    public TradeServiceImpl(TradeRepository tradeRepository,
                                       RedPacketOrderRepository redPacketOrderRepository,
                                       TradeRefundDomainService tradeRefundDomainService,
                                       TradeSplitDomainService tradeSplitDomainService,
                                       TradePayResultDomainService tradePayResultDomainService,
                                       PricingFacade pricingFacade,
                                       PayClient payClient,
                                       TradePayRouteClient tradePayRouteClient,
                                       TradeSettleClient tradeSettleClient,
                                       CouponFacade couponFacade,
                                       UserFacade userFacade,
                                       AiPayIdGenerator aiPayIdGenerator,
                                       AiPayBizTypeRegistry aiPayBizTypeRegistry,
                                       AdminRiskManageFacade adminRiskManageFacade,
                                       OperatorMerchantRoutingService operatorMerchantRoutingService,
                                       @Value("${aipay.trade.persist-success-flow-steps:false}") boolean persistSuccessFlowSteps) {
        this.tradeRepository = tradeRepository;
        this.redPacketOrderRepository = redPacketOrderRepository;
        this.tradeRefundDomainService = tradeRefundDomainService;
        this.tradeSplitDomainService = tradeSplitDomainService;
        this.tradePayResultDomainService = tradePayResultDomainService;
        this.pricingFacade = pricingFacade;
        this.payClient = payClient;
        this.tradePayRouteClient = tradePayRouteClient;
        this.tradeSettleClient = tradeSettleClient;
        this.couponFacade = couponFacade;
        this.userFacade = userFacade;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
        this.adminRiskManageFacade = adminRiskManageFacade;
        this.operatorMerchantRoutingService = operatorMerchantRoutingService;
        this.persistSuccessFlowSteps = persistSuccessFlowSteps;
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public TradeOrderDTO deposit(CreateDepositTradeCommand command) {
        TradeExecutionRequest request = new TradeExecutionRequest(
                TradeType.DEPOSIT,
                command.requestNo(),
                defaultScene(command.businessSceneCode(), DEFAULT_DEPOSIT_SCENE),
                command.payerUserId(),
                command.payeeUserId() == null ? command.payerUserId() : command.payeeUserId(),
                defaultPaymentMethod(command.paymentMethod(), TradeType.DEPOSIT),
                command.amount(),
                null,
                command.walletDebitAmount(),
                command.fundDebitAmount(),
                command.creditDebitAmount(),
                command.inboundDebitAmount(),
                null,
                mergePaymentToolMetadata(command.metadata(), command.paymentToolCode())
        );
        return executeTrade(request);
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public TradeOrderDTO withdraw(CreateWithdrawTradeCommand command) {
        TradeExecutionRequest request = new TradeExecutionRequest(
                TradeType.WITHDRAW,
                command.requestNo(),
                defaultScene(command.businessSceneCode(), DEFAULT_WITHDRAW_SCENE),
                command.payerUserId(),
                command.payeeUserId(),
                defaultPaymentMethod(command.paymentMethod(), TradeType.WITHDRAW),
                command.amount(),
                null,
                null,
                null,
                null,
                null,
                null,
                mergePaymentToolMetadata(command.metadata(), command.paymentToolCode())
        );
        return executeTrade(request);
    }

    /**
     * 处理支付信息。
     */
    @Override
    @Transactional
    public TradeOrderDTO pay(CreatePayTradeCommand command) {
        String businessSceneCode = defaultScene(command.businessSceneCode(), DEFAULT_PAY_SCENE);
        String metadata = mergePaymentToolMetadata(command.metadata(), command.paymentToolCode());
        validateLoanRepayPaymentMethodIfNeeded(businessSceneCode, command.paymentMethod(), metadata);
        Long payeeUserId = operatorMerchantRoutingService.resolvePayeeUserId(
                businessSceneCode,
                requirePositive(command.payeeUserId(), "payeeUserId"),
                metadata
        );
        Money walletDebitAmount = command.walletDebitAmount();
        Money fundDebitAmount = command.fundDebitAmount();
        Money creditDebitAmount = command.creditDebitAmount();
        Money inboundDebitAmount = command.inboundDebitAmount();
        if (shouldIgnoreClientSplitForScene(businessSceneCode)) {
            // 手机话费充值链路以后端计费 payable 为准，客户端拆分仅用于展示，不参与服务端校验。
            log.info(
                    "[{}]入参：{}",
                    logScene(businessSceneCode, "支付"),
                    "requestNo=" + command.requestNo()
                            + ", payerUserId=" + command.payerUserId()
                            + ", payeeUserId=" + payeeUserId
                            + ", paymentMethod=" + command.paymentMethod()
                            + ", amount=" + moneyLogText(command.amount())
                            + ", couponNo=" + normalizeOptional(command.couponNo())
                            + ", walletDebit=" + moneyLogText(command.walletDebitAmount())
                            + ", fundDebit=" + moneyLogText(command.fundDebitAmount())
                            + ", creditDebit=" + moneyLogText(command.creditDebitAmount())
                            + ", inboundDebit=" + moneyLogText(command.inboundDebitAmount())
            );
            walletDebitAmount = null;
            fundDebitAmount = null;
            creditDebitAmount = null;
            inboundDebitAmount = null;
        }
        TradeExecutionRequest request = new TradeExecutionRequest(
                TradeType.PAY,
                command.requestNo(),
                businessSceneCode,
                command.payerUserId(),
                payeeUserId,
                defaultPaymentMethod(command.paymentMethod(), TradeType.PAY),
                command.amount(),
                null,
                walletDebitAmount,
                fundDebitAmount,
                creditDebitAmount,
                inboundDebitAmount,
                command.couponNo(),
                metadata
        );
        return executeTrade(request);
    }

    private boolean shouldIgnoreClientSplitForScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        return normalized != null && MOBILE_HALL_TOP_UP_SCENE.equalsIgnoreCase(normalized);
    }

    private String moneyLogText(Money money) {
        if (money == null) {
            return "null";
        }
        return money.getCurrencyUnit().getCode() + " " + money.getAmount().toPlainString();
    }

    /**
     * 处理转账信息。
     */
    @Override
    @Transactional
    public TradeOrderDTO transfer(CreateTransferTradeCommand command) {
        Long payerUserId = requirePositive(command.payerUserId(), "payerUserId");
        Long payeeUserId = requirePositive(command.payeeUserId(), "payeeUserId");
        if (payerUserId.equals(payeeUserId)) {
            throw new IllegalArgumentException("payeeUserId must not equal payerUserId for transfer");
        }

        TradeExecutionRequest request = new TradeExecutionRequest(
                TradeType.TRANSFER,
                command.requestNo(),
                defaultScene(command.businessSceneCode(), DEFAULT_TRANSFER_SCENE),
                payerUserId,
                payeeUserId,
                defaultPaymentMethod(command.paymentMethod(), TradeType.TRANSFER),
                command.amount(),
                null,
                command.walletDebitAmount(),
                command.fundDebitAmount(),
                command.creditDebitAmount(),
                command.inboundDebitAmount(),
                null,
                mergePaymentToolMetadata(command.metadata(), command.paymentToolCode())
        );
        return executeTrade(request);
    }

    /**
     * 处理退款信息。
     */
    @Override
    @Transactional
    public TradeOrderDTO refund(CreateRefundTradeCommand command) {
        String originalTradeOrderNo = normalizeRequired(command.originalTradeOrderNo(), "originalTradeOrderNo");
        TradeOrder originalTrade = tradeRepository.findTradeOrderByTradeOrderNo(originalTradeOrderNo)
                .orElseThrow(() -> new NoSuchElementException("original trade not found: " + originalTradeOrderNo));
        Money amount = normalizeAmount(command.amount(), "amount");
        Money refundedAmount = defaultZero(tradeRepository.sumSucceededRefundAmount(originalTradeOrderNo));
        refundedAmount = convertCurrency(refundedAmount, originalTrade.getPayableAmount().getCurrencyUnit());
        TradeRefundPreparation refundPreparation = tradeRefundDomainService.prepareRefund(
                originalTrade,
                amount,
                refundedAmount,
                command.payerUserId(),
                command.payeeUserId()
        );

        TradeExecutionRequest request = new TradeExecutionRequest(
                TradeType.REFUND,
                command.requestNo(),
                defaultScene(command.businessSceneCode(), DEFAULT_REFUND_SCENE),
                refundPreparation.payerUserId(),
                refundPreparation.payeeUserId(),
                command.paymentMethod() == null ? originalTrade.getPaymentMethod() : command.paymentMethod(),
                refundPreparation.amount(),
                originalTradeOrderNo,
                command.walletDebitAmount(),
                command.fundDebitAmount(),
                command.creditDebitAmount(),
                null,
                null,
                command.metadata()
        );
        return executeTrade(request);
    }

    /**
     * 按交易订单单号查询记录。
     */
    @Override
    public TradeOrderDTO queryByTradeOrderNo(String tradeOrderNo) {
        TradeOrder tradeOrder = tradeRepository.findTradeOrderByTradeOrderNo(normalizeRequired(tradeOrderNo, "tradeOrderNo"))
                .orElseThrow(() -> new NoSuchElementException("trade not found: " + tradeOrderNo));
        return toTradeOrderDTO(tradeOrder);
    }

    /**
     * 按请求单号查询记录。
     */
    @Override
    public TradeOrderDTO queryByRequestNo(String requestNo) {
        TradeOrder tradeOrder = tradeRepository.findTradeOrderByRequestNo(normalizeRequired(requestNo, "requestNo"))
                .orElseThrow(() -> new NoSuchElementException("trade not found for requestNo: " + requestNo));
        return toTradeOrderDTO(tradeOrder);
    }

    /**
     * 按订单查询记录。
     */
    @Override
    public TradeOrderDTO queryByBusinessOrder(String businessDomainCode, String bizOrderNo) {
        TradeBusinessDomainCode normalizedBusinessDomainCode = TradeBusinessDomainCode.from(
                normalizeRequired(businessDomainCode, "businessDomainCode")
        );
        String normalizedBizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        TradeBusinessIndex businessIndex = tradeRepository.findTradeBusinessIndexByBusinessOrder(
                        normalizedBusinessDomainCode,
                        normalizedBizOrderNo
                )
                .orElseThrow(() -> new NoSuchElementException(
                        "trade not found for businessDomainCode=" + normalizedBusinessDomainCode.name()
                                + ", bizOrderNo=" + normalizedBizOrderNo
                ));
        TradeOrder tradeOrder = tradeRepository.findTradeOrderByTradeOrderNo(businessIndex.getTradeOrderNo())
                .orElseThrow(() -> new NoSuchElementException("trade not found: " + businessIndex.getTradeOrderNo()));
        return toTradeOrderDTO(tradeOrder, true, businessIndex);
    }

    /**
     * 查询钱包流程信息。
     */
    @Override
    public List<TradeWalletFlowDTO> queryRecentWalletFlows(Long userId, Integer limit) {
        Long normalizedUserId = requirePositive(userId, "userId");
        int normalizedLimit = normalizeRecentWalletFlowLimit(limit);
        int scanLimit = Math.min(
                MAX_RECENT_WALLET_FLOW_SCAN_LIMIT,
                normalizedLimit * RECENT_WALLET_FLOW_SCAN_MULTIPLIER
        );

        List<TradeOrder> recentTrades = tradeRepository.findRecentSucceededTradesByUserId(normalizedUserId, scanLimit);
        RecentWalletFlowContext flowContext = buildRecentWalletFlowContext(recentTrades, normalizedUserId);
        List<TradeWalletFlowDTO> flowList = new ArrayList<>();
        for (TradeOrder tradeOrder : recentTrades) {
            if (isFundIncomeSettleScene(tradeOrder.getBusinessSceneCode())) {
                // 爱存收益发放仅影响爱存账户，不应出现在余额流水中。
                continue;
            }
            Money signedWalletAmount = resolveSignedWalletAmount(tradeOrder, normalizedUserId);
            if (signedWalletAmount.compareTo(zeroOf(signedWalletAmount)) == 0) {
                continue;
            }
            flowList.add(toTradeWalletFlowDTO(tradeOrder, signedWalletAmount, normalizedUserId, flowContext));
            if (flowList.size() >= normalizedLimit) {
                break;
            }
        }
        return flowList;
    }

    /**
     * 处理支付结果。
     */
    @Transactional
    public void handlePayResultChanged(PayResultChangedPayload payload) {
        if (payload == null) {
            return;
        }
        if (!"TRADE".equalsIgnoreCase(normalizeRequired(payload.sourceBizType(), "sourceBizType"))) {
            return;
        }

        TradeOrder tradeOrder = tradeRepository.findTradeOrderByTradeOrderNo(normalizeRequired(payload.sourceBizNo(), "sourceBizNo"))
                .orElseThrow(() -> new NoSuchElementException("trade not found: " + payload.sourceBizNo()));
        String payOrderNo = normalizeRequired(payload.payOrderNo(), "payOrderNo");
        String currentPayOrderNo = normalizeOptional(tradeOrder.getCurrentPayOrderNo());
        if (currentPayOrderNo != null && !currentPayOrderNo.equals(payOrderNo)) {
            return;
        }
        if (!tradeOrder.shouldApplyPayResult(payload.statusVersion())) {
            return;
        }

        TradeFlowStep resultStep = startFlowStep(
                tradeOrder.getTradeOrderNo(),
                TradeFlowStepCode.PAY_RESULT_APPLY,
                toPayload(Map.of(
                        "payOrderNo", payOrderNo,
                        "payStatus", normalizeRequired(payload.payStatus(), "payStatus"),
                        "statusVersion", String.valueOf(payload.statusVersion())
                ))
        );

        try {
            TradePayResultDecision payResultDecision = tradePayResultDomainService.decide(
                    payload.payStatus(),
                    payload.resultCode(),
                    payload.resultMessage()
            );
            String payStatus = payResultDecision.payStatus();
            LocalDateTime now = LocalDateTime.now();
            tradeOrder.recordPayResult(payload.statusVersion(), payload.resultCode(), payload.resultMessage(), now);

            if (payResultDecision.handling() == TradePayResultHandling.COMMITTED) {
                TradePayOrderSnapshot payOrder = payClient.queryByPayOrderNo(payOrderNo);
                TradeSettleResult settlementResult = tradeSettleClient.settleCommittedTrade(new TradeSettleRequest(
                        tradeOrder.getTradeType().name(),
                        tradeOrder.getPayerUserId(),
                        tradeOrder.getPayeeUserId(),
                        payOrderNo,
                        tradeOrder.getRequestNo(),
                        tradeOrder.getTradeOrderNo(),
                        tradeOrder.getPricingQuoteNo(),
                        tradeOrder.getSettleAmount(),
                        tradeOrder.getOriginalAmount(),
                        tradeOrder.getPayableAmount(),
                        shouldExecutePayeeCredit(tradeOrder)
                ));
                if ("SUCCESS".equals(settlementResult.status())) {
                    tradeOrder.markSucceeded(now);
                    persistBusinessTradeExtensionsIfNecessary(tradeOrder, payOrder);
                    tradeRepository.saveTradeOrder(tradeOrder);
                } else if ("FAILED".equals(settlementResult.status())) {
                    tradeOrder.markFailed(settlementResult.message(), now);
                    tradeRepository.saveTradeOrder(tradeOrder);
                } else {
                    tradeOrder.markReconPending(
                            settlementResult.message(),
                            payload.statusVersion(),
                            payload.resultCode(),
                            payload.resultMessage(),
                            now
                    );
                    tradeRepository.saveTradeOrder(tradeOrder);
                }
            } else {
                switch (payResultDecision.handling()) {
                    case PROCESSING -> tradeOrder.markPayProcessing(
                            payload.statusVersion(),
                            payload.resultCode(),
                            payload.resultMessage(),
                            now
                    );
                    case ROLLED_BACK -> tradeOrder.markRolledBack(payResultDecision.failureReason(), now);
                    case RECON_PENDING -> tradeOrder.markReconPending(
                            payResultDecision.failureReason(),
                            payload.statusVersion(),
                            payload.resultCode(),
                            payload.resultMessage(),
                            now
                    );
                    case FAILED -> tradeOrder.markFailed(payResultDecision.failureReason(), now);
                    case COMMITTED -> throw new IllegalStateException("unexpected committed decision branch");
                }
                tradeRepository.saveTradeOrder(tradeOrder);
            }

            resultStep.markSuccess(toPayload(Map.of(
                    "payOrderNo", payOrderNo,
                    "payStatus", payStatus,
                    "tradeStatus", tradeOrder.getStatus().name()
            )), LocalDateTime.now());
            saveSuccessFlowStepIfEnabled(resultStep);
        } catch (RuntimeException ex) {
            resultStep.markFailed(truncateErrorMessage(ex.getMessage()), LocalDateTime.now());
            tradeRepository.saveFlowStep(resultStep);
            throw ex;
        }
    }

    private TradeOrderDTO executeTrade(TradeExecutionRequest request) {
        String requestNo = normalizeRequired(request.requestNo(), "requestNo");
        Optional<TradeOrder> existingTrade = tradeRepository.findTradeOrderByRequestNo(requestNo);
        if (existingTrade.isPresent()) {
            return toTradeOrderDTO(existingTrade.get(), false);
        }

        LocalDateTime now = LocalDateTime.now();
        Money originalAmount = normalizeAmount(request.amount(), "amount");
        enforceTradeRiskRule(request, originalAmount);
        String candidateTradeOrderNo = buildTradeOrderNo(request.tradeType(), request.payerUserId());
        TradeOrder tradeOrder = TradeOrder.create(
                candidateTradeOrderNo,
                requestNo,
                request.tradeType(),
                normalizeRequired(request.businessSceneCode(), "businessSceneCode"),
                normalizeOptional(request.originalTradeOrderNo()),
                requirePositive(request.payerUserId(), "payerUserId"),
                request.payeeUserId() == null ? null : requirePositive(request.payeeUserId(), "payeeUserId"),
                normalizeRequired(request.paymentMethod(), "paymentMethod"),
                originalAmount,
                normalizeOptional(request.metadata()),
                now
        );
        tradeOrder = tradeRepository.saveTradeOrder(tradeOrder);
        if (!candidateTradeOrderNo.equals(tradeOrder.getTradeOrderNo())) {
            return toTradeOrderDTO(tradeOrder, false);
        }

        PricingQuoteDTO quote = executeQuoteStep(tradeOrder);
        TradeSplitPlan splitPlan = tradeSplitDomainService.resolveSplitPlan(
                request.tradeType(),
                request.paymentMethod(),
                quote.payableAmount(),
                request.walletDebitAmount(),
                request.fundDebitAmount(),
                request.creditDebitAmount(),
                request.inboundDebitAmount()
        );
        if (shouldIgnoreClientSplitForScene(request.businessSceneCode())) {
            log.info(
                    "[{}]入参：{}",
                    logScene(request.businessSceneCode(), "支付"),
                    "requestNo=" + request.requestNo()
                            + ", tradeOrderNo=" + tradeOrder.getTradeOrderNo()
                            + ", quotedPayable=" + moneyLogText(quote.payableAmount())
                            + ", couponNo=" + normalizeOptional(request.couponNo())
                            + ", walletDebit=" + moneyLogText(splitPlan.getWalletDebitAmount())
                            + ", fundDebit=" + moneyLogText(splitPlan.getFundDebitAmount())
                            + ", creditDebit=" + moneyLogText(splitPlan.getCreditDebitAmount())
                            + ", inboundDebit=" + moneyLogText(splitPlan.getInboundDebitAmount())
            );
        }
        validateLoanRepaySplitPlanIfNeeded(tradeOrder, splitPlan);
        TradeSplitPlan submitSplitPlan = resolveSubmitSplitPlan(tradeOrder, splitPlan, request.couponNo());
        if (shouldIgnoreClientSplitForScene(request.businessSceneCode())) {
            log.info(
                    "[{}]入参：{}",
                    logScene(request.businessSceneCode(), "支付"),
                    "requestNo=" + request.requestNo()
                            + ", tradeOrderNo=" + tradeOrder.getTradeOrderNo()
                            + ", couponNo=" + normalizeOptional(request.couponNo())
                            + ", walletDebit=" + moneyLogText(submitSplitPlan.getWalletDebitAmount())
                            + ", fundDebit=" + moneyLogText(submitSplitPlan.getFundDebitAmount())
                            + ", creditDebit=" + moneyLogText(submitSplitPlan.getCreditDebitAmount())
                            + ", inboundDebit=" + moneyLogText(submitSplitPlan.getInboundDebitAmount())
            );
        }
        TradePaySubmitResult submitReceipt = executePaySubmitStep(tradeOrder, submitSplitPlan, request.couponNo());
        tradeOrder.markPaySubmitted(
                submitReceipt.payOrderNo(),
                submitSplitPlan,
                LocalDateTime.now()
        );
        tradeRepository.saveTradeOrder(tradeOrder);
        return toTradeOrderDTO(tradeOrder, false);
    }

    private void enforceTradeRiskRule(TradeExecutionRequest request, Money originalAmount) {
        if (request == null || originalAmount == null) {
            return;
        }
        if (request.tradeType() == TradeType.REFUND) {
            return;
        }
        RiskDecision decision = adminRiskManageFacade.evaluateTradeRisk(
                normalizeRequired(request.businessSceneCode(), "businessSceneCode"),
                requirePositive(request.payerUserId(), "payerUserId"),
                originalAmount.getAmount(),
                originalAmount.getCurrencyUnit().getCode()
        );
        if (!decision.passed()) {
            throw new IllegalArgumentException(normalizeOptional(decision.message()) == null
                    ? "风控校验未通过"
                    : decision.message());
        }
    }

    private PricingQuoteDTO executeQuoteStep(TradeOrder tradeOrder) {
        TradeFlowStep quoteStep = startFlowStep(
                tradeOrder.getTradeOrderNo(),
                TradeFlowStepCode.PRICING_QUOTE,
                toPayload(Map.of(
                        "requestNo", tradeOrder.getRequestNo(),
                        "paymentMethod", tradeOrder.getPaymentMethod(),
                        "currency", tradeOrder.getOriginalAmount().getCurrencyUnit().getCode(),
                        "amount", tradeOrder.getOriginalAmount().getAmount().toPlainString()
                ))
        );

        try {
            PricingQuoteDTO quote = pricingFacade.quote(new PricingQuoteCommand(
                    "TRADE-" + tradeOrder.getRequestNo(),
                    tradeOrder.getBusinessSceneCode(),
                    tradeOrder.getPaymentMethod(),
                    tradeOrder.getOriginalAmount()
            ));
            quoteStep.markSuccess(toPayload(Map.of(
                    "quoteNo", quote.quoteNo(),
                    "feeAmount", quote.feeAmount().getAmount().toPlainString(),
                    "payableAmount", quote.payableAmount().getAmount().toPlainString(),
                    "settleAmount", quote.settleAmount().getAmount().toPlainString()
            )), LocalDateTime.now());
            saveSuccessFlowStepIfEnabled(quoteStep);

            tradeOrder.markPricingQuoteApplied(
                    quote.quoteNo(),
                    quote.feeAmount(),
                    quote.payableAmount(),
                    quote.settleAmount(),
                    LocalDateTime.now()
            );
            tradeRepository.saveTradeOrder(tradeOrder);
            return quote;
        } catch (RuntimeException ex) {
            String errorMessage = truncateErrorMessage(ex.getMessage());
            quoteStep.markFailed(errorMessage, LocalDateTime.now());
            tradeRepository.saveFlowStep(quoteStep);
            tradeOrder.markFailed("pricing quote failed: " + errorMessage, LocalDateTime.now());
            tradeRepository.saveTradeOrder(tradeOrder);
            throw ex;
        }
    }

    private TradePaySubmitResult executePaySubmitStep(TradeOrder tradeOrder, TradeSplitPlan splitPlan, String couponNo) {
        String bizOrderNo = resolvePayBizOrderNo(tradeOrder);
        TradeFlowStep submitStep = startFlowStep(
                tradeOrder.getTradeOrderNo(),
                TradeFlowStepCode.PAY_SUBMIT,
                toPayload(Map.of(
                        "sourceBizType", "TRADE",
                        "sourceBizNo", tradeOrder.getTradeOrderNo(),
                        "bizOrderNo", bizOrderNo,
                        "payableAmount", tradeOrder.getPayableAmount().getAmount().toPlainString(),
                        "walletDebitAmount", splitPlan.getWalletDebitAmount().getAmount().toPlainString(),
                        "fundDebitAmount", splitPlan.getFundDebitAmount().getAmount().toPlainString(),
                        "creditDebitAmount", splitPlan.getCreditDebitAmount().getAmount().toPlainString(),
                        "inboundDebitAmount", splitPlan.getInboundDebitAmount().getAmount().toPlainString()
                ))
        );

        try {
            boolean requiresPayeeCredit = tradeOrder.getTradeType() == TradeType.PAY && shouldExecutePayeeCredit(tradeOrder);
            TradePaySubmitResult receipt = payClient.submit(new TradePaySubmitRequest(
                    "TRADE",
                    tradeOrder.getTradeOrderNo(),
                    tradeOrder.getTradeOrderNo(),
                    tradeOrder.getTradeType().name(),
                    tradeOrder.getSettleAmount(),
                    requiresPayeeCredit,
                    bizOrderNo,
                    tradeOrder.getBusinessSceneCode(),
                    tradeOrder.getPayerUserId(),
                    tradeOrder.getPayeeUserId(),
                    tradeOrder.getPayableAmount(),
                    splitPlan.getWalletDebitAmount(),
                    splitPlan.getFundDebitAmount(),
                    splitPlan.getCreditDebitAmount(),
                    splitPlan.getInboundDebitAmount(),
                    tradeOrder.getTradeType() == TradeType.WITHDRAW ? tradeOrder.getSettleAmount() : null,
                    normalizeOptional(couponNo),
                    null,
                    extractPaymentToolCode(tradeOrder.getMetadata()),
                    tradeOrder.getPaymentMethod()
            ));
            submitStep.markSuccess(toPayload(Map.of(
                    "payOrderNo", receipt.payOrderNo(),
                    "status", receipt.status(),
                    "statusVersion", String.valueOf(receipt.statusVersion())
            )), LocalDateTime.now());
            saveSuccessFlowStepIfEnabled(submitStep);
            return receipt;
        } catch (RuntimeException ex) {
            String errorMessage = truncateErrorMessage(ex.getMessage());
            submitStep.markFailed(errorMessage, LocalDateTime.now());
            tradeRepository.saveFlowStep(submitStep);
            tradeOrder.markFailed("pay submit failed: " + errorMessage, LocalDateTime.now());
            tradeRepository.saveTradeOrder(tradeOrder);
            throw ex;
        }
    }

    private String resolvePayBizOrderNo(TradeOrder tradeOrder) {
        return normalizeRequired(tradeOrder.getRequestNo(), "requestNo");
    }

    private boolean shouldExecutePayeeCredit(TradeOrder tradeOrder) {
        return operatorMerchantRoutingService.shouldCreditPayee(
                tradeOrder.getPayerUserId(),
                tradeOrder.getPayeeUserId()
        );
    }

    private TradeFlowStep startFlowStep(String tradeOrderNo, TradeFlowStepCode stepCode, String requestPayload) {
        return TradeFlowStep.start(tradeOrderNo, stepCode, requestPayload, LocalDateTime.now());
    }

    private void saveSuccessFlowStepIfEnabled(TradeFlowStep step) {
        if (!persistSuccessFlowSteps) {
            return;
        }
        tradeRepository.saveFlowStep(step);
    }

    private TradeSplitPlan resolveSubmitSplitPlan(TradeOrder tradeOrder, TradeSplitPlan baseSplitPlan, String couponNo) {
        String normalizedCouponNo = normalizeOptional(couponNo);
        if (normalizedCouponNo == null || tradeOrder == null || tradeOrder.getTradeType() != TradeType.PAY) {
            return baseSplitPlan;
        }
        if (couponFacade == null) {
            throw new IllegalStateException("couponFacade not configured");
        }
        Money discountAmount = defaultZero(couponFacade.resolveDiscountAmount(normalizedCouponNo));
        if (discountAmount.compareTo(zeroMoney(discountAmount.getCurrencyUnit())) <= 0) {
            return baseSplitPlan;
        }
        Money payableAmount = defaultZero(tradeOrder.getPayableAmount());
        if (payableAmount.compareTo(zeroMoney(payableAmount.getCurrencyUnit())) <= 0) {
            return baseSplitPlan;
        }
        if (!payableAmount.getCurrencyUnit().equals(discountAmount.getCurrencyUnit())) {
            throw new IllegalArgumentException("coupon currency must equal trade currency");
        }
        Money effectiveDiscount = discountAmount.compareTo(payableAmount) > 0 ? payableAmount : discountAmount;
        return applyDiscountToSplitPlan(baseSplitPlan, effectiveDiscount);
    }

    private TradeSplitPlan applyDiscountToSplitPlan(TradeSplitPlan splitPlan, Money discountAmount) {
        CurrencyUnit currencyUnit = discountAmount.getCurrencyUnit();
        Money walletDebit = convertCurrency(splitPlan.getWalletDebitAmount(), currencyUnit);
        Money fundDebit = convertCurrency(splitPlan.getFundDebitAmount(), currencyUnit);
        Money creditDebit = convertCurrency(splitPlan.getCreditDebitAmount(), currencyUnit);
        Money inboundDebit = convertCurrency(splitPlan.getInboundDebitAmount(), currencyUnit);

        Money remainingDiscount = discountAmount.rounded(2, RoundingMode.HALF_UP);

        Money walletDeduction = minMoney(walletDebit, remainingDiscount);
        walletDebit = walletDebit.minus(walletDeduction);
        remainingDiscount = remainingDiscount.minus(walletDeduction);

        Money fundDeduction = minMoney(fundDebit, remainingDiscount);
        fundDebit = fundDebit.minus(fundDeduction);
        remainingDiscount = remainingDiscount.minus(fundDeduction);

        Money creditDeduction = minMoney(creditDebit, remainingDiscount);
        creditDebit = creditDebit.minus(creditDeduction);
        remainingDiscount = remainingDiscount.minus(creditDeduction);

        Money inboundDeduction = minMoney(inboundDebit, remainingDiscount);
        inboundDebit = inboundDebit.minus(inboundDeduction);
        remainingDiscount = remainingDiscount.minus(inboundDeduction);

        if (remainingDiscount.compareTo(zeroMoney(currencyUnit)) > 0) {
            throw new IllegalArgumentException("coupon discount exceeds payable split amount");
        }

        return TradeSplitPlan.of(currencyUnit, walletDebit, fundDebit, creditDebit, inboundDebit);
    }

    private Money minMoney(Money left, Money right) {
        if (!left.getCurrencyUnit().equals(right.getCurrencyUnit())) {
            throw new IllegalArgumentException("money currency mismatch");
        }
        return left.compareTo(right) <= 0 ? left : right;
    }

    private void persistBusinessTradeExtensionsIfNecessary(TradeOrder tradeOrder, TradePayOrderSnapshot committedPayOrder) {
        if (tradeOrder == null) {
            return;
        }
        String paymentToolSnapshot = buildTradePaymentToolSnapshot(tradeOrder, committedPayOrder);
        if (paymentToolSnapshot != null) {
            tradeOrder.refreshPaymentToolSnapshot(paymentToolSnapshot, LocalDateTime.now());
        }
        persistCreditTradeExtensionIfNecessary(tradeOrder);
        persistPayGatewayTradeBusinessIndexIfNecessary(tradeOrder, committedPayOrder);
    }

    private void persistCreditTradeExtensionIfNecessary(TradeOrder tradeOrder) {
        TradeCreditRouteSnapshot creditRoute = resolveCreditPayRoute(tradeOrder);
        if (creditRoute == null) {
            return;
        }

        TradeBusinessDomainCode businessDomainCode = TradeBusinessDomainCode.from(creditRoute.businessDomainCode());
        String accountNo = normalizeRequired(creditRoute.accountNo(), "accountNo");
        String creditBizOrderNo = resolveCreditBizOrderNo(tradeOrder);
        TradeCreditTradeType creditTradeType = resolveCreditTradeType(tradeOrder, businessDomainCode);
        LocalDateTime occurredAt = tradeOrder.getUpdatedAt() == null ? tradeOrder.getCreatedAt() : tradeOrder.getUpdatedAt();
        String billMonth = resolveCreditBillMonth(creditTradeType, occurredAt);
        String billNo = buildCreditBillNo(businessDomainCode, accountNo, billMonth);
        String repaymentPlanNo = businessDomainCode == TradeBusinessDomainCode.AILOAN
                ? buildLoanRepaymentPlanNo(accountNo, creditBizOrderNo)
                : null;
        Money subjectAmount = resolveCreditSubjectAmount(tradeOrder, creditTradeType);

        tradeRepository.saveTradeCreditOrder(new TradeCreditOrder(
                null,
                creditBizOrderNo,
                tradeOrder.getTradeOrderNo(),
                businessDomainCode == TradeBusinessDomainCode.AILOAN
                        ? TradeCreditProductType.AILOAN
                        : TradeCreditProductType.AICREDIT,
                accountNo,
                billNo,
                billMonth,
                repaymentPlanNo,
                creditTradeType,
                subjectAmount,
                subjectAmount,
                zeroMoney(subjectAmount.getCurrencyUnit()),
                defaultZero(tradeOrder.getFeeAmount()),
                resolveCreditCounterpartyName(businessDomainCode, creditTradeType),
                occurredAt,
                tradeOrder.getCreatedAt(),
                tradeOrder.getUpdatedAt()
        ));
    }

    private void persistPayGatewayTradeBusinessIndexIfNecessary(TradeOrder tradeOrder, TradePayOrderSnapshot committedPayOrder) {
        TradeBusinessDomainCode businessDomainCode = TradeBusinessDomainCode.from(tradeOrder.getBusinessDomainCode());
        if (businessDomainCode != TradeBusinessDomainCode.INBOUND
                && businessDomainCode != TradeBusinessDomainCode.OUTBOUND) {
            return;
        }

        String payOrderNo = normalizeOptional(tradeOrder.getPayOrderNo());
        if (payOrderNo == null) {
            return;
        }

        String participantType = businessDomainCode == TradeBusinessDomainCode.INBOUND ? "INBOUND" : "OUTBOUND";
        TradePayParticipantSnapshot participant = findPayParticipant(committedPayOrder, participantType);
        if (participant == null) {
            participant = payClient.queryParticipantBranch(payOrderNo, participantType);
        }
        if (participant == null) {
            return;
        }

        Map<String, String> payload = parsePayload(participant.requestPayload());
        if (businessDomainCode == TradeBusinessDomainCode.INBOUND) {
            persistInboundTradeBusinessIndex(tradeOrder, payload);
            return;
        }
        persistOutboundTradeBusinessIndex(tradeOrder, payload);
    }

    private void persistInboundTradeBusinessIndex(TradeOrder tradeOrder, Map<String, String> payload) {
        String inboundId = normalizeOptional(payload.get("inboundId"));
        if (inboundId == null) {
            return;
        }
        Money amount = firstPositiveMoney(tradeOrder.getSettleAmount(), tradeOrder.getOriginalAmount(), tradeOrder.getPayableAmount());
        String accountNo = normalizeOptional(payload.get("payerAccountNo"));
        persistBusinessIndexSnapshot(
                tradeOrder,
                TradeBusinessDomainCode.INBOUND,
                inboundId,
                TradeBusinessDomainCode.INBOUND.name(),
                tradeOrder.getTradeType().name(),
                accountNo,
                null,
                null,
                "银行卡充值",
                buildBankDisplaySubtitle(payload, accountNo, tradeOrder.getPaymentMethod()),
                amount
        );
    }

    private void persistOutboundTradeBusinessIndex(TradeOrder tradeOrder, Map<String, String> payload) {
        String outboundId = normalizeOptional(payload.get("outboundId"));
        if (outboundId == null) {
            return;
        }
        Money amount = firstPositiveMoney(tradeOrder.getSettleAmount(), tradeOrder.getOriginalAmount(), tradeOrder.getPayableAmount());
        String accountNo = normalizeOptional(payload.get("payeeAccountNo"));
        persistBusinessIndexSnapshot(
                tradeOrder,
                TradeBusinessDomainCode.OUTBOUND,
                outboundId,
                TradeBusinessDomainCode.OUTBOUND.name(),
                tradeOrder.getTradeType().name(),
                accountNo,
                null,
                null,
                "银行卡提现",
                buildBankDisplaySubtitle(payload, accountNo, tradeOrder.getPaymentMethod()),
                amount
        );
    }

    private void persistBusinessIndexSnapshot(TradeOrder tradeOrder,
                                              TradeBusinessDomainCode businessDomainCode,
                                              String bizOrderNo,
                                              String productType,
                                              String businessType,
                                              String accountNo,
                                              String billNo,
                                              String billMonth,
                                              String displayTitle,
                                              String displaySubtitle,
                                              Money amount) {
        String normalizedBizOrderNo = normalizeOptional(bizOrderNo);
        if (normalizedBizOrderNo == null) {
            return;
        }

        Money indexAmount = amount == null
                ? firstPositiveMoney(tradeOrder.getSettleAmount(), tradeOrder.getOriginalAmount(), tradeOrder.getPayableAmount())
                : amount.rounded(2, RoundingMode.HALF_UP);
        LocalDateTime tradeTime = tradeOrder.getUpdatedAt() == null ? tradeOrder.getCreatedAt() : tradeOrder.getUpdatedAt();

        tradeRepository.saveTradeBusinessIndex(new TradeBusinessIndex(
                null,
                tradeOrder.getTradeOrderNo(),
                businessDomainCode,
                normalizedBizOrderNo,
                normalizeOptional(productType),
                normalizeOptional(businessType),
                tradeOrder.getPayerUserId(),
                tradeOrder.getPayeeUserId(),
                normalizeOptional(accountNo),
                normalizeOptional(billNo),
                normalizeOptional(billMonth),
                normalizeOptional(displayTitle),
                normalizeOptional(displaySubtitle),
                indexAmount,
                tradeOrder.getStatus(),
                tradeTime,
                tradeOrder.getCreatedAt(),
                tradeTime
        ));
    }

    private TradePayParticipantSnapshot findPayParticipant(TradePayOrderSnapshot payOrder, String participantType) {
        String normalizedParticipantType = normalizeRequired(participantType, "participantType");
        if (payOrder == null || payOrder.participants() == null) {
            return null;
        }
        for (TradePayParticipantSnapshot participant : payOrder.participants()) {
            if (normalizedParticipantType.equalsIgnoreCase(participant.participantType())) {
                return participant;
            }
        }
        return null;
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

    private String buildTradePaymentToolSnapshot(TradeOrder tradeOrder, TradePayOrderSnapshot committedPayOrder) {
        TradePayOrderSnapshot payOrderSnapshot = committedPayOrder;
        if (payOrderSnapshot == null) {
            String payOrderNo = normalizeOptional(tradeOrder.getPayOrderNo());
            if (payOrderNo != null) {
                payOrderSnapshot = payClient.queryByPayOrderNo(payOrderNo);
            }
        }
        if (payOrderSnapshot == null) {
            return normalizeOptional(tradeOrder.getPaymentToolSnapshot());
        }

        List<Map<String, Object>> payerTools = new ArrayList<>();
        List<Map<String, Object>> payeeTools = new ArrayList<>();
        Set<String> payerToolKeys = new LinkedHashSet<>();
        Set<String> payeeToolKeys = new LinkedHashSet<>();

        List<TradePayFundDetailSnapshot> fundDetails = payOrderSnapshot.fundDetails() == null
                ? List.of()
                : payOrderSnapshot.fundDetails();
        for (TradePayFundDetailSnapshot detail : fundDetails) {
            if (detail == null) {
                continue;
            }
            Map<String, Object> tool = toPaymentToolMapFromFundDetail(detail);
            if (tool.isEmpty()) {
                continue;
            }
            String owner = normalizeOptional(detail.detailOwner());
            if ("PAYEE".equalsIgnoreCase(owner)) {
                appendToolIfAbsent(payeeTools, payeeToolKeys, tool);
            } else {
                appendToolIfAbsent(payerTools, payerToolKeys, tool);
            }
        }

        List<TradePayParticipantSnapshot> participants = payOrderSnapshot.participants() == null
                ? List.of()
                : payOrderSnapshot.participants();
        for (TradePayParticipantSnapshot participant : participants) {
            if (participant == null) {
                continue;
            }
            String participantType = normalizeOptional(participant.participantType());
            if (participantType == null) {
                continue;
            }
            if ("INBOUND".equalsIgnoreCase(participantType)) {
                Map<String, Object> tool = toBankToolMapFromParticipant(participant, "PAYER");
                appendToolIfAbsent(payerTools, payerToolKeys, tool);
                continue;
            }
            if ("OUTBOUND".equalsIgnoreCase(participantType)) {
                Map<String, Object> tool = toBankToolMapFromParticipant(participant, "PAYEE");
                appendToolIfAbsent(payeeTools, payeeToolKeys, tool);
            }
        }

        if (payerTools.isEmpty() && payeeTools.isEmpty()) {
            return null;
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("version", PAYMENT_TOOL_SNAPSHOT_VERSION);
        snapshot.put("tradeOrderNo", tradeOrder.getTradeOrderNo());
        snapshot.put("payOrderNo", payOrderSnapshot.payOrderNo());
        snapshot.put("generatedAt", LocalDateTime.now().toString());
        snapshot.put("payerTools", payerTools);
        snapshot.put("payeeTools", payeeTools);
        try {
            return OBJECT_MAPPER.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            log.warn("build paymentToolSnapshot failed, tradeOrderNo={}, error={}", tradeOrder.getTradeOrderNo(), ex.getMessage());
            return null;
        }
    }

    private Map<String, Object> toPaymentToolMapFromFundDetail(TradePayFundDetailSnapshot detail) {
        Map<String, Object> tool = new LinkedHashMap<>();
        putIfHasText(tool, "owner", detail.detailOwner());
        putIfHasText(tool, "payTool", detail.payTool());
        putMoney(tool, "amount", detail.amount());
        putMoney(tool, "cumulativeRefundAmount", detail.cumulativeRefundAmount());
        putIfHasText(tool, "channel", detail.channel());
        putIfHasText(tool, "bankOrderNo", detail.bankOrderNo());
        putIfHasText(tool, "bankCardNo", detail.bankCardNo());
        putMoney(tool, "channelFeeAmount", detail.channelFeeAmount());
        putIfHasText(tool, "depositOrderNo", detail.depositOrderNo());
        putIfHasText(tool, "instId", detail.instId());
        putIfHasText(tool, "instChannelCode", detail.instChannelCode());
        putIfHasText(tool, "payChannelCode", detail.payChannelCode());
        putIfHasText(tool, "bankCode", detail.bankCode());
        putIfHasText(tool, "bankName", detail.bankName());
        putIfHasText(tool, "cardType", detail.cardType());
        putIfHasText(tool, "cardHolderName", detail.cardHolderName());
        putIfHasText(tool, "cardTailNo", detail.cardTailNo());
        putIfHasText(tool, "toolSnapshot", detail.toolSnapshot());
        putIfHasText(tool, "redPacketId", detail.redPacketId());
        putIfHasText(tool, "accountNo", detail.accountNo());
        putIfHasText(tool, "fundCode", detail.fundCode());
        putIfHasText(tool, "fundProductCode", detail.fundProductCode());
        putIfHasText(tool, "fundAccountIdentity", detail.fundAccountIdentity());
        putIfHasText(tool, "creditAccountNo", detail.creditAccountNo());
        putIfHasText(tool, "creditAccountType", detail.creditAccountType());
        putIfHasText(tool, "creditProductCode", detail.creditProductCode());
        return tool;
    }

    private Map<String, Object> toBankToolMapFromParticipant(TradePayParticipantSnapshot participant, String owner) {
        Map<String, String> payload = parsePayload(participant.requestPayload());
        String bankCardNo = firstNonBlank(payload.get("payerAccountNo"), payload.get("payeeAccountNo"));
        if (bankCardNo == null) {
            return Map.of();
        }
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("owner", normalizeRequired(owner, "owner"));
        tool.put("payTool", "BANK_CARD");
        putIfHasText(tool, "bankCardNo", bankCardNo);
        putIfHasText(tool, "instId", payload.get("instId"));
        putIfHasText(tool, "instChannelCode", payload.get("instChannelCode"));
        putIfHasText(tool, "payChannelCode", payload.get("payChannelCode"));
        putIfHasText(tool, "bankCode", payload.get("bankCode"));
        putIfHasText(tool, "bankName", payload.get("bankName"));
        putIfHasText(tool, "cardType", payload.get("cardType"));
        putIfHasText(tool, "cardHolderName", payload.get("cardHolderName"));
        putIfHasText(tool, "cardTailNo", payload.get("cardTailNo"));
        putIfHasText(tool, "toolSnapshot", payload.get("toolSnapshot"));
        putIfHasText(tool, "participantType", participant.participantType());
        putIfHasText(tool, "participantBranchId", participant.branchId());
        putIfHasText(tool, "participantResourceId", participant.participantResourceId());
        return tool;
    }

    private void appendToolIfAbsent(List<Map<String, Object>> tools, Set<String> dedupKeys, Map<String, Object> tool) {
        if (tool == null || tool.isEmpty()) {
            return;
        }
        String dedupKey = buildPaymentToolDedupKey(tool);
        if (dedupKey == null) {
            tools.add(tool);
            return;
        }
        if (dedupKeys.add(dedupKey)) {
            tools.add(tool);
        }
    }

    private String buildPaymentToolDedupKey(Map<String, Object> tool) {
        String owner = normalizeAny(tool.get("owner"));
        String payTool = normalizeAny(tool.get("payTool"));
        String payChannelCode = normalizeAny(tool.get("payChannelCode"));
        String instChannelCode = normalizeAny(tool.get("instChannelCode"));
        String bankCardNo = normalizeAny(tool.get("bankCardNo"));
        String accountNo = normalizeAny(tool.get("accountNo"));
        String fundCode = normalizeAny(tool.get("fundCode"));
        String creditAccountNo = normalizeAny(tool.get("creditAccountNo"));
        String redPacketId = normalizeAny(tool.get("redPacketId"));
        if (owner == null && payTool == null && payChannelCode == null && instChannelCode == null
                && bankCardNo == null && accountNo == null && fundCode == null && creditAccountNo == null
                && redPacketId == null) {
            return null;
        }
        return String.join(
                "|",
                defaultDash(owner),
                defaultDash(payTool),
                defaultDash(payChannelCode),
                defaultDash(instChannelCode),
                defaultDash(bankCardNo),
                defaultDash(accountNo),
                defaultDash(fundCode),
                defaultDash(creditAccountNo),
                defaultDash(redPacketId)
        );
    }

    private void putIfHasText(Map<String, Object> target, String key, String value) {
        String normalizedKey = normalizeOptional(key);
        String normalizedValue = normalizeOptional(value);
        if (normalizedKey == null || normalizedValue == null) {
            return;
        }
        target.put(normalizedKey, normalizedValue);
    }

    private void putMoney(Map<String, Object> target, String key, Money value) {
        if (value == null) {
            return;
        }
        Map<String, Object> money = new LinkedHashMap<>();
        money.put("currency", value.getCurrencyUnit().getCode());
        money.put("amount", value.getAmount().toPlainString());
        target.put(key, money);
    }

    private String normalizeAny(Object value) {
        if (value == null) {
            return null;
        }
        return normalizeOptional(String.valueOf(value));
    }

    private String defaultDash(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? "-" : normalized;
    }

    private String buildBankDisplaySubtitle(Map<String, String> payload, String accountNo, String fallback) {
        String bankName = normalizeOptional(payload.get("bankName"));
        String tail = lastFourDigits(accountNo);
        if (bankName != null && tail != null) {
            return bankName + "(尾号" + tail + ")";
        }
        if (bankName != null) {
            return bankName;
        }
        if (tail != null) {
            return "尾号" + tail;
        }
        return normalizeOptional(fallback);
    }

    private String lastFourDigits(String rawValue) {
        String normalized = normalizeOptional(rawValue);
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

    private String firstNonBlank(String preferred, String fallback) {
        String normalizedPreferred = normalizeOptional(preferred);
        if (normalizedPreferred != null) {
            return normalizedPreferred;
        }
        return normalizeOptional(fallback);
    }

    private TradeCreditRouteSnapshot resolveCreditPayRoute(TradeOrder tradeOrder) {
        return tradePayRouteClient.routeCreditForTrade(
                tradeOrder.getBusinessDomainCode(),
                tradeOrder.getBusinessSceneCode(),
                tradeOrder.getPaymentMethod(),
                tradeOrder.getPayerUserId(),
                tradeOrder.getPayeeUserId()
        );
    }

    private TradeCreditTradeType resolveCreditTradeType(TradeOrder tradeOrder, TradeBusinessDomainCode businessDomainCode) {
        boolean creditRepayScene = isCreditRepayBusinessScene(tradeOrder.getBusinessSceneCode());
        if (businessDomainCode == TradeBusinessDomainCode.AILOAN) {
            return creditRepayScene ? TradeCreditTradeType.LOAN_REPAY : TradeCreditTradeType.LOAN_DRAW;
        }
        if (!creditRepayScene) {
            return TradeCreditTradeType.CONSUME;
        }
        String metadata = normalizeOptional(tradeOrder.getMetadata());
        if (containsMinimumRepayKeyword(metadata)) {
            return TradeCreditTradeType.MINIMUM_REPAY;
        }
        if (containsFullRepayKeyword(metadata)) {
            return TradeCreditTradeType.FULL_REPAY;
        }
        return TradeCreditTradeType.REPAY;
    }

    /**
     * 解析信用业务扩展单的主体金额。
     *
     * 业务场景：爱花消费、手机充值等信用支付场景里，账单应展示用户真实占用的信用额度；
     * settleAmount 是商户侧结算金额，可能因计费扣减而小于实际信用扣款，因此消费类交易必须优先取 creditDebitAmount。
     */
    private Money resolveCreditSubjectAmount(TradeOrder tradeOrder, TradeCreditTradeType creditTradeType) {
        if (creditTradeType == TradeCreditTradeType.CONSUME || creditTradeType == TradeCreditTradeType.LOAN_DRAW) {
            return firstPositiveMoney(
                    tradeOrder.getCreditDebitAmount(),
                    tradeOrder.getOriginalAmount(),
                    tradeOrder.getPayableAmount(),
                    tradeOrder.getSettleAmount()
            );
        }
        return firstPositiveMoney(
                tradeOrder.getOriginalAmount(),
                tradeOrder.getPayableAmount(),
                tradeOrder.getSettleAmount()
        );
    }

    private String resolveCreditBillMonth(TradeCreditTradeType creditTradeType, LocalDateTime occurredAt) {
        LocalDate baseDate = (occurredAt == null ? LocalDate.now() : occurredAt.toLocalDate()).withDayOfMonth(1);
        if (creditTradeType == TradeCreditTradeType.CONSUME) {
            baseDate = baseDate.plusMonths(1);
        }
        return String.format(Locale.ROOT, "%04d-%02d", baseDate.getYear(), baseDate.getMonthValue());
    }

    private String buildCreditBillNo(TradeBusinessDomainCode businessDomainCode, String accountNo, String billMonth) {
        if (billMonth == null) {
            return null;
        }
        String normalizedBillMonth = billMonth.replace("-", "");
        String prefix = businessDomainCode == TradeBusinessDomainCode.AILOAN ? "JBILL" : "HBILL";
        return prefix + "-" + accountNo + "-" + normalizedBillMonth;
    }

    private String buildLoanRepaymentPlanNo(String accountNo, String bizOrderNo) {
        return "JPLAN-" + accountNo + "-" + bizOrderNo;
    }

    private String resolveCreditBizOrderNo(TradeOrder tradeOrder) {
        String payOrderNo = normalizeOptional(tradeOrder.getPayOrderNo());
        if (payOrderNo != null) {
            return payOrderNo;
        }
        return normalizeRequired(tradeOrder.getBizOrderNo(), "bizOrderNo");
    }

    private String resolveCreditCounterpartyName(TradeBusinessDomainCode businessDomainCode, TradeCreditTradeType creditTradeType) {
        if (businessDomainCode == TradeBusinessDomainCode.AILOAN) {
            return creditTradeType == TradeCreditTradeType.LOAN_REPAY ? "爱借还款" : "爱借借款";
        }
        return creditTradeType == TradeCreditTradeType.REPAY ? "爱花还款" : "爱花消费";
    }

    private boolean isCreditRepayBusinessScene(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.equals("APP_CREDIT_REPAY")
                || (upper.contains("CREDIT") && upper.contains("REPAY"))
                || (upper.contains("AICREDIT") && upper.contains("REPAY"))
                || (upper.contains("LOAN") && upper.contains("REPAY"))
                || (upper.contains("AILOAN") && upper.contains("REPAY"));
    }

    private boolean containsMinimumRepayKeyword(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("MINIMUM_REPAY")
                || upper.contains("MIN_REPAY")
                || upper.contains("MINIMUM=true")
                || upper.contains("REPAYMODE=MINIMUM");
    }

    private boolean containsFullRepayKeyword(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("FULL_REPAY")
                || upper.contains("ALL_REPAY")
                || upper.contains("FULL=true")
                || upper.contains("REPAYMODE=FULL");
    }

    private String mergePaymentToolMetadata(String metadata, String paymentToolCode) {
        String normalizedMetadata = normalizeOptional(metadata);
        String normalizedToolCode = normalizeOptional(paymentToolCode);
        if (normalizedToolCode == null) {
            return normalizedMetadata;
        }
        if (normalizedMetadata == null) {
            return "payToolCode=" + normalizedToolCode;
        }
        return normalizedMetadata + ";payToolCode=" + normalizedToolCode;
    }

    private String extractPaymentToolCode(String metadata) {
        String normalizedMetadata = normalizeOptional(metadata);
        if (normalizedMetadata == null) {
            return null;
        }
        String[] segments = normalizedMetadata.split(";");
        for (String segment : segments) {
            String[] kv = segment.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            if (!"payToolCode".equalsIgnoreCase(kv[0].trim())) {
                continue;
            }
            return normalizeOptional(kv[1]);
        }
        return null;
    }

    private void validateLoanRepayPaymentMethodIfNeeded(String businessSceneCode, String paymentMethod, String metadata) {
        if (!isLoanRepayBusinessSceneWithMetadata(businessSceneCode, metadata)) {
            return;
        }
        String normalizedPaymentMethod = normalizeOptional(paymentMethod);
        if (normalizedPaymentMethod == null) {
            throw new IllegalArgumentException("loan repay only supports WALLET or BANK_CARD paymentMethod");
        }
        String upper = normalizedPaymentMethod.toUpperCase(Locale.ROOT);
        if (upper.equals("WALLET")
                || upper.startsWith("WALLET:")
                || upper.equals("BANK_CARD")
                || upper.startsWith("BANK_CARD:")) {
            return;
        }
        throw new IllegalArgumentException("loan repay only supports WALLET or BANK_CARD paymentMethod");
    }

    private void validateLoanRepaySplitPlanIfNeeded(TradeOrder tradeOrder, TradeSplitPlan splitPlan) {
        if (tradeOrder == null || splitPlan == null) {
            return;
        }
        if (!isLoanRepayBusinessSceneWithMetadata(tradeOrder.getBusinessSceneCode(), tradeOrder.getMetadata())) {
            return;
        }
        Money zero = zeroOf(splitPlan.getWalletDebitAmount());
        if (splitPlan.getFundDebitAmount().compareTo(zero) > 0 || splitPlan.getCreditDebitAmount().compareTo(zero) > 0) {
            throw new IllegalArgumentException("loan repay splitPlan only supports walletDebitAmount and inboundDebitAmount");
        }
        Money paidByWalletAndBank = splitPlan.getWalletDebitAmount().plus(splitPlan.getInboundDebitAmount());
        if (paidByWalletAndBank.compareTo(zero) <= 0) {
            throw new IllegalArgumentException("loan repay splitPlan requires walletDebitAmount or inboundDebitAmount");
        }
    }

    private boolean isLoanRepayBusinessSceneWithMetadata(String businessSceneCode, String metadata) {
        String normalizedScene = normalizeOptional(businessSceneCode);
        String normalizedMetadata = normalizeOptional(metadata);
        if (normalizedMetadata != null) {
            String upperMetadata = normalizedMetadata.toUpperCase(Locale.ROOT);
            if (upperMetadata.contains("LOANREPAY=TRUE") || upperMetadata.contains("LOAN_REPAY=TRUE")) {
                return true;
            }
        }
        if (normalizedScene == null) {
            return false;
        }
        String upperScene = normalizedScene.toUpperCase(Locale.ROOT);
        return upperScene.contains("LOAN")
                && upperScene.contains("REPAY");
    }

    private int normalizeRecentWalletFlowLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_RECENT_WALLET_FLOW_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_RECENT_WALLET_FLOW_LIMIT));
    }

    private TradeWalletFlowDTO toTradeWalletFlowDTO(TradeOrder tradeOrder,
                                                    Money signedWalletAmount,
                                                    Long currentUserId,
                                                    RecentWalletFlowContext flowContext) {
        String direction = signedWalletAmount.compareTo(zeroOf(signedWalletAmount)) < 0 ? "DEBIT" : "CREDIT";
        WalletFlowPresentation presentation = resolveWalletFlowPresentation(tradeOrder, direction, currentUserId, flowContext);
        return new TradeWalletFlowDTO(
                tradeOrder.getTradeOrderNo(),
                tradeOrder.getTradeType().name(),
                tradeOrder.getBusinessSceneCode(),
                direction,
                formatSignedMoney(signedWalletAmount),
                signedWalletAmount.getCurrencyUnit().getCode(),
                presentation.counterpartyUserId() == null ? null : presentation.counterpartyUserId().toString(),
                presentation.counterpartyNickname(),
                presentation.counterpartyAvatarUrl(),
                normalizeBrandDisplayText(presentation.displayTitle()),
                tradeOrder.getUpdatedAt() == null ? tradeOrder.getCreatedAt() : tradeOrder.getUpdatedAt()
        );
    }

    private WalletFlowPresentation resolveWalletFlowPresentation(TradeOrder tradeOrder,
                                                                 String direction,
                                                                 Long currentUserId,
                                                                 RecentWalletFlowContext flowContext) {
        Long counterpartyUserId = resolveCounterpartyUserId(tradeOrder, currentUserId);
        String counterpartyNickname = null;
        String counterpartyAvatarUrl = null;
        String businessSceneCode = normalizeOptional(tradeOrder.getBusinessSceneCode());
        String displayTitle = isFundSubscribeScene(businessSceneCode) && "DEBIT".equals(direction)
                ? "爱存-单次转入"
                : isFundFastRedeemScene(businessSceneCode)
                ? resolveFundRedeemDisplayTitle(tradeOrder)
                : resolveFlowDisplayTitle(tradeOrder.getTradeType(), direction, counterpartyUserId);
        if (RED_PACKET_SEND_SCENE.equalsIgnoreCase(businessSceneCode) && "DEBIT".equals(direction)) {
            RedPacketOrder redPacketOrder = flowContext.fundingRedPacketByTradeOrderNo().get(tradeOrder.getTradeOrderNo());
            if (redPacketOrder != null) {
                counterpartyUserId = redPacketOrder.getReceiverUserId();
                counterpartyNickname = resolveUserNickname(counterpartyUserId, flowContext.userProfileMap());
                counterpartyAvatarUrl = resolveUserAvatarUrl(counterpartyUserId, flowContext.userProfileMap());
                String displayName = normalizeOptional(counterpartyNickname);
                if (displayName == null && counterpartyUserId != null) {
                    displayName = "用户" + counterpartyUserId;
                }
                displayTitle = displayName == null ? "发红包支出" : "向" + displayName + "发红包";
            }
        } else if (RED_PACKET_CLAIM_SCENE.equalsIgnoreCase(businessSceneCode) && "CREDIT".equals(direction)) {
            RedPacketOrder redPacketOrder = flowContext.claimRedPacketByTradeOrderNo().get(tradeOrder.getTradeOrderNo());
            if (redPacketOrder != null) {
                counterpartyUserId = redPacketOrder.getSenderUserId();
                counterpartyNickname = resolveUserNickname(counterpartyUserId, flowContext.userProfileMap());
                counterpartyAvatarUrl = resolveUserAvatarUrl(counterpartyUserId, flowContext.userProfileMap());
                String displayName = normalizeOptional(counterpartyNickname);
                if (displayName == null && counterpartyUserId != null) {
                    displayName = "用户" + counterpartyUserId;
                }
                displayTitle = displayName == null ? "收到红包" : "收到来自" + displayName + "的红包";
            }
        } else if (counterpartyUserId != null) {
            counterpartyNickname = resolveUserNickname(counterpartyUserId, flowContext.userProfileMap());
            counterpartyAvatarUrl = resolveUserAvatarUrl(counterpartyUserId, flowContext.userProfileMap());
        }
        return new WalletFlowPresentation(counterpartyUserId, counterpartyNickname, counterpartyAvatarUrl, displayTitle);
    }

    private String normalizeBrandDisplayText(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return null;
        }
        return normalized
                .replace("爱付", "爱付")
                .replace("AiPay", "爱付")
                .replace("aipay", "爱付")
                .replace("AiPay", "爱付")
                .replace("爱存", "爱存")
                .replace("AiCash", "爱存")
                .replace("AiCash", "爱存")
                .replace("aicash", "爱存")
                .replace("AiCash", "爱存")
                .replace("爱花", "爱花")
                .replace("AiCredit", "爱花")
                .replace("aicredit", "爱花")
                .replace("AiCredit", "爱花")
                .replace("爱借", "爱借")
                .replace("AiLoan", "爱借")
                .replace("ailoan", "爱借")
                .replace("AiLoan", "爱借");
    }

    private Money resolveSignedWalletAmount(TradeOrder tradeOrder, Long currentUserId) {
        Long payerUserId = tradeOrder.getPayerUserId();
        Long payeeUserId = tradeOrder.getPayeeUserId();
        boolean isPayer = currentUserId.equals(payerUserId);
        boolean isPayee = payeeUserId != null && currentUserId.equals(payeeUserId);

        if (isPayer && !isPayee) {
            return firstPositiveMoney(
                    tradeOrder.getWalletDebitAmount(),
                    tradeOrder.getPayableAmount(),
                    tradeOrder.getOriginalAmount()
            ).negated().rounded(2, RoundingMode.HALF_UP);
        }
        if (!isPayer && isPayee) {
            return firstPositiveMoney(
                    tradeOrder.getSettleAmount(),
                    tradeOrder.getOriginalAmount(),
                    tradeOrder.getPayableAmount()
            ).rounded(2, RoundingMode.HALF_UP);
        }
        if (!isPayer && !isPayee) {
            return zeroMoney(tradeOrder.getOriginalAmount().getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
        }
        return resolveSignedWalletAmountForSelfTrade(tradeOrder);
    }

    private Money resolveSignedWalletAmountForSelfTrade(TradeOrder tradeOrder) {
        Money debitAmount = firstPositiveMoney(
                tradeOrder.getWalletDebitAmount(),
                tradeOrder.getPayableAmount()
        );
        Money creditAmount = firstPositiveMoney(
                tradeOrder.getSettleAmount(),
                tradeOrder.getOriginalAmount()
        );
        switch (tradeOrder.getTradeType()) {
            case DEPOSIT, REFUND -> {
                return creditAmount.rounded(2, RoundingMode.HALF_UP);
            }
            case WITHDRAW -> {
                if (isFundFastRedeemScene(tradeOrder.getBusinessSceneCode())) {
                    return creditAmount.rounded(2, RoundingMode.HALF_UP);
                }
                return debitAmount.negated().rounded(2, RoundingMode.HALF_UP);
            }
            case PAY, TRANSFER -> {
                return debitAmount.negated().rounded(2, RoundingMode.HALF_UP);
            }
            default -> {
                if (debitAmount.compareTo(zeroOf(debitAmount)) > 0) {
                    return debitAmount.negated().rounded(2, RoundingMode.HALF_UP);
                }
                return creditAmount.rounded(2, RoundingMode.HALF_UP);
            }
        }
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

    private Long resolveCounterpartyUserId(TradeOrder tradeOrder, Long currentUserId) {
        Long payerUserId = tradeOrder.getPayerUserId();
        Long payeeUserId = tradeOrder.getPayeeUserId();
        if (currentUserId.equals(payerUserId) && payeeUserId != null && !currentUserId.equals(payeeUserId)) {
            return payeeUserId;
        }
        if (currentUserId.equals(payeeUserId) && payerUserId != null && !currentUserId.equals(payerUserId)) {
            return payerUserId;
        }
        return null;
    }

    private String resolveFlowDisplayTitle(TradeType tradeType, String direction, Long counterpartyUserId) {
        String counterpartyName = counterpartyUserId == null ? null : "用户" + counterpartyUserId;
        return switch (tradeType) {
            case TRANSFER -> "DEBIT".equals(direction)
                    ? (counterpartyName == null ? "转账支出" : "向" + counterpartyName + "转账")
                    : (counterpartyName == null ? "转账收入" : "收到" + counterpartyName + "转账");
            case PAY -> "DEBIT".equals(direction)
                    ? "支付消费"
                    : (counterpartyName == null ? "收款入账" : "收到" + counterpartyName + "付款");
            case WITHDRAW -> "余额提现";
            case DEPOSIT -> "余额充值";
            case REFUND -> "退款入账";
        };
    }

    private String resolveFundRedeemDisplayTitle(TradeOrder tradeOrder) {
        String paymentMethod = normalizeOptional(tradeOrder == null ? null : tradeOrder.getPaymentMethod());
        if (paymentMethod == null) {
            return "爱存转出至余额";
        }
        String upper = paymentMethod.toUpperCase(Locale.ROOT);
        if (upper.contains("WALLET") || paymentMethod.contains("余额")) {
            return "爱存转出至余额";
        }
        String bankName = extractBankName(paymentMethod);
        if (bankName != null) {
            return "爱存转出至" + bankName;
        }
        if (upper.contains("BANK_CARD")
                || upper.contains("BANK")
                || upper.contains("CARD")
                || containsBankCardHint(paymentMethod)) {
            return "爱存转出至银行卡";
        }
        // 爱存转出只有余额或银行卡两个去向，非余额文本统一兜底成银行卡。
        return "爱存转出至银行卡";
    }

    private String extractBankName(String candidate) {
        String normalized = normalizeOptional(candidate);
        if (normalized == null || normalized.contains("余额")) {
            return null;
        }
        Matcher matcher = BANK_NAME_PATTERN.matcher(normalized);
        String matched = null;
        while (matcher.find()) {
            String current = normalizeOptional(matcher.group(1));
            if (current != null && !"银行".equals(current)) {
                matched = current;
            }
        }
        return normalizeRedeemBankName(matched);
    }

    private String normalizeRedeemBankName(String rawBankName) {
        String normalized = normalizeOptional(rawBankName);
        if (normalized == null) {
            return null;
        }
        String stripped = normalized.trim();
        while (true) {
            String next = stripped
                    .replaceFirst("^爱存转出至", "")
                    .replaceFirst("^爱存转出到", "")
                    .replaceFirst("^余额宝转出至", "")
                    .replaceFirst("^余额宝转出到", "")
                    .replaceFirst("^转出至", "")
                    .replaceFirst("^转出到", "")
                    .replaceFirst("^提现至", "")
                    .replaceFirst("^提现到", "")
                    .trim();
            if (next.equals(stripped)) {
                break;
            }
            stripped = next;
        }
        stripped = stripped
                .replaceFirst("^[：:\\s]+", "")
                .replaceFirst("[：:\\s]+$", "");
        if (stripped.isBlank() || "银行".equals(stripped) || "银行卡".equals(stripped)) {
            return null;
        }
        return stripped;
    }

    private boolean containsBankCardHint(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        return normalized.contains("银行卡")
                || normalized.contains("银行")
                || normalized.contains("借记卡")
                || normalized.contains("储蓄卡")
                || normalized.contains("尾号");
    }

    private boolean isFundFastRedeemScene(String businessSceneCode) {
        return "FUND_FAST_REDEEM".equalsIgnoreCase(normalizeOptional(businessSceneCode));
    }

    private boolean isFundSubscribeScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase();
        return upper.contains("FUND_SUBSCRIBE")
                || upper.contains(FundProductCodes.AICASH + "_TRANSFER_IN");
    }

    private boolean isFundIncomeSettleScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("FUND_INCOME_SETTLE")
                || (upper.contains("FUND") && upper.contains("INCOME"));
    }

    private RecentWalletFlowContext buildRecentWalletFlowContext(List<TradeOrder> recentTrades, Long currentUserId) {
        Map<Long, UserWalletFlowProfile> userProfileMap = new LinkedHashMap<>();
        Map<String, RedPacketOrder> fundingRedPacketByTradeOrderNo = new LinkedHashMap<>();
        Map<String, RedPacketOrder> claimRedPacketByTradeOrderNo = new LinkedHashMap<>();
        Map<Long, Boolean> lookupUserIds = new LinkedHashMap<>();
        Map<String, Boolean> fundingTradeOrderNoLookup = new LinkedHashMap<>();
        Map<String, Boolean> claimTradeOrderNoLookup = new LinkedHashMap<>();
        if (recentTrades == null || recentTrades.isEmpty()) {
            return new RecentWalletFlowContext(userProfileMap, fundingRedPacketByTradeOrderNo, claimRedPacketByTradeOrderNo);
        }

        for (TradeOrder tradeOrder : recentTrades) {
            Long counterpartyUserId = resolveCounterpartyUserId(tradeOrder, currentUserId);
            if (counterpartyUserId != null) {
                lookupUserIds.put(counterpartyUserId, Boolean.TRUE);
            }
            String businessSceneCode = normalizeOptional(tradeOrder.getBusinessSceneCode());
            if (RED_PACKET_SEND_SCENE.equalsIgnoreCase(businessSceneCode)) {
                fundingTradeOrderNoLookup.put(tradeOrder.getTradeOrderNo(), Boolean.TRUE);
            } else if (RED_PACKET_CLAIM_SCENE.equalsIgnoreCase(businessSceneCode)) {
                claimTradeOrderNoLookup.put(tradeOrder.getTradeOrderNo(), Boolean.TRUE);
            }
        }

        if (!fundingTradeOrderNoLookup.isEmpty()) {
            for (RedPacketOrder redPacketOrder : redPacketOrderRepository.findByFundingTradeNos(new ArrayList<>(fundingTradeOrderNoLookup.keySet()))) {
                String fundingTradeOrderNo = normalizeOptional(redPacketOrder.getFundingTradeNo());
                if (fundingTradeOrderNo == null) {
                    continue;
                }
                fundingRedPacketByTradeOrderNo.put(fundingTradeOrderNo, redPacketOrder);
                if (redPacketOrder.getReceiverUserId() != null) {
                    lookupUserIds.put(redPacketOrder.getReceiverUserId(), Boolean.TRUE);
                }
            }
        }
        if (!claimTradeOrderNoLookup.isEmpty()) {
            for (RedPacketOrder redPacketOrder : redPacketOrderRepository.findByClaimTradeNos(new ArrayList<>(claimTradeOrderNoLookup.keySet()))) {
                String claimTradeOrderNo = normalizeOptional(redPacketOrder.getClaimTradeNo());
                if (claimTradeOrderNo == null) {
                    continue;
                }
                claimRedPacketByTradeOrderNo.put(claimTradeOrderNo, redPacketOrder);
                if (redPacketOrder.getSenderUserId() != null) {
                    lookupUserIds.put(redPacketOrder.getSenderUserId(), Boolean.TRUE);
                }
            }
        }

        if (!lookupUserIds.isEmpty()) {
            try {
                List<UserProfileDTO> profiles = userFacade.listProfiles(new ArrayList<>(lookupUserIds.keySet()));
                for (UserProfileDTO profile : profiles) {
                    if (profile == null || profile.userId() == null) {
                        continue;
                    }
                    userProfileMap.put(
                            profile.userId(),
                            new UserWalletFlowProfile(
                                    normalizeOptional(profile.nickname()),
                                    normalizeOptional(profile.avatarUrl())
                            )
                    );
                }
            } catch (RuntimeException ignored) {
                // 资料查询仅用于展示增强，不影响余额明细主链路。
            }
        }
        return new RecentWalletFlowContext(userProfileMap, fundingRedPacketByTradeOrderNo, claimRedPacketByTradeOrderNo);
    }

    private String resolveUserNickname(Long userId, Map<Long, UserWalletFlowProfile> userProfileMap) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (userProfileMap == null) {
            return null;
        }
        UserWalletFlowProfile userProfile = userProfileMap.get(userId);
        return userProfile == null ? null : normalizeOptional(userProfile.nickname());
    }

    private String resolveUserAvatarUrl(Long userId, Map<Long, UserWalletFlowProfile> userProfileMap) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (userProfileMap == null) {
            return null;
        }
        UserWalletFlowProfile userProfile = userProfileMap.get(userId);
        return userProfile == null ? null : normalizeOptional(userProfile.avatarUrl());
    }

    private record RecentWalletFlowContext(
            /** 用户资料MAP信息 */
            Map<Long, UserWalletFlowProfile> userProfileMap,
            /** fundingREDpacketBY交易订单单号 */
            Map<String, RedPacketOrder> fundingRedPacketByTradeOrderNo,
            /** claimREDpacketBY交易订单单号 */
            Map<String, RedPacketOrder> claimRedPacketByTradeOrderNo
    ) {
    }

    private record UserWalletFlowProfile(
            /** 昵称 */
            String nickname,
            /** 头像地址 */
            String avatarUrl
    ) {
    }

    private String formatSignedMoney(Money money) {
        BigDecimal normalized = money.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00";
        }
        return normalized.toPlainString();
    }

    private String buildTradeOrderNo(TradeType tradeType, Long userId) {
        return aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_TRADE,
                aiPayBizTypeRegistry.tradeBizType(tradeType),
                String.valueOf(requirePositive(userId, "payerUserId"))
        );
    }

    private String defaultScene(String candidate, String fallback) {
        String normalized = normalizeOptional(candidate);
        return normalized == null ? fallback : normalized;
    }

    private String defaultPaymentMethod(String candidate, TradeType tradeType) {
        String normalized = normalizeOptional(candidate);
        return normalized == null ? tradeType.name() : normalized;
    }

    private String defaultCurrency(String candidate) {
        String normalized = normalizeOptional(candidate);
        return normalized == null ? DEFAULT_CURRENCY : normalized.toUpperCase();
    }

    private Money defaultZero(Money value) {
        if (value == null) {
            return zeroMoney(CurrencyUnit.of(DEFAULT_CURRENCY)).rounded(2, RoundingMode.HALF_UP);
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private TradeOrderDTO toTradeOrderDTO(TradeOrder tradeOrder) {
        return toTradeOrderDTO(tradeOrder, true);
    }

    private TradeOrderDTO toTradeOrderDTO(TradeOrder tradeOrder, boolean includeFlowSteps) {
        return toTradeOrderDTO(tradeOrder, includeFlowSteps, null);
    }

    private TradeOrderDTO toTradeOrderDTO(TradeOrder tradeOrder,
                                          boolean includeFlowSteps,
                                          TradeBusinessIndex businessIndexOverride) {
        List<TradeFlowStepDTO> flowSteps = List.of();
        List<TradePayAttemptDTO> payAttempts = List.of();
        TradePayAttemptDTO currentPayAttempt = null;
        if (includeFlowSteps) {
            flowSteps = tradeRepository.findFlowSteps(tradeOrder.getTradeOrderNo())
                    .stream()
                    .map(step -> new TradeFlowStepDTO(
                            step.getStepCode().name(),
                            step.getStepStatus().name(),
                            step.getRequestPayload(),
                            step.getResponsePayload(),
                            step.getErrorMessage(),
                            step.getStartedAt(),
                            step.getFinishedAt(),
                            step.getCreatedAt(),
                            step.getUpdatedAt()
                    ))
                    .toList();
            payAttempts = loadTradePayAttempts(tradeOrder);
            currentPayAttempt = findCurrentTradePayAttempt(payAttempts, tradeOrder.getPayOrderNo());
        }

        String businessDomainCode = tradeOrder.getBusinessDomainCode();
        String bizOrderNo = tradeOrder.getBizOrderNo();
        if (businessIndexOverride != null) {
            businessDomainCode = businessIndexOverride.getBusinessDomainCode().name();
            bizOrderNo = businessIndexOverride.getBizOrderNo();
        }

        Money originalAmount = tradeOrder.getOriginalAmount();
        Money feeAmount = tradeOrder.getFeeAmount();
        Money payableAmount = tradeOrder.getPayableAmount();
        Money settleAmount = tradeOrder.getSettleAmount();
        return new TradeOrderDTO(
                tradeOrder.getTradeOrderNo(),
                tradeOrder.getRequestNo(),
                tradeOrder.getTradeType().name(),
                tradeOrder.getBusinessSceneCode(),
                businessDomainCode,
                bizOrderNo,
                tradeOrder.getOriginalTradeOrderNo(),
                tradeOrder.getPayerUserId(),
                tradeOrder.getPayeeUserId(),
                tradeOrder.getPaymentMethod(),
                originalAmount,
                feeAmount,
                payableAmount,
                settleAmount,
                new TradeSplitPlanDTO(
                        tradeOrder.getWalletDebitAmount(),
                        tradeOrder.getFundDebitAmount(),
                        tradeOrder.getCreditDebitAmount(),
                        tradeOrder.getInboundDebitAmount()
                ),
                tradeOrder.getPricingQuoteNo(),
                tradeOrder.getPayOrderNo(),
                currentPayAttempt == null ? null : currentPayAttempt.attemptNo(),
                currentPayAttempt == null ? null : currentPayAttempt.statusVersion(),
                currentPayAttempt == null ? null : currentPayAttempt.resultCode(),
                currentPayAttempt == null ? null : currentPayAttempt.resultMessage(),
                payAttempts.size(),
                tradeOrder.getStatus().name(),
                tradeOrder.getFailureReason(),
                tradeOrder.getMetadata(),
                tradeOrder.getPaymentToolSnapshot(),
                tradeOrder.getCreatedAt(),
                tradeOrder.getUpdatedAt(),
                payAttempts,
                flowSteps
        );
    }

    private List<TradePayAttemptDTO> loadTradePayAttempts(TradeOrder tradeOrder) {
        String tradeOrderNo = normalizeOptional(tradeOrder.getTradeOrderNo());
        if (tradeOrderNo == null) {
            return List.of();
        }

        List<TradePayAttemptDTO> attempts = Optional.ofNullable(payClient.queryBySourceBiz(PAY_SOURCE_BIZ_TYPE_TRADE, tradeOrderNo))
                .orElse(List.of())
                .stream()
                .map(this::toTradePayAttemptDTO)
                .toList();

        String currentPayOrderNo = normalizeOptional(tradeOrder.getPayOrderNo());
        if (currentPayOrderNo == null || containsTradePayAttempt(attempts, currentPayOrderNo)) {
            return attempts;
        }

        TradePayOrderSnapshot currentPayOrder = payClient.queryByPayOrderNo(currentPayOrderNo);
        if (currentPayOrder == null) {
            return attempts;
        }

        List<TradePayAttemptDTO> enrichedAttempts = new ArrayList<>();
        enrichedAttempts.add(toTradePayAttemptDTO(currentPayOrder));
        enrichedAttempts.addAll(attempts);
        return enrichedAttempts;
    }

    private boolean containsTradePayAttempt(List<TradePayAttemptDTO> attempts, String payOrderNo) {
        String normalizedPayOrderNo = normalizeOptional(payOrderNo);
        if (normalizedPayOrderNo == null || attempts == null || attempts.isEmpty()) {
            return false;
        }
        for (TradePayAttemptDTO attempt : attempts) {
            if (attempt != null && normalizedPayOrderNo.equalsIgnoreCase(normalizeOptional(attempt.payOrderNo()))) {
                return true;
            }
        }
        return false;
    }

    private TradePayAttemptDTO findCurrentTradePayAttempt(List<TradePayAttemptDTO> attempts, String payOrderNo) {
        String normalizedPayOrderNo = normalizeOptional(payOrderNo);
        if (normalizedPayOrderNo == null || attempts == null || attempts.isEmpty()) {
            return null;
        }
        for (TradePayAttemptDTO attempt : attempts) {
            if (attempt != null && normalizedPayOrderNo.equalsIgnoreCase(normalizeOptional(attempt.payOrderNo()))) {
                return attempt;
            }
        }
        return null;
    }

    private TradePayAttemptDTO toTradePayAttemptDTO(TradePayOrderSnapshot payOrder) {
        List<TradePayParticipantDTO> participants = payOrder.participants() == null
                ? List.of()
                : payOrder.participants().stream().map(this::toTradePayParticipantDTO).toList();
        return new TradePayAttemptDTO(
                payOrder.attemptNo(),
                payOrder.payOrderNo(),
                payOrder.bizOrderNo(),
                payOrder.status(),
                payOrder.statusVersion(),
                payOrder.resultCode(),
                payOrder.resultMessage(),
                payOrder.failureReason(),
                payOrder.originalAmount(),
                payOrder.discountAmount(),
                payOrder.payableAmount(),
                payOrder.actualPaidAmount(),
                payOrder.createdAt(),
                payOrder.updatedAt(),
                participants
        );
    }

    private TradePayParticipantDTO toTradePayParticipantDTO(TradePayParticipantSnapshot participant) {
        return new TradePayParticipantDTO(
                participant.participantType(),
                participant.branchId(),
                participant.participantResourceId(),
                participant.status(),
                participant.requestPayload(),
                participant.responseMessage(),
                participant.createdAt(),
                participant.updatedAt()
        );
    }

    private String toPayload(Map<String, String> values) {
        Map<String, String> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = normalizeOptional(entry.getKey());
            String value = normalizeOptional(entry.getValue());
            if (key != null && value != null) {
                filtered.put(key, value);
            }
        }
        if (filtered.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : filtered.entrySet()) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    private Money normalizeAmount(Money value, String fieldName) {
        if (value == null || value.compareTo(zeroOf(value)) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String fieldName, CurrencyUnit currencyUnit) {
        if (value == null) {
            return zeroMoney(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!value.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(fieldName + " currency must equal payableAmount currency");
        }
        if (value.compareTo(zeroOf(value)) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
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

    private String logScene(String sceneCode, String fallback) {
        String normalized = normalizeOptional(sceneCode);
        return normalized == null ? fallback : normalized;
    }

    private String truncateErrorMessage(String raw) {
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

    private Money zeroMoney(CurrencyUnit currencyUnit) {
        return Money.zero(currencyUnit == null ? CurrencyUnit.of(DEFAULT_CURRENCY) : currencyUnit);
    }

    private Money zeroOf(Money value) {
        return Money.zero(value.getCurrencyUnit());
    }

    private Money convertCurrency(Money value, CurrencyUnit currencyUnit) {
        if (value.getCurrencyUnit().equals(currencyUnit)) {
            return value.rounded(2, RoundingMode.HALF_UP);
        }
        return Money.of(currencyUnit, value.getAmount(), RoundingMode.HALF_UP).rounded(2, RoundingMode.HALF_UP);
    }

    private <T> T logStepDuration(String requestNo, String tradeOrderNo, String stepName, Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            T result = supplier.get();
            log.info(
                    "[{}]入参：{}",
                    logScene(stepName, "链路步骤"),
                    "requestNo=" + requestNo
                            + ", tradeOrderNo=" + tradeOrderNo
                            + ", step=" + stepName
                            + ", costMs=" + ((System.nanoTime() - start) / 1_000_000)
            );
            return result;
        } catch (RuntimeException ex) {
            log.warn(
                    "trade perf requestNo={} tradeOrderNo={} step={} failed costMs={} message={}",
                    requestNo,
                    tradeOrderNo,
                    stepName,
                    (System.nanoTime() - start) / 1_000_000,
                    truncateErrorMessage(ex.getMessage())
            );
            throw ex;
        }
    }

    private void logStepDuration(String requestNo, String tradeOrderNo, String stepName, Runnable runnable) {
        logStepDuration(requestNo, tradeOrderNo, stepName, () -> {
            runnable.run();
            return null;
        });
    }

    private record TradeExecutionRequest(
            /** 交易类型 */
            TradeType tradeType,
            /** 请求幂等号 */
            String requestNo,
            /** 业务场景编码 */
            String businessSceneCode,
            /** 付款方用户ID */
            Long payerUserId,
            /** 收款方用户ID */
            Long payeeUserId,
            /** 支付方式编码 */
            String paymentMethod,
            /** 金额 */
            Money amount,
            /** 原始交易订单单号 */
            String originalTradeOrderNo,
            /** 钱包debit金额 */
            Money walletDebitAmount,
            /** 资金debit金额 */
            Money fundDebitAmount,
            /** 信用debit金额 */
            Money creditDebitAmount,
            /** 入金debit金额 */
            Money inboundDebitAmount,
            /** 优惠券单号 */
            String couponNo,
            /** 扩展信息 */
            String metadata
    ) {
    }

    private record WalletFlowPresentation(
            /** counterparty用户ID */
            Long counterpartyUserId,
            /** counterparty昵称 */
            String counterpartyNickname,
            /** counterparty头像地址 */
            String counterpartyAvatarUrl,
            /** 展示标题信息 */
            String displayTitle
    ) {
    }

}
