package cn.openaipay.application.gateway.bank;

/**
 * 银行网关签名异常
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class BankGatewaySignatureException extends RuntimeException {

    public BankGatewaySignatureException(String message) {
        super(message);
    }
}
