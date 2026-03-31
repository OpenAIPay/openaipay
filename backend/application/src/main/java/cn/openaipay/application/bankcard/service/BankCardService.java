package cn.openaipay.application.bankcard.service;

import cn.openaipay.application.bankcard.command.BindBankCardCommand;
import cn.openaipay.application.bankcard.command.SetDefaultBankCardCommand;
import cn.openaipay.application.bankcard.dto.BankCardDTO;

import java.util.List;

/**
 * 银行卡应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface BankCardService {

    /**
     * 处理银行信息。
     */
    BankCardDTO bindBankCard(BindBankCardCommand command);

    /**
     * 处理SET银行信息。
     */
    BankCardDTO setDefaultBankCard(SetDefaultBankCardCommand command);

    /**
     * 查询用户银行信息列表。
     */
    List<BankCardDTO> listUserBankCards(Long userId);

    /**
     * 查询用户银行信息列表。
     */
    List<BankCardDTO> listUserActiveBankCards(Long userId);
}
