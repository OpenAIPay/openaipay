package cn.openaipay.infrastructure.agreement.mapper;

import cn.openaipay.infrastructure.agreement.dataobject.AgreementSignRecordDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 协议签约记录持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface AgreementSignRecordMapper extends BaseMapper<AgreementSignRecordDO> {

    /**
     * 按单号查找记录。
     */
    default Optional<AgreementSignRecordDO> findBySignNo(String signNo) {
        QueryWrapper<AgreementSignRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("sign_no", signNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按KEY查找记录。
     */
    default Optional<AgreementSignRecordDO> findByIdempotencyKey(Long userId, String bizType, String idempotencyKey) {
        QueryWrapper<AgreementSignRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("biz_type", bizType);
        wrapper.eq("idempotency_key", idempotencyKey);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

}
