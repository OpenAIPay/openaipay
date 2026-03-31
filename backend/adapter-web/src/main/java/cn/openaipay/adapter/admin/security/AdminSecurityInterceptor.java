package cn.openaipay.adapter.admin.security;

import cn.openaipay.application.admin.facade.AdminRbacFacade;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.domain.admin.model.AdminAccount;
import cn.openaipay.domain.admin.repository.AdminRepository;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 后台管理安全拦截器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {

    /** Authorization常量。 */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    /** 管理标识header常量 */
    private static final String ADMIN_ID_HEADER = "X-Admin-Id";

    /** AdminRepository组件 */
    private final AdminRepository adminRepository;
    /** AdminRbacFacade组件 */
    private final AdminRbacFacade adminRbacFacade;
    /** CredentialDomainService组件 */
    private final CredentialDomainService credentialDomainService;

    public AdminSecurityInterceptor(AdminRepository adminRepository,
                                    AdminRbacFacade adminRbacFacade,
                                    CredentialDomainService credentialDomainService) {
        this.adminRepository = adminRepository;
        this.adminRbacFacade = adminRbacFacade;
        this.credentialDomainService = credentialDomainService;
    }

    /**
     * 处理PRE信息。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Long tokenAdminId = credentialDomainService
                .resolveSubjectIdFromAuthorizationHeader(request.getHeader(AUTHORIZATION_HEADER));
        if (tokenAdminId == null || tokenAdminId <= 0) {
            throw new UnauthorizedException("后台鉴权失败：缺少有效 Bearer Token");
        }

        Long assertedAdminId = resolveAssertedAdminId(request);
        if (assertedAdminId != null && !assertedAdminId.equals(tokenAdminId)) {
            throw new UnauthorizedException("后台鉴权失败：管理员身份与令牌不匹配");
        }
        Long adminId = tokenAdminId;
        if (adminId == null || adminId <= 0) {
            throw new UnauthorizedException("后台鉴权失败：缺少有效管理员身份");
        }

        AdminAccount account = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new UnauthorizedException("后台鉴权失败：管理员不存在"));
        if (!account.isActive()) {
            throw new UnauthorizedException("后台鉴权失败：管理员账号已禁用");
        }

        request.setAttribute(AdminRequestContext.ATTR_ADMIN_ID, adminId);
        request.setAttribute(AdminRequestContext.ATTR_ADMIN_USERNAME, account.getUsername());

        String requiredPermission = resolvePermission(handlerMethod);
        if (requiredPermission != null && !adminRbacFacade.hasPermission(adminId, requiredPermission)) {
            throw new ForbiddenException("权限不足，缺少权限：" + requiredPermission);
        }
        return true;
    }

    private Long resolveAssertedAdminId(HttpServletRequest request) {
        Long fromHeader = parseLong(request.getHeader(ADMIN_ID_HEADER));
        if (fromHeader != null) {
            return fromHeader;
        }
        return parseLong(request.getParameter("adminId"));
    }

    private String resolvePermission(HandlerMethod handlerMethod) {
        RequireAdminPermission methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                RequireAdminPermission.class
        );
        if (methodAnnotation != null && hasText(methodAnnotation.value())) {
            return methodAnnotation.value().trim();
        }

        RequireAdminPermission classAnnotation = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(),
                RequireAdminPermission.class
        );
        if (classAnnotation != null && hasText(classAnnotation.value())) {
            return classAnnotation.value().trim();
        }
        return null;
    }

    private Long parseLong(String text) {
        if (!hasText(text)) {
            return null;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("后台鉴权失败：管理员标识格式错误");
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}
