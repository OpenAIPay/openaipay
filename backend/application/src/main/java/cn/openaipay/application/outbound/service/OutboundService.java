package cn.openaipay.application.outbound.service;

import cn.openaipay.application.outbound.command.CancelOutboundWithdrawCommand;
import cn.openaipay.application.outbound.command.SubmitOutboundWithdrawCommand;
import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;

import java.util.List;

/**
 * 出金应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface OutboundService {

    /**
     * 查询概览。
     */
    OutboundOrderOverviewDTO getOverview();

    /**
     * 查询订单列表。
     */
    List<OutboundOrderDTO> listOrders(String outboundId,
                                      String requestBizNo,
                                      String payOrderNo,
                                      String outboundStatus,
                                      Integer pageNo,
                                      Integer pageSize);

    /**
     * 提交业务数据。
     */
    OutboundOrderDTO submitWithdraw(SubmitOutboundWithdrawCommand command);

    /**
     * 取消业务数据。
     */
    OutboundOrderDTO cancelWithdraw(CancelOutboundWithdrawCommand command);

    /**
     * 按出金ID查询记录。
     */
    OutboundOrderDTO queryByOutboundId(String outboundId);

    /**
     * 按请求业务单号查询记录。
     */
    OutboundOrderDTO queryByRequestBizNo(String requestBizNo);
}
