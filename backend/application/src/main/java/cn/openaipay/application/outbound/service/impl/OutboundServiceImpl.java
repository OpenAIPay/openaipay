package cn.openaipay.application.outbound.service.impl;

import cn.openaipay.application.outbound.command.CancelOutboundWithdrawCommand;
import cn.openaipay.application.outbound.command.SubmitOutboundWithdrawCommand;
import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;
import cn.openaipay.application.outbound.port.OutboundOrderReadPort;
import cn.openaipay.application.outbound.service.OutboundService;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.outbound.client.GatewayWithdrawCancelRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawClient;
import cn.openaipay.domain.outbound.client.GatewayWithdrawConfirmRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawInitiateRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawQueryRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawResultSnapshot;
import cn.openaipay.domain.outbound.model.OutboundStatus;
import cn.openaipay.domain.outbound.model.OutboundOrder;
import cn.openaipay.domain.outbound.repository.OutboundOrderRepository;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * 出金应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/06
 */
@Service
public class OutboundServiceImpl implements OutboundService {
    /** 业务身份信息 */
    private static final String DEFAULT_BIZ_IDENTITY = "OPENAIPAY";
    /** 编码银行信息 */
    private static final String CODE_BANK_TIMEOUT = "BANK_TIMEOUT";
    /** 编码银行信息 */
    private static final String CODE_BANK_CALL_ERROR = "BANK_CALL_ERROR";
    /** 编码银行签约信息 */
    private static final String CODE_BANK_SIGN_ERROR = "BANK_SIGN_ERROR";
    /** 默认分页页码。 */
    private static final int DEFAULT_PAGE_NO = 1;
    /** 默认分页条数。 */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** 分页最大条数。 */
    private static final int MAX_PAGE_SIZE = 200;

    /** 出金订单信息 */
    private final OutboundOrderRepository outboundOrderRepository;
    /** 出金只读查询 */
    private final OutboundOrderReadPort outboundOrderReadPort;
    /** 网关信息 */
    private final GatewayWithdrawClient gatewayWithdrawClient;
    /** AI支付ID */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AI支付业务类型 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;

    public OutboundServiceImpl(OutboundOrderRepository outboundOrderRepository,
                               OutboundOrderReadPort outboundOrderReadPort,
                               GatewayWithdrawClient gatewayWithdrawClient,
                               AiPayIdGenerator aiPayIdGenerator,
                               AiPayBizTypeRegistry aiPayBizTypeRegistry) {
        this.outboundOrderRepository = outboundOrderRepository;
        this.outboundOrderReadPort = outboundOrderReadPort;
        this.gatewayWithdrawClient = gatewayWithdrawClient;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
    }

    /**
     * 查询概览。
     */
    @Override
    @Transactional(readOnly = true)
    public OutboundOrderOverviewDTO getOverview() {
        return outboundOrderReadPort.getOverview();
    }

