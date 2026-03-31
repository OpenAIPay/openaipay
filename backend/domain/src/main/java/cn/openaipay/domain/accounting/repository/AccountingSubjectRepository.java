package cn.openaipay.domain.accounting.repository;

import cn.openaipay.domain.accounting.model.AccountingSubject;
import cn.openaipay.domain.accounting.model.AccountingSubjectQuery;

import java.util.List;
import java.util.Optional;

/**
 * 会计科目仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingSubjectRepository {

    /**
     * 保存业务数据。
     */
    AccountingSubject save(AccountingSubject subject);

    /**
     * 按科目编码查找记录。
     */
    Optional<AccountingSubject> findBySubjectCode(String subjectCode);

    /**
     * 查询业务数据列表。
     */
    List<AccountingSubject> list(AccountingSubjectQuery query);
}
