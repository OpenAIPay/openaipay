package cn.openaipay.application.inbound.service.impl;

import cn.openaipay.application.inbound.command.CancelInboundDepositCommand;
import cn.openaipay.application.inbound.command.SubmitInboundDepositCommand;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;
import cn.openaipay.application.inbound.port.InboundOrderReadPort;
import cn.openaipay.application.inbound.service.InboundService;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.inbound.client.GatewayDepositCancelRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositClient;
import cn.openaipay.domain.inbound.client.GatewayDepositConfirmRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositInitiateRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositQueryRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositResultSnapshot;
import cn.openaipay.domain.inbound.model.InboundStatus;
import cn.openaipay.domain.inbound.model.InboundOrder;
import cn.openaipay.domain.inbound.repository.InboundOrderRepository;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * 入金应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class InboundServiceImpl implements InboundService {
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

    /** 入金订单信息 */
    private final InboundOrderRepository inboundOrderRepository;
    /** 入金只读查询 */
    private final InboundOrderReadPort inboundOrderReadPort;
    /** 网关信息 */
    private final GatewayDepositClient gatewayDepositClient;
    /** AI支付ID */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AI支付业务类型 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;

    public InboundServiceImpl(InboundOrderRepository inboundOrderRepository,
                              InboundOrderReadPort inboundOrderReadPort,
                              GatewayDepositClient gatewayDepositClient,
                              AiPayIdGenerator aiPayIdGenerator,
                              AiPayBizTypeRegistry aiPayBizTypeRegistry) {
        this.inboundOrderRepository = inboundOrderRepository;
        this.inboundOrderReadPort = inboundOrderReadPort;
        this.gatewayDepositClient = gatewayDepositClient;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
    }

    /**
     * 查询概览。
     */
    @Override
    @Transactional(readOnly = true)
    public InboundOrderOverviewDTO getOverview() {
        return inboundOrderReadPort.getOverview();
    }

    /**
     * 查询订单列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> listOrders(String inboundId,
                                            String requestBizNo,
                                            String payOrderNo,
                                            String inboundStatus,
                                            Integer pageNo,
                                            Integer pageSize) {
        return inboundOrderReadPort.listOrders(
                inboundId,
                requestBizNo,
                payOrderNo,
                inboundStatus,
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    /**
     * 提交业务数据。
     */
    @Override
    @Transactional
    public InboundOrderDTO submitDeposit(SubmitInboundDepositCommand command) {
        validateSubmitCommand(command);
        String requestBizNo = normalizeRequired(command.requestBizNo(), "requestBizNo");
        InboundOrder existingOrder = inboundOrderRepository.findByRequestBizNo(requestBizNo).orElse(null);
        if (existingOrder != null) {
            return toDTO(resumeSubmit(existingOrder, command));
        }

        Long payerUserId = requirePositive(command.payerUserId(), "payerUserId");
        Money amount = normalizePositiveAmount(command.amount(), "amount");
        LocalDateTime now = LocalDateTime.now();

        String inboundId = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_INBOUND,
                aiPayBizTypeRegistry.inboundOrderCreateBizType(),
                String.valueOf(payerUserId)
        );
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        String payOrderNo = normalizeRequired(command.payOrderNo(), "payOrderNo");

        InboundOrder order = InboundOrder.createForDeposit(
                inboundId,
                null,
                instChannelCode,
                null,
                normalizeRequired(command.payerAccountNo(), "payerAccountNo"),
                amount,
                amount,
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
    @Transactional
    public InboundOrderDTO cancelDeposit(CancelInboundDepositCommand command) {
        String inboundId = normalizeRequired(command.inboundId(), "inboundId");
        InboundOrder order = mustGet(inboundId);
        if (order.getInboundStatus() == InboundStatus.CANCELED) {
            return toDTO(order);
        }
        GatewayDepositResultSnapshot queryResult = queryDeposit(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        if (queryState == GatewayQueryState.SUCCEEDED) {
            return toDTO(inboundOrderRepository.save(markSucceededFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.CANCELED) {
            return toDTO(inboundOrderRepository.save(markCanceledFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.FAILED) {
            return toDTO(inboundOrderRepository.save(markFailedFromQuery(order, queryResult)));
        }
        if (queryState == GatewayQueryState.RECON_PENDING) {
            order.markReconPending(
                    CODE_BANK_TIMEOUT,
                    "银行状态待确认，暂不撤销",
                    queryResult == null ? LocalDateTime.now() : queryResult.gmtResp(),
                    LocalDateTime.now()
            );
            return toDTO(inboundOrderRepository.save(order));
        }
        GatewayDepositResultSnapshot gatewayResult = gatewayDepositClient.cancelDeposit(new GatewayDepositCancelRequest(
                order.getInboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode"),
                normalizeOptional(command.reason())
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
        return toDTO(inboundOrderRepository.save(order));
    }

    /**
     * 按入金ID查询记录。
     */
    @Override
    @Transactional(readOnly = true)
    public InboundOrderDTO queryByInboundId(String inboundId) {
        return toDTO(mustGet(normalizeRequired(inboundId, "inboundId")));
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    @Transactional(readOnly = true)
    public InboundOrderDTO queryByRequestBizNo(String requestBizNo) {
        InboundOrder order = inboundOrderRepository.findByRequestBizNo(normalizeRequired(requestBizNo, "requestBizNo"))
                .orElseThrow(() -> new NoSuchElementException("inbound order not found for requestBizNo: " + requestBizNo));
        return toDTO(order);
    }

    private InboundOrder resumeSubmit(InboundOrder order, SubmitInboundDepositCommand command) {
        if (order.getInboundStatus() == InboundStatus.SUCCEEDED
                || order.getInboundStatus() == InboundStatus.FAILED
                || order.getInboundStatus() == InboundStatus.CANCELED) {
            return order;
        }
        if (order.getInboundStatus() == InboundStatus.ACCEPTED) {
            return confirmAccepted(order);
        }
        if (order.getInboundStatus() == InboundStatus.RECON_PENDING) {
            return reconcilePending(order, command);
        }
        return initiateAndConfirm(order, command);
    }

    private InboundOrder initiateAndConfirm(InboundOrder order, SubmitInboundDepositCommand command) {
        Long payerUserId = requirePositive(command.payerUserId(), "payerUserId");
        LocalDateTime now = LocalDateTime.now();
        if (order.getGmtSubmit() == null) {
            order.markSubmitted(now);
            order = inboundOrderRepository.save(order);
        }

        GatewayDepositResultSnapshot gatewayResult = gatewayDepositClient.initiateDeposit(new GatewayDepositInitiateRequest(
                order.getInboundId(),
                order.getInstChannelCode(),
                payerUserId,
                order.getPayerAccountNo(),
                order.getInboundAmount(),
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
            order = inboundOrderRepository.save(order);
            return confirmAccepted(order);
        }
        if (shouldMarkReconPending(gatewayResult)) {
            order.markReconPending(
                    defaultValue(gatewayResult.resultCode(), CODE_BANK_TIMEOUT),
                    defaultValue(gatewayResult.resultDescription(), "银行受理结果待确认"),
                    gatewayResult.gmtResp(),
                    LocalDateTime.now()
            );
            order = inboundOrderRepository.save(order);
            return reconcilePending(order, command);
        }
        order.markFailed(
                defaultValue(gatewayResult.resultCode(), "FAILED"),
                defaultValue(gatewayResult.resultDescription(), "银行受理失败"),
                gatewayResult.gmtResp(),
                LocalDateTime.now()
        );
        return inboundOrderRepository.save(order);
    }

    private InboundOrder confirmAccepted(InboundOrder order) {
        if (order.getInboundStatus() == InboundStatus.SUCCEEDED
                || order.getInboundStatus() == InboundStatus.FAILED
                || order.getInboundStatus() == InboundStatus.CANCELED) {
            return order;
        }
        GatewayDepositResultSnapshot queryResult = queryDeposit(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        if (queryState == GatewayQueryState.SUCCEEDED) {
            return inboundOrderRepository.save(markSucceededFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.CANCELED) {
            return inboundOrderRepository.save(markCanceledFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.FAILED) {
            return inboundOrderRepository.save(markFailedFromQuery(order, queryResult));
        }
        if (queryState == GatewayQueryState.RECON_PENDING) {
            order.markReconPending(
                    defaultValue(queryResult.resultCode(), CODE_BANK_TIMEOUT),
                    defaultValue(queryResult.resultDescription(), "银行状态待确认"),
                    queryResult.gmtResp(),
                    LocalDateTime.now()
            );
            return inboundOrderRepository.save(order);
        }
        GatewayDepositResultSnapshot gatewayResult = gatewayDepositClient.confirmDeposit(new GatewayDepositConfirmRequest(
                order.getInboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode")
        ));
        if (gatewayResult.success()) {
            order.markSucceeded(
                    gatewayResult.inboundOrderNo(),
                    defaultValue(gatewayResult.resultCode(), "SUCCESS"),
                    defaultValue(gatewayResult.resultDescription(), "银行入账成功"),
                    gatewayResult.gmtSettle(),
                    LocalDateTime.now()
            );
        } else {
            if (shouldMarkReconPending(gatewayResult)) {
                order.markReconPending(
                        defaultValue(gatewayResult.resultCode(), CODE_BANK_TIMEOUT),
                        defaultValue(gatewayResult.resultDescription(), "银行入账结果待确认"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            } else {
                order.markFailed(
                        defaultValue(gatewayResult.resultCode(), "FAILED"),
                        defaultValue(gatewayResult.resultDescription(), "银行入账失败"),
                        gatewayResult.gmtResp(),
                        LocalDateTime.now()
                );
            }
        }
        return inboundOrderRepository.save(order);
    }

    private InboundOrder reconcilePending(InboundOrder order, SubmitInboundDepositCommand command) {
        GatewayDepositResultSnapshot queryResult = queryDeposit(order);
        GatewayQueryState queryState = resolveGatewayQueryState(queryResult);
        return switch (queryState) {
            case SUCCEEDED -> inboundOrderRepository.save(markSucceededFromQuery(order, queryResult));
            case CANCELED -> inboundOrderRepository.save(markCanceledFromQuery(order, queryResult));
            case FAILED -> inboundOrderRepository.save(markFailedFromQuery(order, queryResult));
            case ACCEPTED -> {
                order.markAccepted(
                        defaultValue(queryResult.resultCode(), "ACCEPTED"),
                        defaultValue(queryResult.resultDescription(), "银行已受理"),
                        defaultValue(queryResult.instId(), order.getInstId()),
                        defaultValue(queryResult.instChannelCode(), order.getInstChannelCode()),
                        queryResult.gmtResp(),
                        LocalDateTime.now()
                );
                order = inboundOrderRepository.save(order);
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
                yield inboundOrderRepository.save(order);
            }
        };
    }

    private InboundOrder mustGet(String inboundId) {
        return inboundOrderRepository.findByInboundId(inboundId)
                .orElseThrow(() -> new NoSuchElementException("inbound order not found: " + inboundId));
    }

    private InboundOrderDTO toDTO(InboundOrder order) {
        return new InboundOrderDTO(
                order.getInboundId(),
                order.getRequestBizNo(),
                order.getBizOrderNo(),
                order.getTradeOrderNo(),
                order.getPayOrderNo(),
                order.getPayerAccountNo(),
                order.getInboundAmount(),
                order.getAccountAmount(),
                order.getSettleAmount(),
                order.getInboundStatus().name(),
                order.getResultCode(),
                order.getResultDescription(),
                order.getInstId(),
                order.getInstChannelCode(),
                order.getInboundOrderNo(),
                order.getPayChannelCode(),
                order.getGmtSubmit(),
                order.getGmtResp(),
                order.getGmtSettle(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private void validateSubmitCommand(SubmitInboundDepositCommand command) {
        normalizeRequired(command.requestBizNo(), "requestBizNo");
        normalizeRequired(command.bizOrderNo(), "bizOrderNo");
        normalizeRequired(command.payOrderNo(), "payOrderNo");
        requirePositive(command.payerUserId(), "payerUserId");
        normalizeRequired(command.payerAccountNo(), "payerAccountNo");
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

    private GatewayDepositResultSnapshot queryDeposit(InboundOrder order) {
        return gatewayDepositClient.queryDeposit(new GatewayDepositQueryRequest(
                order.getInboundId(),
                normalizeRequired(order.getInstChannelCode(), "instChannelCode")
        ));
    }

    private boolean shouldMarkReconPending(GatewayDepositResultSnapshot gatewayResult) {
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

    private GatewayQueryState resolveGatewayQueryState(GatewayDepositResultSnapshot queryResult) {
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

    private InboundOrder markSucceededFromQuery(InboundOrder order, GatewayDepositResultSnapshot queryResult) {
        order.markSucceeded(
                queryResult.inboundOrderNo(),
                defaultValue(queryResult.resultCode(), "SUCCESS"),
                defaultValue(queryResult.resultDescription(), "银行入账成功"),
                queryResult.gmtSettle(),
                LocalDateTime.now()
        );
        return order;
    }

    private InboundOrder markCanceledFromQuery(InboundOrder order, GatewayDepositResultSnapshot queryResult) {
        order.markCanceled(
                defaultValue(queryResult.resultCode(), "CANCELED"),
                defaultValue(queryResult.resultDescription(), "银行已撤销"),
                queryResult.gmtResp(),
                LocalDateTime.now()
        );
        return order;
    }

    private InboundOrder markFailedFromQuery(InboundOrder order, GatewayDepositResultSnapshot queryResult) {
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
