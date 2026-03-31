package cn.openaipay.application.bankcard.facade.impl;

import cn.openaipay.application.bankcard.command.BindBankCardCommand;
import cn.openaipay.application.bankcard.command.SetDefaultBankCardCommand;
import cn.openaipay.application.bankcard.dto.BankCardDTO;
import cn.openaipay.application.bankcard.facade.BankCardFacade;
import cn.openaipay.application.bankcard.service.BankCardService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 银行卡门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class BankCardFacadeImpl implements BankCardFacade {

    /** BankCardService组件 */
    private final BankCardService bankCardService;

    public BankCardFacadeImpl(BankCardService bankCardService) {
        this.bankCardService = bankCardService;
    }

    /**
     * 处理银行信息。
     */
    @Override
    public BankCardDTO bindBankCard(BindBankCardCommand command) {
        return bankCardService.bindBankCard(command);
    }

    /**
     * 处理SET银行信息。
     */
    @Override
    public BankCardDTO setDefaultBankCard(SetDefaultBankCardCommand command) {
        return bankCardService.setDefaultBankCard(command);
    }

    /**
     * 查询用户银行信息列表。
     */
    @Override
    public List<BankCardDTO> listUserBankCards(Long userId) {
        return bankCardService.listUserBankCards(userId);
    }

    /**
     * 查询用户银行信息列表。
     */
    @Override
    public List<BankCardDTO> listUserActiveBankCards(Long userId) {
        return bankCardService.listUserActiveBankCards(userId);
    }
}
