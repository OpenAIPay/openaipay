package cn.openaipay.domain.shared.number;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * RateValue说明。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public final class RateValue extends Number implements Comparable<RateValue> {

    /** 精度常量 */
    public static final int SCALE = 6;
    /** 零值常量。 */
    public static final RateValue ZERO = of(BigDecimal.ZERO);
    /** 单位值常量。 */
    public static final RateValue ONE = of(BigDecimal.ONE);

    /** 值 */
    private final BigDecimal value;

    public RateValue(BigDecimal value) {
        this.value = normalize(requireValue(value));
    }

    public RateValue(String value) {
        this(new BigDecimal(value));
    }

    /**
     * 处理OF信息。
     */
    public static RateValue of(BigDecimal value) {
        return new RateValue(value);
    }

    /**
     * 处理OF信息。
     */
    public static RateValue of(RateValue value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        return value;
    }

    /**
     * 解析业务数据。
     */
    public static RateValue parse(String value) {
        return new RateValue(value);
    }

    /**
     * 处理业务数据。
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RateValue fromJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof RateValue rateValue) {
            return rateValue;
        }
        if (value instanceof Number number) {
            return new RateValue(new BigDecimal(number.toString()));
        }
        return new RateValue(String.valueOf(value));
    }

    /**
     * 处理ADD信息。
     */
    public RateValue add(RateValue augend) {
        return new RateValue(this.value.add(requireOther(augend).value));
    }

    /**
     * 处理业务数据。
     */
    public RateValue subtract(RateValue subtrahend) {
        return new RateValue(this.value.subtract(requireOther(subtrahend).value));
    }

    /**
     * 处理业务数据。
     */
    public RateValue multiply(RateValue multiplicand) {
        return new RateValue(this.value.multiply(requireOther(multiplicand).value));
    }

    /**
     * 处理业务数据。
     */
    public RateValue multiply(long multiplicand) {
        return new RateValue(this.value.multiply(BigDecimal.valueOf(multiplicand)));
    }

    /**
     * 处理业务数据。
     */
    public RateValue multiply(int multiplicand) {
        return multiply((long) multiplicand);
    }

    /**
     * 处理业务数据。
     */
    public RateValue divide(RateValue divisor, int scale, RoundingMode roundingMode) {
        return new RateValue(this.value.divide(requireOther(divisor).value, scale, roundingMode));
    }

    /**
     * 处理业务数据。
     */
    public RateValue divide(RateValue divisor, RoundingMode roundingMode) {
        return new RateValue(this.value.divide(requireOther(divisor).value, SCALE, roundingMode));
    }

    /**
     * 处理SET信息。
     */
    public RateValue setScale(int scale, RoundingMode roundingMode) {
        return new RateValue(this.value.setScale(scale, roundingMode));
    }

    /**
     * 处理ABS信息。
     */
    public RateValue abs() {
        return this.value.signum() < 0 ? new RateValue(this.value.abs()) : this;
    }

    /**
     * 处理业务数据。
     */
    public RateValue negate() {
        return new RateValue(this.value.negate());
    }

    /**
     * 处理MAX信息。
     */
    public RateValue max(RateValue other) {
        return compareTo(other) >= 0 ? this : other;
    }

    /**
     * 处理MIN信息。
     */
    public RateValue min(RateValue other) {
        return compareTo(other) <= 0 ? this : other;
    }

    /**
     * 处理业务数据。
     */
    public int signum() {
        return this.value.signum();
    }

    /**
     * 处理业务数据。
     */
    public int scale() {
        return this.value.scale();
    }

    /**
     * 处理业务数据。
     */
    public RateValue stripTrailingZeros() {
        return new RateValue(this.value.stripTrailingZeros());
    }

    /**
     * 转换为业务数据。
     */
    public String toPlainString() {
        return this.value.toPlainString();
    }

    /**
     * 转换为BIG信息。
     */
    public BigDecimal toBigDecimal() {
        return this.value;
    }

    /**
     * 转换为业务数据。
     */
    @JsonValue
    public BigDecimal toJson() {
        return toBigDecimal();
    }

    /**
     * 处理值信息。
     */
    public long longValueExact() {
        return this.value.longValueExact();
    }

    /**
     * 处理INT值信息。
     */
    public int intValueExact() {
        return this.value.intValueExact();
    }

    /**
     * 处理TO信息。
     */
    @Override
    public int compareTo(RateValue other) {
        return this.value.compareTo(requireOther(other).value);
    }

    /**
     * 处理INT值信息。
     */
    @Override
    public int intValue() {
        return this.value.intValue();
    }

    /**
     * 处理值信息。
     */
    @Override
    public long longValue() {
        return this.value.longValue();
    }

    /**
     * 处理值信息。
     */
    @Override
    public float floatValue() {
        return this.value.floatValue();
    }

    /**
     * 处理值信息。
     */
    @Override
    public double doubleValue() {
        return this.value.doubleValue();
    }

    /**
     * 转换为业务数据。
     */
    @Override
    public String toString() {
        return toPlainString();
    }

    /**
     * 处理业务数据。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RateValue other)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    /**
     * 处理编码。
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal requireValue(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("rate must not be null");
        }
        return value;
    }

    private static RateValue requireOther(RateValue other) {
        if (other == null) {
            throw new IllegalArgumentException("other rate must not be null");
        }
        return other;
    }
}
