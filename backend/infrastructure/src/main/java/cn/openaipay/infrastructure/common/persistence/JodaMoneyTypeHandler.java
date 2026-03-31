package cn.openaipay.infrastructure.common.persistence;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Joda Money类型处理器
 *
 * 业务场景：将账户、交易、支付域中所有金额字段在数据库DECIMAL与Money对象间双向转换，
 * 保证金额统一以Money建模并保持精度一致。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@MappedTypes(Money.class)
@MappedJdbcTypes({JdbcType.DECIMAL, JdbcType.NUMERIC})
public class JodaMoneyTypeHandler extends BaseTypeHandler<Money> {

    /** 默认币种 */
    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");

    /**
     * 处理SETNON信息。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Money parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, parameter.getAmount());
    }

    /**
     * 获取结果。
     */
    @Override
    public Money getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toMoney(rs.getBigDecimal(columnName));
    }

    /**
     * 获取结果。
     */
    @Override
    public Money getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toMoney(rs.getBigDecimal(columnIndex));
    }

    /**
     * 获取结果。
     */
    @Override
    public Money getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toMoney(cs.getBigDecimal(columnIndex));
    }

    private Money toMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return Money.of(DEFAULT_CURRENCY, value, RoundingMode.HALF_UP);
    }
}
