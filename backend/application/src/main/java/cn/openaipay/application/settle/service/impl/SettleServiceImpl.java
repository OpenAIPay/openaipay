package cn.openaipay.application.settle.service.impl;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxPublisher;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.facade.PricingFacade;
import cn.openaipay.application.settle.command.SettleCommittedTradeCommand;
import cn.openaipay.application.settle.dto.SettleResultDTO;
import cn.openaipay.application.settle.async.SettleAccountingEventRequestedPayload;
import cn.openaipay.application.settle.service.SettleService;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import cn.openaipay.application.walletaccount.dto.WalletTccBranchDTO;
import cn.openaipay.domain.settle.service.SettleDomainService;
import cn.openaipay.domain.settle.service.SettlePlan;
import cn.openaipay.domain.settle.service.SettlePlanStatus;
import cn.openaipay.domain.settle.service.SettleRequest;
import cn.openaipay.domain.settle.service.SettleWalletAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 结算应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class SettleServiceImpl implements SettleService {

    /** 结算域信息 */
    private final SettleDomainService settleDomainService;
    /** 计费信息 */
    private final PricingFacade pricingFacade;
    /** 钱包信息 */
    private final WalletAccountFacade walletAccountFacade;
    /** 异步消息信息 */
    private final OutboxPublisher outboxPublisher;
    /** AI支付ID */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AI支付业务类型 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;
    /** 手续费用户ID */
    private final Long platformFeeUserId;

    public SettleServiceImpl(SettleDomainService settleDomainService,
                             PricingFacade pricingFacade,
                             WalletAccountFacade walletAccountFacade,
                             OutboxPublisher outboxPublisher,
                             AiPayIdGenerator aiPayIdGenerator,
                             AiPayBizTypeRegistry aiPayBizTypeRegistry,
                             @Value("${aipay.settle.platform-fee-user-id:880921068428800021}") Long platformFeeUserId) {
        this.settleDomainService = settleDomainService;
        this.pricingFacade = pricingFacade;
        this.walletAccountFacade = walletAccountFacade;
        this.outboxPublisher = outboxPublisher;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
        this.platformFeeUserId = platformFeeUserId;
    }

    /**
     * 处理结算交易信息。
     */
    @Override
    public SettleResultDTO settleCommittedTrade(SettleCommittedTradeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        String settleBizNo = resolveSettleBizNo(command);
        if (settleBizNo == null) {
            return SettleResultDTO.reconPending("settle business key missing");
        }

        PricingQuoteDTO pricingQuote = loadPricingQuote(command.pricingQuoteNo());
        if (command.pricingQuoteNo() != null && pricingQuote == null) {
            return SettleResultDTO.reconPending("pricing quote not found: " + normalizeMessage(command.pricingQuoteNo()));
        }

        SettlePlan plan = settleDomainService.resolveCommittedTradePlan(new SettleRequest(
                command.tradeType(),
                command.payerUserId(),
                command.payeeUserId(),
                settleBizNo,
                command.settleAmount(),
                pricingQuote == null ? null : pricingQuote.feeAmount(),
                command.originalAmount(),
                command.payableAmount(),
                command.shouldCreditPayee(),
                pricingQuote == null ? null : pricingQuote.feeBearer(),
                normalizePositive(platformFeeUserId)
        ));

        if (plan.status() == SettlePlanStatus.NO_ACTION) {
            return SettleResultDTO.success();
        }
        if (plan.status() == SettlePlanStatus.RECON_PENDING) {
            return SettleResultDTO.reconPending(normalizeMessage(plan.message()));
        }
        SettleResultDTO result = executePlan(plan, settleBizNo);
        if ("SUCCESS".equals(result.status())) {
            publishAccountingEventRequested(command, settleBizNo);
        }
        return result;
    }

    private SettleResultDTO executePlan(SettlePlan plan, String settleBizNo) {
        ExecutionResult primaryResult = executeWalletActions(
                buildExecutionXid("PRIMARY", settleBizNo),
                plan.primaryActions()
        );
        if (primaryResult.success()) {
            return SettleResultDTO.success();
        }
        if (plan.compensationActions().isEmpty()
                || primaryResult.confirmedCount() > 0
                || !primaryResult.cancelCompleted()) {
            return SettleResultDTO.reconPending(primaryResult.message());
        }
        return executeCompensation(plan, settleBizNo, primaryResult.message());
    }

    private SettleResultDTO executeCompensation(SettlePlan plan, String settleBizNo, String primaryErrorMessage) {
        ExecutionResult compensationResult = executeWalletActions(
                buildExecutionXid("COMPENSATE", settleBizNo),
                plan.compensationActions()
        );
        if (compensationResult.success()) {
            if (plan.compensationSuccessIsFailure()) {
                return SettleResultDTO.failed(plan.compensationSuccessMessagePrefix() + ": " + normalizeMessage(primaryErrorMessage));
            }
            return SettleResultDTO.success();
        }
        return SettleResultDTO.reconPending(
                normalizeMessage(primaryErrorMessage) + "; compensate failed: " + normalizeMessage(compensationResult.message())
        );
    }

    private ExecutionResult executeWalletActions(String xid, List<SettleWalletAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return ExecutionResult.succeeded(0);
        }

        List<PreparedWalletAction> preparedActions = new ArrayList<>();
        for (SettleWalletAction action : actions) {
            String branchId = buildWalletBranchId(action.tradeBizType(), action.userId());
            try {
                walletAccountFacade.tccTry(
                        xid,
                        branchId,
                        action.userId(),
                        action.operationType(),
                        "SETTLEMENT_HOLD",
                        action.amount(),
                        action.settleBizNo()
                );
                preparedActions.add(new PreparedWalletAction(action, branchId));
            } catch (RuntimeException ex) {
                boolean cancelCompleted = cancelPreparedActions(xid, preparedActions);
                return ExecutionResult.failed(
                        action.failureMessagePrefix() + ": " + compactError(ex.getMessage()),
                        0,
                        cancelCompleted
                );
            }
        }

        int confirmedCount = 0;
        for (int index = 0; index < preparedActions.size(); index++) {
            PreparedWalletAction preparedAction = preparedActions.get(index);
            try {
                walletAccountFacade.tccConfirm(xid, preparedAction.branchId());
                confirmedCount++;
            } catch (RuntimeException ex) {
                boolean cancelCompleted = cancelPreparedActions(xid, preparedActions.subList(index, preparedActions.size()));
                return ExecutionResult.failed(
                        preparedAction.action().failureMessagePrefix() + ": " + compactError(ex.getMessage()),
                        confirmedCount,
                        cancelCompleted
                );
            }
        }
        return ExecutionResult.succeeded(confirmedCount);
    }

    private boolean cancelPreparedActions(String xid, List<PreparedWalletAction> preparedActions) {
        if (preparedActions == null || preparedActions.isEmpty()) {
            return true;
        }
        boolean cancelCompleted = true;
        List<PreparedWalletAction> reverseOrder = new ArrayList<>(preparedActions);
        Collections.reverse(reverseOrder);
        for (PreparedWalletAction preparedAction : reverseOrder) {
            try {
                WalletTccBranchDTO canceledBranch = walletAccountFacade.tccCancel(
                        xid,
                        preparedAction.branchId(),
                        preparedAction.action().userId(),
                        preparedAction.action().operationType(),
                        "SETTLEMENT_HOLD",
                        preparedAction.action().amount(),
                        preparedAction.action().settleBizNo()
                );
                if (canceledBranch == null || !"CANCELED".equalsIgnoreCase(canceledBranch.branchStatus())) {
                    cancelCompleted = false;
                }
            } catch (RuntimeException ignored) {
                cancelCompleted = false;
            }
        }
        return cancelCompleted;
    }

    private String buildWalletBranchId(String tradeBizType, Long userId) {
        return aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_WALLET_ACCOUNT,
                aiPayBizTypeRegistry.tradeBizType(normalizeRequired(tradeBizType, "tradeBizType")),
                String.valueOf(requirePositive(userId, "userId"))
        );
    }

    private String resolveSettleBizNo(SettleCommittedTradeCommand command) {
        String payOrderNo = normalizeOptional(command.payOrderNo());
        if (payOrderNo != null) {
            return payOrderNo;
        }
        return normalizeOptional(command.tradeOrderNo());
    }

    private PricingQuoteDTO loadPricingQuote(String pricingQuoteNo) {
        String normalizedQuoteNo = normalizeOptional(pricingQuoteNo);
        if (normalizedQuoteNo == null) {
            return null;
        }
        try {
            return pricingFacade.getQuote(normalizedQuoteNo);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String buildExecutionXid(String phase, String settleBizNo) {
        return "settle-" + normalizeRequired(phase, "phase").toLowerCase() + ':'
                + normalizeRequired(settleBizNo, "settleBizNo");
    }

    private void publishAccountingEventRequested(SettleCommittedTradeCommand command, String settleBizNo) {
        String normalizedSettleBizNo = normalizeRequired(settleBizNo, "settleBizNo");
        String messageKey = normalizedSettleBizNo + ":SUCCESS";
        SettleAccountingEventRequestedPayload payload = new SettleAccountingEventRequestedPayload(
                command.tradeType(),
                command.payerUserId(),
                command.payeeUserId(),
                command.payOrderNo(),
                command.requestNo(),
                command.tradeOrderNo(),
                normalizedSettleBizNo,
                command.settleAmount(),
                command.originalAmount(),
                command.payableAmount(),
                command.shouldCreditPayee()
        );
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.SETTLE_ACCOUNTING_EVENT_REQUESTED,
                messageKey,
                payload.toPayload(),
                20
        );
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private Long normalizePositive(Long value) {
        return value == null || value <= 0 ? null : value;
    }

    private String normalizeRequired(String rawValue, String fieldName) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String trimmed = rawValue.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeMessage(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        return normalized == null ? "unknown" : normalized;
    }

    private String compactError(String message) {
        String normalized = normalizeMessage(message);
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private record PreparedWalletAction(
        /** 处理动作 */
        SettleWalletAction action,
        /** 分支ID */
        String branchId
    ) {
    }

    private record ExecutionResult(
        /** 是否成功 */
        boolean success,
        /** 消息内容 */
        String message,
        /** confirmed数量 */
        int confirmedCount,
        /** cancelcompleted信息 */
        boolean cancelCompleted
    ) {
        private static ExecutionResult succeeded(int confirmedCount) {
            return new ExecutionResult(true, null, confirmedCount, true);
        }

        private static ExecutionResult failed(String message, int confirmedCount, boolean cancelCompleted) {
            return new ExecutionResult(false, message, confirmedCount, cancelCompleted);
        }
    }
}
