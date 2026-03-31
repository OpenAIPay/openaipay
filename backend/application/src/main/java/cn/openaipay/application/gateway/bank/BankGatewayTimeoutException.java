package cn.openaipay.application.gateway.bank;

/**
 * 银行网关超时异常
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class BankGatewayTimeoutException extends RuntimeException {

    public BankGatewayTimeoutException(String message) {
        super(message);
    }
}
