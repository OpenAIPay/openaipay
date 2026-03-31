package cn.openaipay.domain.trade.client;

import java.util.List;

/**
 * PayClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface PayClient {
    /**
     * 提交业务数据。
     */
    TradePaySubmitResult submit(TradePaySubmitRequest request);

    /**
     * 按支付订单单号查询记录。
     */
    TradePayOrderSnapshot queryByPayOrderNo(String payOrderNo);

    /**
     * 按业务查询记录。
     */
    List<TradePayOrderSnapshot> queryBySourceBiz(String sourceBizType, String sourceBizNo);

    /**
     * 查询业务数据。
     */
    TradePayParticipantSnapshot queryParticipantBranch(String payOrderNo, String participantType);
}
