package cn.openaipay.application.inbound.service;

import cn.openaipay.application.inbound.command.CancelInboundDepositCommand;
import cn.openaipay.application.inbound.command.SubmitInboundDepositCommand;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;

import java.util.List;

/**
 * 入金应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface InboundService {

    /**
     * 查询概览。
     */
    InboundOrderOverviewDTO getOverview();

    /**
     * 查询订单列表。
     */
    List<InboundOrderDTO> listOrders(String inboundId,
                                     String requestBizNo,
                                     String payOrderNo,
                                     String inboundStatus,
                                     Integer pageNo,
                                     Integer pageSize);

    /**
     * 提交业务数据。
     */
    InboundOrderDTO submitDeposit(SubmitInboundDepositCommand command);

    /**
     * 取消业务数据。
     */
    InboundOrderDTO cancelDeposit(CancelInboundDepositCommand command);

    /**
     * 按入金ID查询记录。
     */
    InboundOrderDTO queryByInboundId(String inboundId);

    /**
     * 按请求业务单号查询记录。
     */
    InboundOrderDTO queryByRequestBizNo(String requestBizNo);
}
