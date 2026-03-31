package cn.openaipay.application.settle.facade.impl;

import cn.openaipay.application.settle.command.SettleCommittedTradeCommand;
import cn.openaipay.application.settle.dto.SettleResultDTO;
import cn.openaipay.application.settle.facade.SettleFacade;
import cn.openaipay.application.settle.service.SettleService;
import org.springframework.stereotype.Service;

/**
 * 结算门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class SettleFacadeImpl implements SettleFacade {

    /** 结算信息 */
    private final SettleService settleService;

    public SettleFacadeImpl(SettleService settleService) {
        this.settleService = settleService;
    }

    /**
     * 处理结算交易信息。
     */
    @Override
    public SettleResultDTO settleCommittedTrade(SettleCommittedTradeCommand command) {
        return settleService.settleCommittedTrade(command);
    }
}
