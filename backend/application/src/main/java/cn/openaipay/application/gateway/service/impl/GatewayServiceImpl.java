package cn.openaipay.application.gateway.service.impl;

import cn.openaipay.application.gateway.bank.BankGatewaySignatureException;
import cn.openaipay.application.gateway.bank.BankGatewayTimeoutException;
import cn.openaipay.application.gateway.command.GatewayDepositCancelCommand;
import cn.openaipay.application.gateway.command.GatewayDepositConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayDepositInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayDepositQueryCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawCancelCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayDepositResultDTO;
import cn.openaipay.application.gateway.dto.GatewayWithdrawResultDTO;
import cn.openaipay.application.gateway.port.BankDepositCancelRequest;
import cn.openaipay.application.gateway.port.BankDepositConfirmRequest;
import cn.openaipay.application.gateway.port.BankDepositInitiateRequest;
import cn.openaipay.application.gateway.port.BankDepositQueryRequest;
import cn.openaipay.application.gateway.port.BankDepositResult;
import cn.openaipay.application.gateway.port.BankGatewayPort;
import cn.openaipay.application.gateway.port.BankWithdrawCancelRequest;
import cn.openaipay.application.gateway.port.BankWithdrawConfirmRequest;
import cn.openaipay.application.gateway.port.BankWithdrawInitiateRequest;
import cn.openaipay.application.gateway.port.BankWithdrawQueryRequest;
import cn.openaipay.application.gateway.port.BankWithdrawResult;
import cn.openaipay.application.gateway.service.GatewayService;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * 银行网关应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class GatewayServiceImpl implements GatewayService {
    /** 银行客户端 */
    private final BankGatewayPort bankGatewayPort;
    /** 最大重试次数 */
    private final int retryTimes;
    /** 重试间隔毫秒 */
    private final long retryBackoffMs;

    public GatewayServiceImpl(BankGatewayPort bankGatewayPort,
                                         @Value("${aipay.gateway.retry-times:2}") int retryTimes,
                                         @Value("${aipay.gateway.retry-backoff-ms:80}") long retryBackoffMs) {
        this.bankGatewayPort = bankGatewayPort;
        this.retryTimes = Math.max(0, retryTimes);
        this.retryBackoffMs = Math.max(0L, retryBackoffMs);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayDepositResultDTO initiateDeposit(GatewayDepositInitiateCommand command) {
        validateInitiateCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithRetry(
                () -> toGatewayDepositResultDTO(bankGatewayPort.initiateDeposit(toBankDepositInitiateRequest(command))),
                instId,
                instChannelCode,
                "受理超时",
                "受理失败"
        );
    }

    /**
     * 查询业务数据。
     */
    @Override
    public GatewayDepositResultDTO queryDeposit(GatewayDepositQueryCommand command) {
        validateQueryCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithRetry(
                () -> toGatewayDepositResultDTO(bankGatewayPort.queryDeposit(toBankDepositQueryRequest(command))),
                instId,
                instChannelCode,
                "查单超时",
                "查单失败"
        );
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayDepositResultDTO confirmDeposit(GatewayDepositConfirmCommand command) {
        validateConfirmCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithRetry(
                () -> toGatewayDepositResultDTO(bankGatewayPort.confirmDeposit(toBankDepositConfirmRequest(command))),
                instId,
                instChannelCode,
                "确认超时",
                "确认失败"
        );
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayDepositResultDTO cancelDeposit(GatewayDepositCancelCommand command) {
        validateCancelCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithRetry(
                () -> toGatewayDepositResultDTO(bankGatewayPort.cancelDeposit(toBankDepositCancelRequest(command))),
                instId,
                instChannelCode,
                "撤销超时",
                "撤销失败"
        );
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO initiateWithdraw(GatewayWithdrawInitiateCommand command) {
        validateWithdrawInitiateCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithdrawWithRetry(
                () -> toGatewayWithdrawResultDTO(bankGatewayPort.initiateWithdraw(toBankWithdrawInitiateRequest(command))),
                instId,
                instChannelCode,
                "受理超时",
                "受理失败"
        );
    }

    /**
     * 查询业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO queryWithdraw(GatewayWithdrawQueryCommand command) {
        validateWithdrawQueryCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithdrawWithRetry(
                () -> toGatewayWithdrawResultDTO(bankGatewayPort.queryWithdraw(toBankWithdrawQueryRequest(command))),
                instId,
                instChannelCode,
                "查单超时",
                "查单失败"
        );
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO confirmWithdraw(GatewayWithdrawConfirmCommand command) {
        validateWithdrawConfirmCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithdrawWithRetry(
                () -> toGatewayWithdrawResultDTO(bankGatewayPort.confirmWithdraw(toBankWithdrawConfirmRequest(command))),
                instId,
                instChannelCode,
                "确认超时",
                "确认失败"
        );
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO cancelWithdraw(GatewayWithdrawCancelCommand command) {
        validateWithdrawCancelCommand(command);
        String instId = normalizeInstId(command.instChannelCode());
        String instChannelCode = normalizeRequired(command.instChannelCode(), "instChannelCode");
        return executeWithdrawWithRetry(
                () -> toGatewayWithdrawResultDTO(bankGatewayPort.cancelWithdraw(toBankWithdrawCancelRequest(command))),
                instId,
                instChannelCode,
                "撤销超时",
                "撤销失败"
        );
    }

    private GatewayDepositResultDTO executeWithRetry(Supplier<GatewayDepositResultDTO> action,
                                                     String instId,
                                                     String instChannelCode,
                                                     String timeoutMessage,
                                                     String defaultFailureMessage) {
        int attempts = retryTimes + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (BankGatewayTimeoutException ex) {
                if (attempt == attempts) {
                    return fail("BANK_TIMEOUT", timeoutMessage, instId, instChannelCode);
                }
                sleepBeforeRetry(attempt);
            } catch (BankGatewaySignatureException ex) {
                return fail("BANK_SIGN_ERROR", defaultValue(ex.getMessage(), "签名校验失败"), instId, instChannelCode);
            } catch (RuntimeException ex) {
                return fail("BANK_CALL_ERROR", buildFailureMessage(defaultFailureMessage, ex.getMessage()), instId, instChannelCode);
            }
        }
        return fail("BANK_TIMEOUT", timeoutMessage, instId, instChannelCode);
    }

    private GatewayWithdrawResultDTO executeWithdrawWithRetry(Supplier<GatewayWithdrawResultDTO> action,
                                                              String instId,
                                                              String instChannelCode,
                                                              String timeoutMessage,
                                                              String defaultFailureMessage) {
        int attempts = retryTimes + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (BankGatewayTimeoutException ex) {
                if (attempt == attempts) {
                    return failWithdraw("BANK_TIMEOUT", timeoutMessage, instId, instChannelCode);
                }
                sleepBeforeRetry(attempt);
            } catch (BankGatewaySignatureException ex) {
                return failWithdraw("BANK_SIGN_ERROR", defaultValue(ex.getMessage(), "签名校验失败"), instId, instChannelCode);
            } catch (RuntimeException ex) {
                return failWithdraw("BANK_CALL_ERROR", buildFailureMessage(defaultFailureMessage, ex.getMessage()), instId, instChannelCode);
            }
        }
        return failWithdraw("BANK_TIMEOUT", timeoutMessage, instId, instChannelCode);
    }

    private void sleepBeforeRetry(int attempt) {
        if (retryBackoffMs <= 0L) {
            return;
        }
        long sleepMs = retryBackoffMs * attempt;
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("gateway retry interrupted");
        }
    }

    private String buildFailureMessage(String defaultFailureMessage, String detailMessage) {
        String detail = normalizeOptional(detailMessage);
        if (detail == null) {
            return defaultFailureMessage;
        }
        return defaultFailureMessage + ": " + detail;
    }

    private GatewayDepositResultDTO fail(String resultCode,
                                         String resultDescription,
                                         String instId,
                                         String instChannelCode) {
        return new GatewayDepositResultDTO(
                false,
                resultCode,
                resultDescription,
                instId,
                null,
                null,
                instChannelCode,
                null,
                LocalDateTime.now(),
                null
        );
    }

    private GatewayWithdrawResultDTO failWithdraw(String resultCode,
                                                  String resultDescription,
                                                  String instId,
                                                  String instChannelCode) {
        return new GatewayWithdrawResultDTO(
                false,
                resultCode,
                resultDescription,
                instId,
                null,
                null,
                instChannelCode,
                null,
                LocalDateTime.now(),
                null
        );
    }

    private BankDepositInitiateRequest toBankDepositInitiateRequest(GatewayDepositInitiateCommand command) {
        return new BankDepositInitiateRequest(
                command.inboundId(),
                command.instChannelCode(),
                command.payerUserId(),
                command.payerAccountNo(),
                command.amount(),
                command.payChannelCode(),
                command.requestIdentify(),
                command.bizIdentity()
        );
    }

    private BankDepositConfirmRequest toBankDepositConfirmRequest(GatewayDepositConfirmCommand command) {
        return new BankDepositConfirmRequest(command.inboundId(), command.instChannelCode());
    }

    private BankDepositQueryRequest toBankDepositQueryRequest(GatewayDepositQueryCommand command) {
        return new BankDepositQueryRequest(command.inboundId(), command.instChannelCode());
    }

    private BankDepositCancelRequest toBankDepositCancelRequest(GatewayDepositCancelCommand command) {
        return new BankDepositCancelRequest(
                command.inboundId(),
                command.instChannelCode(),
                command.reason()
        );
    }

    private BankWithdrawInitiateRequest toBankWithdrawInitiateRequest(GatewayWithdrawInitiateCommand command) {
        return new BankWithdrawInitiateRequest(
                command.outboundId(),
                command.instChannelCode(),
                command.payerUserId(),
                command.payeeAccountNo(),
                command.amount(),
                command.payChannelCode(),
                command.requestIdentify(),
                command.bizIdentity()
        );
    }

    private BankWithdrawConfirmRequest toBankWithdrawConfirmRequest(GatewayWithdrawConfirmCommand command) {
        return new BankWithdrawConfirmRequest(command.outboundId(), command.instChannelCode());
    }

    private BankWithdrawQueryRequest toBankWithdrawQueryRequest(GatewayWithdrawQueryCommand command) {
        return new BankWithdrawQueryRequest(command.outboundId(), command.instChannelCode());
    }

    private BankWithdrawCancelRequest toBankWithdrawCancelRequest(GatewayWithdrawCancelCommand command) {
        return new BankWithdrawCancelRequest(
                command.outboundId(),
                command.instChannelCode(),
                command.reason()
        );
    }

    private GatewayDepositResultDTO toGatewayDepositResultDTO(BankDepositResult result) {
        return new GatewayDepositResultDTO(
                result.success(),
                result.resultCode(),
                result.resultDescription(),
                result.instId(),
                result.instSerialNo(),
                result.instRefNo(),
                result.instChannelCode(),
                result.inboundOrderNo(),
                result.gmtResp(),
                result.gmtSettle()
        );
    }

    private GatewayWithdrawResultDTO toGatewayWithdrawResultDTO(BankWithdrawResult result) {
        return new GatewayWithdrawResultDTO(
                result.success(),
                result.resultCode(),
                result.resultDescription(),
                result.instId(),
                result.instSerialNo(),
                result.instRefNo(),
                result.instChannelCode(),
                result.outboundOrderNo(),
                result.gmtResp(),
                result.gmtSettle()
        );
    }

    private void validateInitiateCommand(GatewayDepositInitiateCommand command) {
        normalizeRequired(command.inboundId(), "inboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
        if (command.payerUserId() == null || command.payerUserId() <= 0) {
            throw new IllegalArgumentException("payerUserId must be greater than 0");
        }
        normalizeRequired(command.payerAccountNo(), "payerAccountNo");
        Money amount = command.amount();
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        normalizeRequired(command.payChannelCode(), "payChannelCode");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
        normalizeRequired(command.requestIdentify(), "requestIdentify");
        normalizeRequired(command.bizIdentity(), "bizIdentity");
    }

    private void validateConfirmCommand(GatewayDepositConfirmCommand command) {
        normalizeRequired(command.inboundId(), "inboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private void validateQueryCommand(GatewayDepositQueryCommand command) {
        normalizeRequired(command.inboundId(), "inboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private void validateCancelCommand(GatewayDepositCancelCommand command) {
        normalizeRequired(command.inboundId(), "inboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private void validateWithdrawInitiateCommand(GatewayWithdrawInitiateCommand command) {
        normalizeRequired(command.outboundId(), "outboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
        if (command.payerUserId() == null || command.payerUserId() <= 0) {
            throw new IllegalArgumentException("payerUserId must be greater than 0");
        }
        normalizeRequired(command.payeeAccountNo(), "payeeAccountNo");
        Money amount = command.amount();
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        normalizeRequired(command.payChannelCode(), "payChannelCode");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
        normalizeRequired(command.requestIdentify(), "requestIdentify");
        normalizeRequired(command.bizIdentity(), "bizIdentity");
    }

    private void validateWithdrawConfirmCommand(GatewayWithdrawConfirmCommand command) {
        normalizeRequired(command.outboundId(), "outboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private void validateWithdrawQueryCommand(GatewayWithdrawQueryCommand command) {
        normalizeRequired(command.outboundId(), "outboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private void validateWithdrawCancelCommand(GatewayWithdrawCancelCommand command) {
        normalizeRequired(command.outboundId(), "outboundId");
        normalizeRequired(command.instChannelCode(), "instChannelCode");
    }

    private String normalizeInstId(String instChannelCode) {
        return normalizeRequired(instChannelCode, "instChannelCode").toUpperCase(Locale.ROOT);
    }

    private String defaultValue(String raw, String fallback) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? fallback : normalized;
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
}
