package cn.openaipay.domain.shared.number;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * FundAmount说明。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public final class FundAmount extends Number implements Comparable<FundAmount> {

    /** 精度常量 */
    public static final int SCALE = 4;
    /** 零值常量。 */
    public static final FundAmount ZERO = of(BigDecimal.ZERO);
    /** 单位值常量。 */
    public static final FundAmount ONE = of(BigDecimal.ONE);

    /** 值 */
    private final BigDecimal value;

    public FundAmount(BigDecimal value) {
        this.value = normalize(requireValue(value));
    }

    public FundAmount(String value) {
        this(new BigDecimal(value));
    }

    /**
     * 处理OF信息。
     */
    public static FundAmount of(BigDecimal value) {
        return new FundAmount(value);
    }

    /**
     * 处理OF信息。
     */
    public static FundAmount of(FundAmount value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        return value;
    }

    /**
     * 解析业务数据。
     */
    public static FundAmount parse(String value) {
        return new FundAmount(value);
    }

    /**
     * 处理业务数据。
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FundAmount fromJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FundAmount fundAmount) {
            return fundAmount;
        }
        if (value instanceof Number number) {
            return new FundAmount(new BigDecimal(number.toString()));
        }
        return new FundAmount(String.valueOf(value));
    }

    /**
     * 处理ADD信息。
     */
    public FundAmount add(FundAmount augend) {
        return new FundAmount(this.value.add(requireOther(augend).value));
    }

    /**
     * 处理业务数据。
     */
    public FundAmount subtract(FundAmount subtrahend) {
        return new FundAmount(this.value.subtract(requireOther(subtrahend).value));
    }

    /**
     * 处理业务数据。
     */
    public FundAmount multiply(FundAmount multiplicand) {
        return new FundAmount(this.value.multiply(requireOther(multiplicand).value));
    }

    /**
     * 处理业务数据。
     */
    public FundAmount multiply(long multiplicand) {
        return new FundAmount(this.value.multiply(BigDecimal.valueOf(multiplicand)));
    }

    /**
     * 处理业务数据。
     */
    public FundAmount multiply(int multiplicand) {
        return multiply((long) multiplicand);
    }

    /**
     * 处理业务数据。
     */
    public FundAmount divide(FundAmount divisor, int scale, RoundingMode roundingMode) {
        return new FundAmount(this.value.divide(requireOther(divisor).value, scale, roundingMode));
    }

    /**
     * 处理业务数据。
     */
    public FundAmount divide(FundAmount divisor, RoundingMode roundingMode) {
        return new FundAmount(this.value.divide(requireOther(divisor).value, SCALE, roundingMode));
    }

    /**
     * 处理SET信息。
     */
    public FundAmount setScale(int scale, RoundingMode roundingMode) {
        return new FundAmount(this.value.setScale(scale, roundingMode));
    }

    /**
     * 处理ABS信息。
     */
    public FundAmount abs() {
        return this.value.signum() < 0 ? new FundAmount(this.value.abs()) : this;
    }

    /**
     * 处理业务数据。
     */
    public FundAmount negate() {
        return new FundAmount(this.value.negate());
    }

    /**
     * 处理MAX信息。
     */
    public FundAmount max(FundAmount other) {
        return compareTo(other) >= 0 ? this : other;
    }

    /**
     * 处理MIN信息。
     */
    public FundAmount min(FundAmount other) {
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
    public FundAmount stripTrailingZeros() {
        return new FundAmount(this.value.stripTrailingZeros());
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
    public int compareTo(FundAmount other) {
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
        if (!(obj instanceof FundAmount other)) {
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
            throw new IllegalArgumentException("fund amount must not be null");
        }
        return value;
    }

    private static FundAmount requireOther(FundAmount other) {
        if (other == null) {
            throw new IllegalArgumentException("other fund amount must not be null");
        }
        return other;
    }
}
