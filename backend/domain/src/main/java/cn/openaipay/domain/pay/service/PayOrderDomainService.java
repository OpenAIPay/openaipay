package cn.openaipay.domain.pay.service;

import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayParticipantType;
import java.util.List;

/**
 * 支付订单领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface PayOrderDomainService {

    /**
     * 基于支付提交参数创建已提交支付单。
     */
    PayOrder createSubmittedOrder(PayOrderSubmission submission);

    /**
     * 解析支付参与方准备顺序。
     */
    List<PayParticipantType> resolvePreparationSequence(PayOrder payOrder);
}
