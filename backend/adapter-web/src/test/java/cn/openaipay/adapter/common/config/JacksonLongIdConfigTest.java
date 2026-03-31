package cn.openaipay.adapter.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson长整型ID配置测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class JacksonLongIdConfigTest {

    private final Jackson2ObjectMapperBuilderCustomizer customizer = new JacksonLongIdConfig().longIdAsStringCustomizer();
    private final ObjectMapper objectMapper = buildObjectMapper();

    @Test
    void shouldSerializeLongIdAsString() throws Exception {
        LongIdHolder holder = new LongIdHolder(880109000000000001L, 7L, 2, 3.14D);

        String json = objectMapper.writeValueAsString(holder);

        assertThat(json).contains("\"userId\":\"880109000000000001\"");
        assertThat(json).contains("\"version\":\"7\"");
    }

    @Test
    void shouldKeepIntAndDecimalAsNumber() throws Exception {
        LongIdHolder holder = new LongIdHolder(880109000000000001L, 7L, 2, 3.14D);

        String json = objectMapper.writeValueAsString(holder);

        assertThat(json).contains("\"count\":2");
        assertThat(json).contains("\"amount\":3.14");
    }

    private ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        customizer.customize(builder);
        return builder.build();
    }

    private record LongIdHolder(
        /** 用户标识 */
        Long userId,
        /** 版本号 */
        long version,
        /** 条数 */
        int count,
        /** 金额 */
        double amount
    ) {
    }
}
