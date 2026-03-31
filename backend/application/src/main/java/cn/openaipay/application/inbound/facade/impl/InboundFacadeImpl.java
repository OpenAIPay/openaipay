package cn.openaipay.application.inbound.facade.impl;

import cn.openaipay.application.inbound.command.CancelInboundDepositCommand;
import cn.openaipay.application.inbound.command.SubmitInboundDepositCommand;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;
import cn.openaipay.application.inbound.facade.InboundFacade;
import cn.openaipay.application.inbound.service.InboundService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 入金门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class InboundFacadeImpl implements InboundFacade {
    /** 入金信息 */
    private final InboundService inboundService;

    public InboundFacadeImpl(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    /**
     * 查询概览。
     */
    @Override
    public InboundOrderOverviewDTO getOverview() {
        return inboundService.getOverview();
    }

    /**
     * 查询订单列表。
     */
    @Override
    public List<InboundOrderDTO> listOrders(String inboundId,
                                            String requestBizNo,
                                            String payOrderNo,
                                            String inboundStatus,
                                            Integer pageNo,
                                            Integer pageSize) {
        return inboundService.listOrders(inboundId, requestBizNo, payOrderNo, inboundStatus, pageNo, pageSize);
    }

    /**
     * 提交业务数据。
     */
    @Override
    public InboundOrderDTO submitDeposit(SubmitInboundDepositCommand command) {
        return inboundService.submitDeposit(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public InboundOrderDTO cancelDeposit(CancelInboundDepositCommand command) {
        return inboundService.cancelDeposit(command);
    }

    /**
     * 按入金ID查询记录。
     */
    @Override
    public InboundOrderDTO queryByInboundId(String inboundId) {
        return inboundService.queryByInboundId(inboundId);
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    public InboundOrderDTO queryByRequestBizNo(String requestBizNo) {
        return inboundService.queryByRequestBizNo(requestBizNo);
    }
}
