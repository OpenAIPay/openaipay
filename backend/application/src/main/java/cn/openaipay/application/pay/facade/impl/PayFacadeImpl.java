package cn.openaipay.application.pay.facade.impl;

import cn.openaipay.application.pay.command.SubmitPayCommand;
import cn.openaipay.application.pay.dto.PayOrderDTO;
import cn.openaipay.application.pay.dto.PayParticipantBranchDTO;
import cn.openaipay.application.pay.dto.PaySubmitReceiptDTO;
import cn.openaipay.application.pay.facade.PayFacade;
import cn.openaipay.application.pay.service.PayService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 支付门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PayFacadeImpl implements PayFacade {
    /** 支付应用服务。 */
    private final PayService payService;

    public PayFacadeImpl(PayService payService) {
        this.payService = payService;
    }

    /**
     * 提交业务数据。
     */
    @Override
    public PaySubmitReceiptDTO submit(SubmitPayCommand command) {
        return payService.submit(command);
    }

    /**
     * 按支付订单单号查询记录。
     */
    @Override
    public PayOrderDTO queryByPayOrderNo(String payOrderNo) {
        return payService.queryByPayOrderNo(payOrderNo);
    }

    /**
     * 按业务查询记录列表。
     */
    @Override
    public List<PayOrderDTO> listBySourceBiz(String sourceBizType, String sourceBizNo) {
        return payService.listBySourceBiz(sourceBizType, sourceBizNo);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public PayParticipantBranchDTO queryParticipantBranch(String payOrderNo, String participantType) {
        return payService.queryParticipantBranch(payOrderNo, participantType);
    }
}
