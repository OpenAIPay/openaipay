package cn.openaipay.application.outbound.facade.impl;

import cn.openaipay.application.outbound.command.CancelOutboundWithdrawCommand;
import cn.openaipay.application.outbound.command.SubmitOutboundWithdrawCommand;
import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;
import cn.openaipay.application.outbound.facade.OutboundFacade;
import cn.openaipay.application.outbound.service.OutboundService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 出金门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class OutboundFacadeImpl implements OutboundFacade {
    /** 出金信息 */
    private final OutboundService outboundService;

    public OutboundFacadeImpl(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    /**
     * 查询概览。
     */
    @Override
    public OutboundOrderOverviewDTO getOverview() {
        return outboundService.getOverview();
    }

    /**
     * 查询订单列表。
     */
    @Override
    public List<OutboundOrderDTO> listOrders(String outboundId,
                                             String requestBizNo,
                                             String payOrderNo,
                                             String outboundStatus,
                                             Integer pageNo,
                                             Integer pageSize) {
        return outboundService.listOrders(outboundId, requestBizNo, payOrderNo, outboundStatus, pageNo, pageSize);
    }

    /**
     * 提交业务数据。
     */
    @Override
    public OutboundOrderDTO submitWithdraw(SubmitOutboundWithdrawCommand command) {
        return outboundService.submitWithdraw(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public OutboundOrderDTO cancelWithdraw(CancelOutboundWithdrawCommand command) {
        return outboundService.cancelWithdraw(command);
    }

    /**
     * 按出金ID查询记录。
     */
    @Override
    public OutboundOrderDTO queryByOutboundId(String outboundId) {
        return outboundService.queryByOutboundId(outboundId);
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    public OutboundOrderDTO queryByRequestBizNo(String requestBizNo) {
        return outboundService.queryByRequestBizNo(requestBizNo);
    }
}
