package cn.openaipay.adapter.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * C端用户鉴权WebMvc配置。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Configuration
public class UserWebMvcConfig implements WebMvcConfigurer {

    /** UserSecurityInterceptor组件。 */
    private final UserSecurityInterceptor userSecurityInterceptor;

    public UserWebMvcConfig(UserSecurityInterceptor userSecurityInterceptor) {
        this.userSecurityInterceptor = userSecurityInterceptor;
    }

    /**
     * 注册用户鉴权拦截器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSecurityInterceptor)
                .addPathPatterns(
                        "/api/users/**",
                        "/api/kyc/**",
                        "/api/contacts/**",
                        "/api/conversations/**",
                        "/api/messages/**",
                        "/api/cashier/**",
                        "/api/assets/**",
                        "/api/trade/**",
                        "/api/feedback/**",
                        "/api/agreements/**",
                        "/api/bankcards/**",
                        "/api/coupons/**",
                        "/api/credit-accounts/users/**",
                        "/api/loan-accounts/users/**",
                        "/api/fund-accounts/**",
                        "/api/media/images/upload",
                        "/api/media/owners/**"
                )
                .excludePathPatterns(
                        "/api/users/profile-by-login",
                        "/api/fund-accounts/trades/**",
                        "/api/fund-accounts/products/**",
                        "/api/fund-accounts/pay-freeze/**",
                        "/api/fund-accounts/switch/**",
                        "/api/fund-accounts/income/**"
                );
    }
}
