package cn.openaipay.infrastructure.common.persistence;

import cn.openaipay.domain.shared.number.FundAmount;
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
 * FundAmount类型处理器
 *
 * 业务场景：爱存相关份额、净值、收益等基金金额字段在数据库DECIMAL与FundAmount值对象间转换。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@MappedTypes(FundAmount.class)
@MappedJdbcTypes({JdbcType.DECIMAL, JdbcType.NUMERIC})
public class FundAmountTypeHandler extends BaseTypeHandler<FundAmount> {

    /**
     * 处理SETNON信息。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FundAmount parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, parameter.toBigDecimal());
    }

    /**
     * 获取结果。
     */
    @Override
    public FundAmount getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toFundAmount(rs.getBigDecimal(columnName));
    }

    /**
     * 获取结果。
     */
    @Override
    public FundAmount getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toFundAmount(rs.getBigDecimal(columnIndex));
    }

    /**
     * 获取结果。
     */
    @Override
    public FundAmount getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toFundAmount(cs.getBigDecimal(columnIndex));
    }

    private FundAmount toFundAmount(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return FundAmount.of(value);
    }
}
