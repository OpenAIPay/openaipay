package cn.openaipay.domain.trade.repository;

import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeBusinessIndex;
import cn.openaipay.domain.trade.model.TradeCreditOrder;
import cn.openaipay.domain.trade.model.TradeFlowStep;
import cn.openaipay.domain.trade.model.TradeFundOrder;
import cn.openaipay.domain.trade.model.TradeOrder;
import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 交易仓储接口。
 *
 * 业务场景：统一收口交易主单、流程轨迹、业务扩展单和业务查询索引，避免应用层同时依赖多张交易相关表。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface TradeRepository {

    /**
     * 保存交易主单。
     *
     * 业务场景：交易编排在报价、预提交、提交、回滚等阶段会多次刷新主单状态。
     */
    TradeOrder saveTradeOrder(TradeOrder tradeOrder);

    /**
     * 按交易号查询交易主单。
     *
     * 业务场景：支付、退款、转账链路需要通过统一交易号回查编排状态。
     */
    Optional<TradeOrder> findTradeOrderByTradeOrderNo(String tradeOrderNo);

    /**
     * 按请求号查询交易主单。
     *
     * 业务场景：幂等请求重试时需要从 requestNo 直接拿到既有主单。
     */
    Optional<TradeOrder> findTradeOrderByRequestNo(String requestNo);

    /**
     * 查询用户最近成功交易。
     *
     * 业务场景：余额页“余额变动明细”需要按时间倒序展示用户最近几笔已完成交易。
     */
    List<TradeOrder> findRecentSucceededTradesByUserId(Long userId, int limit);

    /**
     * 统计原交易下成功退款总额。
     *
     * 业务场景：退款场景需要校验累计退款金额不能超过原交易金额。
     */
    Money sumSucceededRefundAmount(String originalTradeOrderNo);

    /**
     * 保存交易流程步骤。
     *
     * 业务场景：记录计费、支付预提交、提交流程和补偿回滚的执行轨迹。
     */
    TradeFlowStep saveFlowStep(TradeFlowStep step);

    /**
     * 按交易号查询流程步骤列表。
     *
     * 业务场景：后台排查某一笔交易失败原因时查看完整执行步骤。
     */
    List<TradeFlowStep> findFlowSteps(String tradeOrderNo);

    /**
     * 保存业务交易查询索引。
     *
     * 业务场景：爱花、爱借、爱存等业务需要按业务单号直接检索交易，而不是再从统一主单里做条件拼装。
     */
    TradeBusinessIndex saveTradeBusinessIndex(TradeBusinessIndex tradeBusinessIndex);

    /**
     * 按统一交易号查询业务交易索引。
     *
     * 业务场景：统一主单查到后进一步补全业务展示信息。
     */
    Optional<TradeBusinessIndex> findTradeBusinessIndexByTradeOrderNo(String tradeOrderNo);

    /**
     * 按业务域和业务单号查询业务交易索引。
     *
     * 业务场景：后台输入爱花还款单号、爱存申购单号时可直接命中对应交易。
     */
    Optional<TradeBusinessIndex> findTradeBusinessIndexByBusinessOrder(TradeBusinessDomainCode businessDomainCode,
                                                                       String bizOrderNo);

    /**
     * 按用户查询统一账单聚合索引列表。
     *
     * 业务场景：账单中心按用户和账期读取 trade 与 fundTrade 聚合流水。
     */
    List<TradeBusinessIndex> findRecentTradeBusinessIndexesByUserId(Long userId,
                                                                    String billMonth,
                                                                    TradeBusinessDomainCode businessDomainCode,
                                                                    int limit);

    /**
     * 按用户分页查询统一账单聚合索引列表。
     *
     * 业务场景：账单页下拉分页时按 offset + limit 从 trade_bill_index 稳定读取，支持收益发放等高频流水连续翻页。
     */
    List<TradeBusinessIndex> findTradeBusinessIndexesByUserId(Long userId,
                                                              String billMonth,
                                                              TradeBusinessDomainCode businessDomainCode,
                                                              int offset,
                                                              int limit);

    /**
     * 按游标分页查询统一账单聚合索引列表。
     *
     * 业务场景：账单页下拉采用游标续拉，避免高页码 offset 带来的扫描放大和翻页抖动。
     */
    List<TradeBusinessIndex> findTradeBusinessIndexesByUserIdAfterCursor(Long userId,
                                                                         String billMonth,
                                                                         TradeBusinessDomainCode businessDomainCode,
                                                                         LocalDateTime cursorTradeTime,
                                                                         Long cursorId,
                                                                         int limit);

    /**
     * 保存信用业务交易扩展单。
     *
     * 业务场景：爱花、爱借等信用类产品需要保存账单号、计划号、本金利息拆分等特有字段。
     */
    TradeCreditOrder saveTradeCreditOrder(TradeCreditOrder tradeCreditOrder);

    /**
     * 按交易号查询信用业务交易扩展单。
     *
     * 业务场景：通过统一交易号回查对应的爱花或爱借业务明细。
     */
    Optional<TradeCreditOrder> findTradeCreditOrderByTradeOrderNo(String tradeOrderNo);

    /**
     * 按业务单号查询信用业务交易扩展单。
     *
     * 业务场景：后台按爱花还款单号、爱借借款单号直接查询信用交易详情。
     */
    Optional<TradeCreditOrder> findTradeCreditOrderByBizOrderNo(String bizOrderNo);

    /**
     * 按账户号查询最近一笔指定类型的信用业务交易扩展单。
     *
     * 业务场景：爱借还款链路需要读取最近借款的分期期数和利率元数据，作为“保持期数重算月供”的参数来源。
     */
    Optional<TradeCreditOrder> findLatestTradeCreditOrderByAccountAndType(String creditAccountNo,
                                                                           String creditTradeType);

    /**
     * 保存基金业务交易扩展单。
     *
     * 业务场景：爱存等基金业务需要保存基金账户号、份额变化和净值日期。
     */
    TradeFundOrder saveTradeFundOrder(TradeFundOrder tradeFundOrder);

    /**
     * 按交易号查询基金业务交易扩展单。
     *
     * 业务场景：通过统一交易号回查爱存申购、赎回等业务明细。
     */
    Optional<TradeFundOrder> findTradeFundOrderByTradeOrderNo(String tradeOrderNo);

    /**
     * 按业务单号查询基金业务交易扩展单。
     *
     * 业务场景：后台按爱存申赎业务单号直接定位对应基金交易。
     */
    Optional<TradeFundOrder> findTradeFundOrderByBizOrderNo(String bizOrderNo);
}
