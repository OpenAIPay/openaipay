package cn.openaipay.domain.bankcard.repository;

import cn.openaipay.domain.bankcard.model.BankCard;

import java.util.List;
import java.util.Optional;

/**
 * 银行卡仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface BankCardRepository {

    /**
     * 按单号查找记录。
     */
    Optional<BankCard> findByCardNo(String cardNo);

    /**
     * 按用户ID查找记录。
     */
    List<BankCard> findByUserId(Long userId);

    /**
     * 按用户ID查找记录。
     */
    List<BankCard> findActiveByUserId(Long userId);

    /**
     * 保存业务数据。
     */
    BankCard save(BankCard bankCard);

    /**
     * 保存ALL信息。
     */
    List<BankCard> saveAll(List<BankCard> bankCards);
}
