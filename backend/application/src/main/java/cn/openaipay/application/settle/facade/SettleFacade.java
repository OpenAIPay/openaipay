package cn.openaipay.application.settle.facade;

import cn.openaipay.application.settle.command.SettleCommittedTradeCommand;
import cn.openaipay.application.settle.dto.SettleResultDTO;

/**
 * 结算门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface SettleFacade {

    /**
     * 执行交易支付成功后的结算入账。
     */
    SettleResultDTO settleCommittedTrade(SettleCommittedTradeCommand command);
}
