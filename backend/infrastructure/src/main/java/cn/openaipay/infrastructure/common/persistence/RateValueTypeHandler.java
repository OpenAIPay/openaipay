package cn.openaipay.infrastructure.common.persistence;

import cn.openaipay.domain.shared.number.RateValue;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RateValue类型处理器
 *
 * 业务场景：汇率报价、计费费率等字段在数据库DECIMAL与RateValue值对象间转换。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@MappedTypes(RateValue.class)
@MappedJdbcTypes({JdbcType.DECIMAL, JdbcType.NUMERIC})
public class RateValueTypeHandler extends BaseTypeHandler<RateValue> {

    /**
     * 处理SETNON信息。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RateValue parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, parameter.toBigDecimal());
    }

    /**
     * 获取结果。
     */
    @Override
    public RateValue getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toRateValue(rs.getBigDecimal(columnName));
    }

    /**
     * 获取结果。
     */
    @Override
    public RateValue getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toRateValue(rs.getBigDecimal(columnIndex));
    }

    /**
     * 获取结果。
     */
    @Override
    public RateValue getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toRateValue(cs.getBigDecimal(columnIndex));
    }

    private RateValue toRateValue(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return RateValue.of(value);
    }
}
