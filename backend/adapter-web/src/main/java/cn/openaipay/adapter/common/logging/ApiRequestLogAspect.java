package cn.openaipay.adapter.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 接口入参与异常日志切面。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Aspect
@Component
public class ApiRequestLogAspect {

    /** LOG信息。 */
    private static final Logger log = LoggerFactory.getLogger(ApiRequestLogAspect.class);
    /** 指标记录器。 */
    private final ApiRequestMetricsRecorder apiRequestMetricsRecorder;

    public ApiRequestLogAspect(ApiRequestMetricsRecorder apiRequestMetricsRecorder) {
        this.apiRequestMetricsRecorder = apiRequestMetricsRecorder;
    }

    /**
     * 处理控制器层日志。
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        HttpServletRequest request = currentRequest();
        Object requestArg = ApiSceneLogSupport.selectRequestArg(joinPoint.getArgs());
        String scene = ApiSceneLogSupport.resolveScene(request, requestArg, joinPoint.getSignature().getName());
        String requestPayload = ApiSceneLogSupport.buildRequestPayload(request, requestArg);
        if (request != null) {
            request.setAttribute(ApiSceneLogSupport.ATTR_SCENE, scene);
            request.setAttribute(ApiSceneLogSupport.ATTR_REQUEST_PAYLOAD, requestPayload);
        }

        log.info("[{}]入参：{}", scene, requestPayload);
        try {
            Object response = joinPoint.proceed();
            apiRequestMetricsRecorder.record(scene, true, System.nanoTime() - startNanos);
            return response;
        } catch (Throwable ex) {
            apiRequestMetricsRecorder.record(scene, false, System.nanoTime() - startNanos);
            log.error("[{}]业务异常, request:{}", scene, requestPayload, ex);
            ApiSceneLogSupport.markBizErrorLogged(request);
            throw ex;
        }
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest();
    }
}
