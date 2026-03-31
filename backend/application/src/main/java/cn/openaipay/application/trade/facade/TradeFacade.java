package cn.openaipay.application.trade.facade;

import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.command.CreateDepositTradeCommand;
import cn.openaipay.application.trade.command.CreateRefundTradeCommand;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.command.CreateWithdrawTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.dto.TradeWalletFlowDTO;
import java.util.List;

/**
 * 交易门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface TradeFacade {

    /**
     * 处理业务数据。
     */
    TradeOrderDTO deposit(CreateDepositTradeCommand command);

    /**
     * 处理业务数据。
     */
    TradeOrderDTO withdraw(CreateWithdrawTradeCommand command);

    /**
     * 处理支付信息。
     */
    TradeOrderDTO pay(CreatePayTradeCommand command);

    /**
     * 处理转账信息。
     */
    TradeOrderDTO transfer(CreateTransferTradeCommand command);

    /**
     * 处理退款信息。
     */
    TradeOrderDTO refund(CreateRefundTradeCommand command);


    /**
     * 按交易订单单号查询记录。
     */
    TradeOrderDTO queryByTradeOrderNo(String tradeOrderNo);

    /**
     * 按请求单号查询记录。
     */
    TradeOrderDTO queryByRequestNo(String requestNo);

    /**
     * 按业务域和业务单号查询交易主单。
     *
     * 业务场景：后台运营按爱花、爱借、爱存等业务单快速定位交易。
     */
    TradeOrderDTO queryByBusinessOrder(String businessDomainCode, String bizOrderNo);

    /**
     * 查询钱包流程信息。
     */
    List<TradeWalletFlowDTO> queryRecentWalletFlows(Long userId, Integer limit);
}