    /**
     * 查询订单列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> listOrders(String outboundId,
                                             String requestBizNo,
                                             String payOrderNo,
                                             String outboundStatus,
                                             Integer pageNo,
                                             Integer pageSize) {
        return outboundOrderReadPort.listOrders(
                outboundId,
                requestBizNo,
                payOrderNo,
                outboundStatus,
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    /**
     * 提交业务数据。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundOrderDTO submitWithdraw(SubmitOutboundWithdrawCommand command) {
        validateSubmitCommand(command);
        String requestBizNo = normalizeRequired(command.requestBizNo(), "requestBizNo");
        OutboundOrder existingOrder = outboundOrderRepository.findByRequestBizNo(requestBizNo).orElse(null);
        if (existingOrder != null) {
            return toDTO(resumeSubmit(existingOrder, command));
        }

        Long payerUserId = requirePositive(command.payerUserId(), "payerUserId");
        Money amount = normalizePositiveAmount(command.amount(), "amount");
        LocalDateTime now = LocalDateTime.now();

        String outboundId = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_OUTBOUND,
                aiPayBizTypeRegistry.outboundOrderCreateBizType(),
                String.valueOf(payerUserId)
        );
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        String payOrderNo = normalizeRequired(command.payOrderNo(), "payOrderNo");

        OutboundOrder order = OutboundOrder.createForWithdraw(
                outboundId,
                null,
                instChannelCode,
                null,
                normalizeRequired(command.payeeAccountNo(), "payeeAccountNo"),
                amount,
                defaultValue(command.requestIdentify(), requestBizNo),
                requestBizNo,
                normalizeRequired(command.bizOrderNo(), "bizOrderNo"),
                normalizeOptional(command.tradeOrderNo()),
                payOrderNo,
                normalizeRequired(command.payChannelCode(), "payChannelCode"),
                defaultValue(command.bizIdentity(), DEFAULT_BIZ_IDENTITY),
                now
        );
        return toDTO(resumeSubmit(order, command));
    }

    /**
     * 取消业务数据。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundOrderDTO cancelWithdraw(CancelOutboundWithdrawCommand command) {
        String outboundId = normalizeRequired(command.outboundId(), "outboundId");
        OutboundOrder order = mustGet(outboundId);
        if (order.getOutboundStatus() == OutboundStatus.CANCELED) {
            return toDTO(order);
        }
        GatewayWithdrawResultSnapshot queryResult = queryWithdraw(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        if (queryState == GatewayQueryState.SUCCEEDED) {
            return toDTO(outboundOrderRepository.save(markSucceededFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.CANCELED) {
            return toDTO(outboundOrderRepository.save(markCanceledFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.FAILED) {
            return toDTO(outboundOrderRepository.save(markFailedFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.RECON_PENDING) {
            order.markReconPending(
                    CODE_BANK_TIMEOUT,
                    "银行状态待确认，暂不撤销",
                    queryResult == null ? LocalDateTime.now() : queryResult.gmtResp(),
                    LocalDateTime.now()
            );
            return toDTO(outboundOrderRepository.save(order));
        }
        GatewayWithdrawResultSnapshot gatewayResult = gatewayWithdrawClient.cancelWithdraw(new GatewayWithdrawCancelRequest(
                order.getOutboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode"),
                command.reason()
        ));
        if (gatewayResult.success()) {
            order.markCanceled(
                    defaultValue(gatewayResult.resultCode(), "SUCCESS"),
                    defaultValue(gatewayResult.resultDescription(), "银行撤销成功"),
                    gatewayResult.gmtResp(),
                    LocalDateTime.now()
            );
        } else {
            if (shouldMarkReconPending(gatewayResult)) {
                order.markReconPending(
                        defaultValue(gatewayResult.resultCode(), CODE_BANK_TIMEOUT),
                        defaultValue(gatewayResult.resultDescription(), "银行撤销结果待确认"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            } else {
                order.markFailed(
                        defaultValue(gatewayResult.resultCode(), "FAILED"),
                        defaultValue(gatewayResult.resultDescription(), "银行撤销失败"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            }
        }
        return toDTO(outboundOrderRepository.save(order));
    }

    /**
     * 按出金ID查询记录。
     */
    @Override
    @Transactional(readOnly = true)
    public OutboundOrderDTO queryByOutboundId(String outboundId) {
        return toDTO(mustGet(normalizeRequired(outboundId, "outboundId")));
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    @Transactional(readOnly = true)
    public OutboundOrderDTO queryByRequestBizNo(String requestBizNo) {
        OutboundOrder order = outboundOrderRepository.findByRequestBizNo(normalizeRequired(requestBizNo, "requestBizNo"))
                .orElseThrow(() -> new NoSuchElementException("outbound order not found for requestBizNo: " + requestBizNo));
        return toDTO(order);
    }

    private OutboundOrder resumeSubmit(OutboundOrder order, SubmitOutboundWithdrawCommand command) {
        if (order.getOutboundStatus() == OutboundStatus.SUCCEEDED
                || order.getOutboundStatus() == OutboundStatus.FAILED
                || order.getOutboundStatus() == OutboundStatus.CANCELED) {
            return order;
        }
        if (order.getOutboundStatus() == OutboundStatus.ACCEPTED) {
            return confirmAccepted(order);
        }
        if (order.getOutboundStatus() == OutboundStatus.RECON_PENDING) {
            return reconcilePending(order, command);
        }
        return initiateAndConfirm(order, command);
    }

    private OutboundOrder initiateAndConfirm(OutboundOrder order, SubmitOutboundWithdrawCommand command) {
        Long payerUserId = requirePositive(command.payerUserId(), "payerUserId");
        LocalDateTime now = LocalDateTime.now();
        if (order.getGmtSubmit() == null) {
            order.markSubmitted(now);
            order = outboundOrderRepository.save(order);
        }

        GatewayWithdrawResultSnapshot gatewayResult = gatewayWithdrawClient.initiateWithdraw(new GatewayWithdrawInitiateRequest(
                order.getOutboundId(),
                order.getInstChannelCode(),
                payerUserId,
                order.getPayeeAccountNo(),
                order.getOutboundAmount(),
                order.getPayChannelCode(),
                order.getRequestIdentify(),
                order.getBizIdentity()
        ));
        if (gatewayResult.success()) {
            order.markAccepted(
                    defaultValue(gatewayResult.resultCode(), "SUCCESS"),
                    defaultValue(gatewayResult.resultDescription(), "银行受理成功"),
                    defaultValue(gatewayResult.instId(), order.getInstId()),
                    defaultValue(gatewayResult.instChannelCode(), order.getInstChannelCode()),
                    gatewayResult.gmtResp(),
                    LocalDateTime.now()
            );
            order = outboundOrderRepository.save(order);
            return confirmAccepted(order);
        }
        if (shouldMarkReconPending(gatewayResult)) {
            order.markReconPending(
                    defaultValue(gatewayResult.resultCode(), CODE_BANK_TIMEOUT),
                    defaultValue(gatewayResult.resultDescription(), "银行受理结果待确认"),
                    gatewayResult.gmtResp(),
                    LocalDateTime.now()
            );
            order = outboundOrderRepository.save(order);
            return reconcilePending(order, command);
        }
        order.markFailed(
                defaultValue(gatewayResult.resultCode(), "FAILED"),
                defaultValue(gatewayResult.resultDescription(), "银行受理失败"),
                gatewayResult.gmtResp(),
                LocalDateTime.now()
        );
        return outboundOrderRepository.save(order);
    }

    private OutboundOrder confirmAccepted(OutboundOrder order) {
        if (order.getOutboundStatus() == OutboundStatus.SUCCEEDED
                || order.getOutboundStatus() == OutboundStatus.FAILED
                || order.getOutboundStatus() == OutboundStatus.CANCELED) {
            return order;
        }
        GatewayWithdrawResultSnapshot queryResult = queryWithdraw(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        if (queryState == GatewayQueryState.SUCCEEDED) {
            return outboundOrderRepository.save(markSucceededFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.CANCELED) {
            return outboundOrderRepository.save(markCanceledFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.FAILED) {
            return outboundOrderRepository.save(markFailedFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.RECON_PENDING) {
            order.markReconPending(
                    defaultValue(queryResult.resultCode(), CODE_BANK_TIMEOUT),
                    defaultValue(queryResult.resultDescription(), "银行状态待确认"),
                    queryResult.gmtResp(),
                    LocalDateTime.now()
            );
            return outboundOrderRepository.save(order);
        }
        GatewayWithdrawResultSnapshot gatewayResult = gatewayWithdrawClient.confirmWithdraw(new GatewayWithdrawConfirmRequest(
                order.getOutboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode")
        ));
        if (gatewayResult.success()) {
            order.markSucceeded(
                    gatewayResult.outboundOrderNo(),
                    defaultValue(gatewayResult.resultCode(), "SUCCESS"),
                    defaultValue(gatewayResult.resultDescription(), "银行出款成功"),
                    gatewayResult.gmtSettle(),
                    LocalDateTime.now()
            );
        } else {
            if (shouldMarkReconPending(gatewayResult)) {
                order.markReconPending(
                        defaultValue(gatewayResult.resultCode(), CODE_BANK_TIMEOUT),
                        defaultValue(gatewayResult.resultDescription(), "银行出款结果待确认"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            } else {
                order.markFailed(
                        defaultValue(gatewayResult.resultCode(), "FAILED"),
                        defaultValue(gatewayResult.resultDescription(), "银行出款失败"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            }
        }
        return outboundOrderRepository.save(order);
    }

    private OutboundOrder reconcilePending(OutboundOrder order, SubmitOutboundWithdrawCommand command) {
        GatewayWithdrawResultSnapshot queryResult = queryWithdraw(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        return switch (queryState) {
            case SUCCEEDED -> outboundOrderRepository.save(markSucceededFromQuery(order, queryResult));
            case CANCELED -> outboundOrderRepository.save(markCanceledFromQuery(order, queryResult));
            case FAILED -> outboundOrderRepository.save(markFailedFromQuery(order, queryResult));
            case ACCEPTED -> {
                order.markAccepted(
                        defaultValue(queryResult.resultCode(), "ACCEPTED"),
                        defaultValue(queryResult.resultDescription(), "银行已受理"),
                        defaultValue(queryResult.instId(), order.getInstId()),
                        defaultValue(queryResult.instChannelCode(), order.getInstChannelCode()),
                        queryResult.gmtResp(),
                        LocalDateTime.now()
                );
                order = outboundOrderRepository.save(order);
                yield confirmAccepted(order);
            }
            case NOT_FOUND -> initiateAndConfirm(order, command);
            case RECON_PENDING -> {
                order.markReconPending(
                        defaultValue(queryResult.resultCode(), CODE_BANK_TIMEOUT),
                        defaultValue(queryResult.resultDescription(), "银行状态待确认"),
                        queryResult.gmtResp(),
                        LocalDateTime.now()
                );
                yield outboundOrderRepository.save(order);
            }
        };
    }

    private OutboundOrder mustGet(String outboundId) {
        return outboundOrderRepository.findByOutboundId(outboundId)
                .orElseThrow(() -> new NoSuchElementException("outbound order not found: " + outboundId));
    }

    private OutboundOrderDTO toDTO(OutboundOrder order) {
        return new OutboundOrderDTO(
                order.getOutboundId(),
                order.getRequestBizNo(),
                order.getBizOrderNo(),
                order.getTradeOrderNo(),
                order.getPayOrderNo(),
                order.getPayeeAccountNo(),
                order.getOutboundAmount(),
                order.getOutboundStatus().name(),
                order.getResultCode(),
                order.getResultDescription(),
                order.getInstId(),
                order.getInstChannelCode(),
                order.getOutboundOrderNo(),
                order.getPayChannelCode(),
                order.getGmtSubmit(),
                order.getGmtResp(),
                order.getGmtSettle(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private void validateSubmitCommand(SubmitOutboundWithdrawCommand command) {
        normalizeRequired(command.requestBizNo(), "requestBizNo");
        normalizeRequired(command.bizOrderNo(), "bizOrderNo");
        normalizeRequired(command.payOrderNo(), "payOrderNo");
        requirePositive(command.payerUserId(), "payerUserId");
        normalizeRequired(command.payeeAccountNo(), "payeeAccountNo");
        normalizePositiveAmount(command.amount(), "amount");
        normalizeRequired(command.payChannelCode(), "payChannelCode");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private Money normalizePositiveAmount(Money amount, String fieldName) {
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
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

    private GatewayWithdrawResultSnapshot queryWithdraw(OutboundOrder order) {
        return gatewayWithdrawClient.queryWithdraw(new GatewayWithdrawQueryRequest(
                order.getOutboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode")
        ));
    }

    private boolean shouldMarkReconPending(GatewayWithdrawResultSnapshot gatewayResult) {
        if (gatewayResult == null || gatewayResult.success()) {
            return false;
        }
        return isGatewayUncertainCode(gatewayResult.resultCode());
    }

    private boolean isGatewayUncertainCode(String resultCode) {
        String normalized = normalizeOptional(resultCode);
        if (normalized == null) {
            return true;
        }
        String upperCode = normalized.toUpperCase(Locale.ROOT);
        return CODE_BANK_TIMEOUT.equals(upperCode)
                || CODE_BANK_CALL_ERROR.equals(upperCode)
                || CODE_BANK_SIGN_ERROR.equals(upperCode);
    }

    private GatewayQueryState resolveGatewayQueryState(GatewayWithdrawResultSnapshot queryResult) {
        if (queryResult == null) {
            return GatewayQueryState.RECON_PENDING;
        }
        String normalized = normalizeOptional(queryResult.resultCode());
        if (normalized == null) {
            return GatewayQueryState.RECON_PENDING;
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "SUCCESS", "SUCCEEDED" -> GatewayQueryState.SUCCEEDED;
            case "ACCEPTED" -> GatewayQueryState.ACCEPTED;
            case "CANCELED" -> GatewayQueryState.CANCELED;
            case "FAILED" -> GatewayQueryState.FAILED;
            case "NOT_FOUND" -> GatewayQueryState.NOT_FOUND;
            case CODE_BANK_TIMEOUT, CODE_BANK_CALL_ERROR, CODE_BANK_SIGN_ERROR -> GatewayQueryState.RECON_PENDING;
            default -> queryResult.success() ? GatewayQueryState.FAILED : GatewayQueryState.RECON_PENDING;
        };
    }

    private OutboundOrder markSucceededFromQuery(OutboundOrder order, GatewayWithdrawResultSnapshot queryResult) {
        order.markSucceeded(
                queryResult.outboundOrderNo(),
                defaultValue(queryResult.resultCode(), "SUCCESS"),
                defaultValue(queryResult.resultDescription(), "银行出款成功"),
                queryResult.gmtSettle(),
                LocalDateTime.now()
        );
        return order;
    }

    private OutboundOrder markCanceledFromQuery(OutboundOrder order, GatewayWithdrawResultSnapshot queryResult) {
        order.markCanceled(
                defaultValue(queryResult.resultCode(), "CANCELED"),
                defaultValue(queryResult.resultDescription(), "银行已撤销"),
                queryResult.gmtResp(),
                LocalDateTime.now()
        );
        return order;
    }

    private OutboundOrder markFailedFromQuery(OutboundOrder order, GatewayWithdrawResultSnapshot queryResult) {
        order.markFailed(
                defaultValue(queryResult.resultCode(), "FAILED"),
                defaultValue(queryResult.resultDescription(), "银行处理失败"),
                queryResult.gmtResp(),
                LocalDateTime.now()
        );
        return order;
    }

    private enum GatewayQueryState {
        SUCCEEDED,
        ACCEPTED,
        CANCELED,
        FAILED,
        NOT_FOUND,
        RECON_PENDING
    }
}
