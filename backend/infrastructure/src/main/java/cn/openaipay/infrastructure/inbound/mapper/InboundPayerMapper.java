package cn.openaipay.infrastructure.inbound.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.inbound.dataobject.InboundPayerDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 入金付款方持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface InboundPayerMapper extends BaseMapper<InboundPayerDO> {

    @Insert("""
            INSERT INTO inbound_payer (
                inbound_id,
                payer_account_no,
                gmt_create,
                gmt_modified
            ) VALUES (
                #{inboundId},
                #{payerAccountNo},
                #{createdAt},
                #{updatedAt}
            )
            ON DUPLICATE KEY UPDATE
                id = LAST_INSERT_ID(id),
                payer_account_no = VALUES(payer_account_no),
                gmt_modified = VALUES(gmt_modified)
            """)
    /**
     * 按入金ID保存或更新记录。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsertByInboundId(InboundPayerDO entity);

    /**
     * 按入金ID查找记录。
     */
    default Optional<InboundPayerDO> findByInboundId(String inboundId) {
        QueryWrapper<InboundPayerDO> wrapper = new QueryWrapper<>();
        wrapper.eq("inbound_id", inboundId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
