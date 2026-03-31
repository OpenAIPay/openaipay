package cn.openaipay.domain.accounting.repository;

import cn.openaipay.domain.accounting.model.AccountingVoucher;
import cn.openaipay.domain.accounting.model.AccountingVoucherQuery;

import java.util.List;
import java.util.Optional;

/**
 * 会计凭证仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingVoucherRepository {

    /**
     * 保存业务数据。
     */
    AccountingVoucher save(AccountingVoucher voucher);

    /**
     * 按凭证单号查找记录。
     */
    Optional<AccountingVoucher> findByVoucherNo(String voucherNo);

    /**
     * 按事件ID查找记录。
     */
    Optional<AccountingVoucher> findByEventId(String eventId);

    /**
     * 查询业务数据列表。
     */
    List<AccountingVoucher> list(AccountingVoucherQuery query);
}
