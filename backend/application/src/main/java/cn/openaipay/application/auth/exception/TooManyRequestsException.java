package cn.openaipay.application.auth.exception;

/**
 * 请求过于频繁异常。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
