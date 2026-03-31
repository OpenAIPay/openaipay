package cn.openaipay.application.auth.exception;

/**
 * Unauthorized异常
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
