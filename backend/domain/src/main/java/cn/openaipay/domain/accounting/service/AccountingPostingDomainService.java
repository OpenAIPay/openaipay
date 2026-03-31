package cn.openaipay.domain.accounting.service;

import cn.openaipay.domain.accounting.model.AccountingEvent;
import cn.openaipay.domain.accounting.model.AccountingVoucher;

/**
 * 会计过账领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingPostingDomainService {

    /**
     * 构建凭证信息。
     */
    AccountingVoucher buildVoucher(AccountingEvent event);

    AccountingVoucher buildReverseVoucher(AccountingVoucher originalVoucher,
                                          String reverseEventId,
                                          String reverseReason,
                                          String operator);
}
