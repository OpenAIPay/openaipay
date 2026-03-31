package cn.openaipay.application.trade.facade.impl;

import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.command.CreateDepositTradeCommand;
import cn.openaipay.application.trade.command.CreateRefundTradeCommand;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.command.CreateWithdrawTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.dto.TradeWalletFlowDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import cn.openaipay.application.trade.service.TradeService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 交易门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class TradeFacadeImpl implements TradeFacade {
    /** 交易应用服务。 */
    private final TradeService tradeService;

    public TradeFacadeImpl(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public TradeOrderDTO deposit(CreateDepositTradeCommand command) {
        return tradeService.deposit(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public TradeOrderDTO withdraw(CreateWithdrawTradeCommand command) {
        return tradeService.withdraw(command);
    }

    /**
     * 处理支付信息。
     */
    @Override
    public TradeOrderDTO pay(CreatePayTradeCommand command) {
        return tradeService.pay(command);
    }

    /**
     * 处理转账信息。
     */
    @Override
    public TradeOrderDTO transfer(CreateTransferTradeCommand command) {
        return tradeService.transfer(command);
    }

    /**
     * 处理退款信息。
     */
    @Override
    public TradeOrderDTO refund(CreateRefundTradeCommand command) {
        return tradeService.refund(command);
    }

    /**
     * 按交易订单单号查询记录。
     */
    @Override
    public TradeOrderDTO queryByTradeOrderNo(String tradeOrderNo) {
        return tradeService.queryByTradeOrderNo(tradeOrderNo);
    }

    /**
     * 按请求单号查询记录。
     */
    @Override
    public TradeOrderDTO queryByRequestNo(String requestNo) {
        return tradeService.queryByRequestNo(requestNo);
    }

    /**
     * 按订单查询记录。
     */
    @Override
    public TradeOrderDTO queryByBusinessOrder(String businessDomainCode, String bizOrderNo) {
        return tradeService.queryByBusinessOrder(businessDomainCode, bizOrderNo);
    }

    /**
     * 查询钱包流程信息。
     */
    @Override
    public List<TradeWalletFlowDTO> queryRecentWalletFlows(Long userId, Integer limit) {
        return tradeService.queryRecentWalletFlows(userId, limit);
    }
}
