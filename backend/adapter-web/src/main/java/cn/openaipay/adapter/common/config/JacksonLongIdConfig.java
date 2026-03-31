package cn.openaipay.adapter.common.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson长整型ID序列化配置
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Configuration
public class JacksonLongIdConfig {

    /**
     * 统一将Long/long序列化为字符串，避免前端丢失精度。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longIdAsStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
