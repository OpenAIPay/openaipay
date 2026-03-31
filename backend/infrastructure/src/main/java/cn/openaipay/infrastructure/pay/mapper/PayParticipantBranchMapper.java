package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayParticipantBranchDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付参与方分支持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface PayParticipantBranchMapper extends BaseMapper<PayParticipantBranchDO> {

    /**
     * 按支付订单单号订单IDASC查找记录。
     */
    default List<PayParticipantBranchDO> findByPayOrderNoOrderByIdAsc(String payOrderNo) {
        QueryWrapper<PayParticipantBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 按支付订单单号查找记录。
     */
    default Optional<PayParticipantBranchDO> findByPayOrderNoAndParticipantType(String payOrderNo, String participantType) {
        QueryWrapper<PayParticipantBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.eq("participant_type", participantType);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按支付订单单号与ID查找记录。
     */
    default Optional<PayParticipantBranchDO> findByPayOrderNoAndBranchId(String payOrderNo, String branchId) {
        QueryWrapper<PayParticipantBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
