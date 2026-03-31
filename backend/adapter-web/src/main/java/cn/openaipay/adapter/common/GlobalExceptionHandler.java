package cn.openaipay.adapter.common;

import cn.openaipay.adapter.common.logging.ApiSceneLogSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.auth.exception.TooManyRequestsException;
import cn.openaipay.application.auth.exception.UnauthorizedException;

import java.util.NoSuchElementException;

/**
 * 全局异常Handler模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "服务器开小差，请稍后再试";
    private static final String DATABASE_UNAVAILABLE_MESSAGE = "服务暂不可用，请稍后重试";
    private static final String DATA_ACCESS_ERROR_MESSAGE = "数据服务暂不可用，请稍后重试";

    /**
     * 处理NOT信息。
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        String localizedMessage = ErrorMessageLocalizer.localize(ex.getMessage());
        String code = resolveNotFoundCode(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(code, localizedMessage));
    }

    /**
     * 处理BAD请求。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_ARGUMENT", ErrorMessageLocalizer.localize(ex.getMessage())));
    }

    /**
     * 处理非法状态请求。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_STATE", ErrorMessageLocalizer.localize(ex.getMessage())));
    }

    /**
     * 处理业务数据。
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("UNAUTHORIZED", ErrorMessageLocalizer.localize(ex.getMessage())));
    }

    /**
     * 处理业务数据。
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("FORBIDDEN", ErrorMessageLocalizer.localize(ex.getMessage())));
    }

    /**
     * 处理请求频率过高。
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.fail("TOO_MANY_REQUESTS", ErrorMessageLocalizer.localize(ex.getMessage())));
    }

    /**
     * 处理业务数据。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("invalid request");
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", ErrorMessageLocalizer.localize(msg)));
    }

    /**
     * 请求体媒体类型不支持。
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                        HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.fail("UNSUPPORTED_MEDIA_TYPE", "请求内容类型不支持，请使用 application/json"));
    }

    /**
     * 数据库连接不可用。
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseUnavailable(CannotGetJdbcConnectionException ex,
                                                                       HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("DATABASE_UNAVAILABLE", DATABASE_UNAVAILABLE_MESSAGE));
    }

    /**
     * 数据访问异常。
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        if (containsCause(ex, CannotGetJdbcConnectionException.class)) {
            logBusinessException(request, ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail("DATABASE_UNAVAILABLE", DATABASE_UNAVAILABLE_MESSAGE));
        }
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("DATA_ACCESS_ERROR", DATA_ACCESS_ERROR_MESSAGE));
    }

    /**
     * 处理业务数据。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        logBusinessException(request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("INTERNAL_ERROR", INTERNAL_ERROR_MESSAGE));
    }

    private void logBusinessException(HttpServletRequest request, Exception ex) {
        if (ApiSceneLogSupport.isBizErrorLogged(request)) {
            return;
        }
        String scene = ApiSceneLogSupport.resolveScene(request, null, "通用接口");
        String requestPayload = ApiSceneLogSupport.buildRequestPayload(request, null);
        log.error("[{}]业务异常, request:{}", scene, requestPayload, ex);
        ApiSceneLogSupport.markBizErrorLogged(request);
    }

    private boolean containsCause(Throwable throwable, Class<? extends Throwable> targetType) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (targetType.isInstance(cursor)) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private String resolveNotFoundCode(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.toLowerCase();
        if (message.contains("user not found")) {
            return "USER_NOT_FOUND";
        }
        return "RESOURCE_NOT_FOUND";
    }
}
