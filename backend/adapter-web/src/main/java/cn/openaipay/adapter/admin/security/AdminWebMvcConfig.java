package cn.openaipay.adapter.admin.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 后台管理WebMvc配置类
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {

    /** AdminSecurityInterceptor组件 */
    private final AdminSecurityInterceptor adminSecurityInterceptor;

    public AdminWebMvcConfig(AdminSecurityInterceptor adminSecurityInterceptor) {
        this.adminSecurityInterceptor = adminSecurityInterceptor;
    }

    /**
     * 处理ADD信息。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSecurityInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/**");
    }
}
