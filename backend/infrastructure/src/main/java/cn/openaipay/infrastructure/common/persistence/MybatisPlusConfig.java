package cn.openaipay.infrastructure.common.persistence;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 *
 * 业务场景：资金账户、交易、后台RBAC等聚合在并发更新时依赖乐观锁字段保障数据一致性，
 * 该配置在全局启用MyBatis-Plus拦截器并注册乐观锁插件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 处理业务数据。
     *
     * @return MybatisPlusInterceptor实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
