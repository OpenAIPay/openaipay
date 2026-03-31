package cn.openaipay.adapter.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

/**
 * Jackson金额配置测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/06
 */
class JacksonMoneyConfigTest {

    /** 金额module信息 */
    private final Module moneyModule = new JacksonMoneyConfig().moneyModule();
    /** object映射器信息 */
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(moneyModule);

    @Test
    void shouldSerializeLargeMoneyWithoutOverflow() throws Exception {
        MoneyHolder holder = new MoneyHolder(Money.parse("CNY 66666666.00"));

        String json = objectMapper.writeValueAsString(holder);

        assertThat(json).contains("\"amount\":\"66666666.00\"");
        assertThat(json).contains("\"currencyCode\":\"CNY\"");
        assertThat(json).contains("\"currencyUnit\":{\"code\":\"CNY\"}");
        assertThat(json).doesNotContain("amountMinorInt");
        assertThat(json).doesNotContain("amountMajorInt");
    }

    @Test
    void shouldDeserializeMoneyFromCompatiblePayload() throws Exception {
        String json = "{\"amount\":\"88.66\",\"currencyCode\":\"CNY\"}";

        Money value = objectMapper.readValue(json, Money.class);

        assertThat(value.getAmount()).isEqualByComparingTo(new BigDecimal("88.66"));
        assertThat(value.getCurrencyUnit().getCode()).isEqualTo("CNY");
    }

    private record MoneyHolder(
        /** 金额 */
        Money amount
    ) {
    }
}
