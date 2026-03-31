package cn.openaipay.infrastructure.inbound.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.inbound.dataobject.InboundOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 入金订单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrderDO> {

    @Insert("""
            INSERT INTO inbound_order (
                inbound_id,
                inst_id,
                inst_channel_code,
                inbound_order_no,
                payer_account_no,
                inbound_amount,
                account_amount,
                settle_amount,
                inbound_status,
                result_code,
                result_description,
                request_identify,
                request_biz_no,
                biz_order_no,
                trade_order_no,
                pay_order_no,
                pay_channel_code,
                biz_identity,
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
                #{payerAccountNo},
                #{inboundAmount},
                #{accountAmount},
                #{settleAmount},
                #{inboundStatus},
                #{resultCode},
                #{resultDescription},
                #{requestIdentify},
                #{requestBizNo},
                #{bizOrderNo},
                #{tradeOrderNo},
                #{payOrderNo},
                #{payChannelCode},
                #{bizIdentity},
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
                payer_account_no = VALUES(payer_account_no),
                inbound_amount = VALUES(inbound_amount),
                account_amount = VALUES(account_amount),
                settle_amount = VALUES(settle_amount),
                inbound_status = VALUES(inbound_status),
                result_code = VALUES(result_code),
                result_description = VALUES(result_description),
                request_identify = VALUES(request_identify),
                biz_order_no = VALUES(biz_order_no),
                trade_order_no = VALUES(trade_order_no),
                pay_channel_code = VALUES(pay_channel_code),
                biz_identity = VALUES(biz_identity),
                gmt_submit = VALUES(gmt_submit),
                gmt_resp = VALUES(gmt_resp),
                gmt_settle = VALUES(gmt_settle),
                gmt_modified = VALUES(gmt_modified)
            """)
    /**
     * 按入金ID保存或更新记录。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsertByInboundId(InboundOrderDO entity);

    /**
     * 按入金ID查找记录。
     */
    default Optional<InboundOrderDO> findByInboundId(String inboundId) {
        QueryWrapper<InboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("inbound_id", inboundId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按请求业务单号查找记录。
     */
    default Optional<InboundOrderDO> findByRequestBizNo(String requestBizNo) {
        QueryWrapper<InboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("request_biz_no", requestBizNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按支付订单单号查找记录。
     */
    default Optional<InboundOrderDO> findByPayOrderNo(String payOrderNo) {
        QueryWrapper<InboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
