package cn.openaipay.application.settle.service;

import cn.openaipay.application.settle.command.SettleCommittedTradeCommand;
import cn.openaipay.application.settle.dto.SettleResultDTO;

/**
 * 结算应用服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface SettleService {

    /**
     * 执行支付成功后的结算入账。
     */
    SettleResultDTO settleCommittedTrade(SettleCommittedTradeCommand command);
}
