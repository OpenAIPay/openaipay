package cn.openaipay.domain.pay.repository;

import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayFundDetailOwner;
import cn.openaipay.domain.pay.model.PayFundDetailSummary;
import cn.openaipay.domain.pay.model.PayFundDetailTool;
import cn.openaipay.domain.pay.model.PayParticipantBranch;

import java.util.List;
import java.util.Optional;
/**
 * 支付订单仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface PayOrderRepository {

    /**
     * 保存支付主单。
     */
    PayOrder saveOrder(PayOrder payOrder);

    /**
     * 按支付单号查询支付主单。
     */
    Optional<PayOrder> findOrderByPayOrderNo(String payOrderNo);

    /**
     * 按交易单号查询最新支付主单。
     */
    Optional<PayOrder> findLatestOrderByTradeOrderNo(String tradeOrderNo);

    /**
     * 按业务订单号查询支付主单。
     */
    Optional<PayOrder> findOrderByBizOrderNo(String bizOrderNo);

    /**
     * 批量按交易订单号查询最新支付主单。
     */
    List<PayOrder> findLatestOrdersByTradeOrderNos(List<String> tradeOrderNos);

    /**
     * 批量按业务订单号查询支付主单。
     */
    List<PayOrder> findOrdersByBizOrderNos(List<String> bizOrderNos);

    /**
     * 批量按支付单号查询支付主单。
     */
    List<PayOrder> findOrdersByPayOrderNos(List<String> payOrderNos);

    /**
     * 批量按来源业务类型+来源业务单号查询最新支付主单。
     */
    List<PayOrder> findLatestOrdersBySourceBizNos(String sourceBizType, List<String> sourceBizNos);

    /**
     * 按来源业务类型和来源业务单号查询支付主单。
     */
    Optional<PayOrder> findOrderBySourceBiz(String sourceBizType, String sourceBizNo);

    /**
     * 按来源业务类型和来源业务单号查询最新支付尝试单。
     */
    Optional<PayOrder> findLatestOrderBySourceBiz(String sourceBizType, String sourceBizNo);

    /**
     * 按来源业务类型、来源业务单号和尝试序号查询支付主单。
     */
    Optional<PayOrder> findOrderBySourceBizAndAttemptNo(String sourceBizType, String sourceBizNo, int attemptNo);

    /**
     * 按来源业务类型和来源业务单号查询全部支付尝试单，按最新尝试优先返回。
     */
    List<PayOrder> findOrdersBySourceBiz(String sourceBizType, String sourceBizNo);

    /**
     * 查询待对账的支付主单，按最早更新时间优先返回。
     */
    List<PayOrder> findReconPendingOrders(int limit);

    /**
     * 保存支付参与方分支。
     */
    PayParticipantBranch saveParticipantBranch(PayParticipantBranch branch);

    /**
     * 按支付单号查询参与方分支列表。
     */
    List<PayParticipantBranch> findParticipantBranches(String payOrderNo);

    /**
     * 按支付单号和参与方类型查询分支。
     */
    Optional<PayParticipantBranch> findParticipantBranch(String payOrderNo, String participantType);

    /**
     * 保存支付资金明细。
     */
    PayFundDetailSummary saveFundDetail(PayFundDetailSummary fundDetail);

    /**
     * 按支付单号查询资金明细列表。
     */
    List<PayFundDetailSummary> findFundDetails(String payOrderNo);

    /**
     * 按支付单号、工具、归属方查询资金明细。
     */
    Optional<PayFundDetailSummary> findFundDetail(String payOrderNo,
                                                  PayFundDetailTool payTool,
                                                  PayFundDetailOwner detailOwner);
}
