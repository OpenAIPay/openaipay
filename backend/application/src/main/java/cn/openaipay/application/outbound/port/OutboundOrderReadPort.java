package cn.openaipay.application.outbound.port;

import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;

import java.util.List;

/**
 * 出金只读查询端口
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface OutboundOrderReadPort {

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
                                      int pageNo,
                                      int pageSize);
}
