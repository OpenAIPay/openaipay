package cn.openaipay.adapter.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.joda.money.Money;

/**
 * Money序列化器
 *
 * @author: tenggk.ai
 * @date: 2026/03/06
 */
public class MoneyJsonSerializer extends JsonSerializer<Money> {

    /**
     * 处理业务数据。
     */
    @Override
    public void serialize(Money value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("amount", value.getAmount().toPlainString());
        generator.writeStringField("currencyCode", value.getCurrencyUnit().getCode());
        generator.writeObjectFieldStart("currencyUnit");
        generator.writeStringField("code", value.getCurrencyUnit().getCode());
        generator.writeEndObject();
        generator.writeEndObject();
    }
}
