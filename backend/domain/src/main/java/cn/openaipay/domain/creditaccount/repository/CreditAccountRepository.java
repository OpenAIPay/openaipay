package cn.openaipay.domain.creditaccount.repository;

import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditRepayBillMode;
import cn.openaipay.domain.creditaccount.model.CreditTccBranch;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 信用账户仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CreditAccountRepository {

    /**
     * 按账户号查询信用账户。
     */
    Optional<CreditAccount> findByAccountNo(String accountNo);

    /**
     * 按账户号加锁查询信用账户。
     */
    Optional<CreditAccount> findByAccountNoForUpdate(String accountNo);

    /**
     * 按用户ID查询默认信用账户（当前默认返回爱花账户）。
     */
    Optional<CreditAccount> findByUserId(Long userId);

    /**
     * 按用户ID和账户类型查询信用账户。
     */
    Optional<CreditAccount> findByUserIdAndType(Long userId, CreditAccountType accountType);

    /**
     * 保存信用账户。
     */
    CreditAccount save(CreditAccount creditAccount);

    /**
     * 按XID和分支ID查询TCC分支。
     */
    Optional<CreditTccBranch> findBranch(String xid, String branchId);

    /**
     * 按XID和分支ID加锁查询TCC分支。
     */
    Optional<CreditTccBranch> findBranchForUpdate(String xid, String branchId);

    /**
     * 保存TCC分支。
     */
    CreditTccBranch saveBranch(CreditTccBranch branch);

    /**
     * 查询本月已确认的爱花消费分支。
     *
     * 业务场景：爱花“总计账单”页面需要展示下月账单累计金额，
     * 该金额由本月已确认入账的爱花消费分支汇总而来。
     *
     * @param accountNo 爱花账户号
     * @param startInclusive 本月起始时间（含）
     * @param endExclusive 下月起始时间（不含）
     * @return 本月已确认的爱花消费分支列表
     */
    List<CreditTccBranch> findConfirmedPrincipalLendBranches(
            String accountNo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    /**
     * 查询指定时间窗口内已确认的爱花本金还款分支。
     *
     * 业务场景：爱花“3月账单”和“4月账单累计中”都要实时扣减已还金额，
     * 但当前账单还款与下期预还款必须分开统计，避免账单之间互相串账。
     *
     * @param accountNo 爱花账户号
     * @param startInclusive 账单出账时间（含）
     * @param endExclusive 查询截止时间（不含）
     * @param repayBillMode 目标账单模式，CURRENT 表示当前账单，NEXT 表示下期预还
     * @return 已确认的爱花本金还款分支列表
     */
    List<CreditTccBranch> findConfirmedPrincipalRepayBranches(
            String accountNo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            CreditRepayBillMode repayBillMode
    );
}
