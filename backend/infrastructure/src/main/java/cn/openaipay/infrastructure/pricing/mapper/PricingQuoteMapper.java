package cn.openaipay.infrastructure.pricing.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pricing.dataobject.PricingQuoteDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * Pricing报价持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface PricingQuoteMapper extends BaseMapper<PricingQuoteDO> {

    /**
     * 按单号查找记录。
     */
    default Optional<PricingQuoteDO> findByQuoteNo(String quoteNo) {
        QueryWrapper<PricingQuoteDO> wrapper = new QueryWrapper<>();
        wrapper.eq("quote_no", quoteNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按请求单号查找记录。
     */
    default Optional<PricingQuoteDO> findByRequestNo(String requestNo) {
        QueryWrapper<PricingQuoteDO> wrapper = new QueryWrapper<>();
        wrapper.eq("request_no", requestNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
