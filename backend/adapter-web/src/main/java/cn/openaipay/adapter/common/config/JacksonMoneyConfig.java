package cn.openaipay.adapter.common.config;

import cn.openaipay.adapter.common.json.MoneyJsonDeserializer;
import cn.openaipay.adapter.common.json.MoneyJsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.money.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson金额配置
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Configuration
public class JacksonMoneyConfig {

    /**
     * 处理金额模块信息。
     */
    @Bean
    public Module moneyModule() {
        SimpleModule module = new SimpleModule("money-module");
        module.addDeserializer(Money.class, new MoneyJsonDeserializer());
        module.addSerializer(Money.class, new MoneyJsonSerializer());
        return module;
    }
}
