package cn.openaipay.domain.loanaccount.repository;

import cn.openaipay.domain.loanaccount.model.LoanAccountProfile;
import java.util.Optional;

/**
 * 爱借账户档案仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface LoanAccountProfileRepository {

    /**
     * 按账户号查询档案。
     */
    Optional<LoanAccountProfile> findByAccountNo(String accountNo);

    /**
     * 按账户号查询档案并加锁。
     */
    Optional<LoanAccountProfile> findByAccountNoForUpdate(String accountNo);

    /**
     * 保存档案。
     */
    LoanAccountProfile save(LoanAccountProfile profile);
}
