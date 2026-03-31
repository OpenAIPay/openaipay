package cn.openaipay.infrastructure.inbound.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.inbound.dataobject.InboundInstDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 入金机构持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface InboundInstMapper extends BaseMapper<InboundInstDO> {

    @Insert("""
            INSERT INTO inbound_inst (
                inbound_id,
                inst_id,
                inst_channel_code,
                inbound_order_no,
                result_code,
                result_description,
                inbound_status,
                gmt_submit,
                gmt_resp,
                gmt_settle,
                gmt_create,
                gmt_modified
            ) VALUES (
                #{inboundId},
                #{instId},
                #{instChannelCode},
                #{inboundOrderNo},
                #{resultCode},
                #{resultDescription},
                #{inboundStatus},
                #{gmtSubmit},
                #{gmtResp},
                #{gmtSettle},
                #{createdAt},
                #{updatedAt}
            )
            ON DUPLICATE KEY UPDATE
                id = LAST_INSERT_ID(id),
                inst_id = VALUES(inst_id),
                inst_channel_code = VALUES(inst_channel_code),
                inbound_order_no = VALUES(inbound_order_no),
                result_code = VALUES(result_code),
                result_description = VALUES(result_description),
                inbound_status = VALUES(inbound_status),
                gmt_submit = VALUES(gmt_submit),
                gmt_resp = VALUES(gmt_resp),
                gmt_settle = VALUES(gmt_settle),
                gmt_modified = VALUES(gmt_modified)
            """)
    /**
     * 按入金ID保存或更新记录。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsertByInboundId(InboundInstDO entity);

    /**
     * 按入金ID查找记录。
     */
    default Optional<InboundInstDO> findByInboundId(String inboundId) {
        QueryWrapper<InboundInstDO> wrapper = new QueryWrapper<>();
        wrapper.eq("inbound_id", inboundId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
