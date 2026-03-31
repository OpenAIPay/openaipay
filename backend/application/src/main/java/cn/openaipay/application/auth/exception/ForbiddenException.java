package cn.openaipay.application.auth.exception;

/**
 * Forbidden异常
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
