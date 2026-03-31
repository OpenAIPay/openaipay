package cn.openaipay.application.pay.facade;

import cn.openaipay.application.pay.command.SubmitPayCommand;
import cn.openaipay.application.pay.dto.PayOrderDTO;
import cn.openaipay.application.pay.dto.PayParticipantBranchDTO;
import cn.openaipay.application.pay.dto.PaySubmitReceiptDTO;
import java.util.List;

/**
 * 支付门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface PayFacade {

    /**
     * 提交业务数据。
     */
    PaySubmitReceiptDTO submit(SubmitPayCommand command);

    /**
     * 按支付订单单号查询记录。
     */
    PayOrderDTO queryByPayOrderNo(String payOrderNo);

    /**
     * 按业务查询记录列表。
     */
    List<PayOrderDTO> listBySourceBiz(String sourceBizType, String sourceBizNo);

    /**
     * 查询业务数据。
     */
    PayParticipantBranchDTO queryParticipantBranch(String payOrderNo, String participantType);
}
