package cn.openaipay.application.inbound.port;

import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;

import java.util.List;

/**
 * 入金只读查询端口
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface InboundOrderReadPort {

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
                                     int pageNo,
                                     int pageSize);
}
