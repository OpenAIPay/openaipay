package cn.openaipay.adapter.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * Money反序列化器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class MoneyJsonDeserializer extends JsonDeserializer<Money> {

    /** 默认信息 */
    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");

    /**
     * 处理业务数据。
     */
    @Override
    public Money deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return Money.of(DEFAULT_CURRENCY, node.decimalValue(), RoundingMode.HALF_UP);
        }
        if (node.isTextual()) {
            return parseText(node.asText(), parser);
        }
        if (node.isObject()) {
            return parseObject(node, parser);
        }
        throw JsonMappingException.from(parser, "money format is not supported");
    }

    private Money parseText(String raw, JsonParser parser) throws JsonMappingException {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw JsonMappingException.from(parser, "money text must not be blank");
        }
        String[] segments = text.split("\\s+");
        if (segments.length == 2) {
            CurrencyUnit currency = parseCurrency(segments[0], parser);
            return Money.of(currency, parseDecimal(segments[1], parser), RoundingMode.HALF_UP);
        }
        return Money.of(DEFAULT_CURRENCY, parseDecimal(text, parser), RoundingMode.HALF_UP);
    }

    private Money parseObject(JsonNode node, JsonParser parser) throws JsonMappingException {
        String currencyCode = textOrNull(node.get("currencyCode"));
        if (currencyCode == null) {
            JsonNode currencyUnit = node.get("currencyUnit");
            if (currencyUnit != null && currencyUnit.isObject()) {
                currencyCode = textOrNull(currencyUnit.get("code"));
            }
        }
        CurrencyUnit currency = currencyCode == null ? DEFAULT_CURRENCY : parseCurrency(currencyCode, parser);
        JsonNode amountNode = node.get("amount");
        if (amountNode == null || amountNode.isNull()) {
            throw JsonMappingException.from(parser, "money.amount must not be null");
        }
        return Money.of(currency, parseDecimal(amountNode, parser), RoundingMode.HALF_UP);
    }

    private CurrencyUnit parseCurrency(String raw, JsonParser parser) throws JsonMappingException {
        String currencyCode = raw == null ? "" : raw.trim().toUpperCase();
        if (currencyCode.isEmpty()) {
            throw JsonMappingException.from(parser, "currencyCode must not be blank");
        }
        try {
            return CurrencyUnit.of(currencyCode);
        } catch (IllegalArgumentException ex) {
            throw JsonMappingException.from(parser, "invalid currencyCode: " + currencyCode, ex);
        }
    }

    private BigDecimal parseDecimal(JsonNode node, JsonParser parser) throws JsonMappingException {
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            return parseDecimal(node.asText(), parser);
        }
        throw JsonMappingException.from(parser, "amount must be number or text");
    }

    private BigDecimal parseDecimal(String raw, JsonParser parser) throws JsonMappingException {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw JsonMappingException.from(parser, "amount must not be blank");
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            throw JsonMappingException.from(parser, "invalid amount: " + text, ex);
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }
}
