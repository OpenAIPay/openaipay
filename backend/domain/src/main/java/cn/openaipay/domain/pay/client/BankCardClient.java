package cn.openaipay.domain.pay.client;

import java.util.List;

/**
 * BankCardClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface BankCardClient {
    /**
     * 查询用户银行信息列表。
     */
    List<PayBankCardSnapshot> listUserActiveBankCards(Long userId);
}
