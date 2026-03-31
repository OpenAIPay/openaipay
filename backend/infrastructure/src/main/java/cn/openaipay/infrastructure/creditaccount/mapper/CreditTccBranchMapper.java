package cn.openaipay.infrastructure.creditaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditTccBranchDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 信用TCC分支持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface CreditTccBranchMapper extends BaseMapper<CreditTccBranchDO> {

    /**
     * 按XID与ID查找记录。
     */
    default Optional<CreditTccBranchDO> findByXidAndBranchId(String xid, String branchId) {
        QueryWrapper<CreditTccBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按XID与ID查找记录并加锁。
     */
    default Optional<CreditTccBranchDO> findByXidAndBranchIdForUpdate(String xid, String branchId) {
        QueryWrapper<CreditTccBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 查询当前月份已确认入账的爱花消费分支。
     *
     * 业务场景：爱花总计账单页中的“下月账单累计金额”需要把当月消费汇总后展示出来，
     * 因此这里只筛选已确认的消费类分支，不包含还款、撤销或冻结中的记录。
     *
     * @param accountNo 爱花账户号
     * @param startInclusive 月起始时间（含）
     * @param endExclusive 下月起始时间（不含）
     * @return 已确认的爱花消费分支列表
     */
    default List<CreditTccBranchDO> findConfirmedPrincipalLendBranches(
            String accountNo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    ) {
        QueryWrapper<CreditTccBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("account_no", accountNo);
        wrapper.eq("operation_type", "LEND");
        wrapper.eq("asset_category", "PRINCIPAL");
        wrapper.eq("branch_status", "CONFIRMED");
        wrapper.ge("created_at", startInclusive);
        wrapper.lt("created_at", endExclusive);
        wrapper.orderByAsc("created_at");
        return selectList(wrapper);
    }

    /**
     * 查询账单出账后已确认入账的爱花还款分支。
     *
     * 业务场景：爱花还款完成后回到还款页，应还金额必须实时减少，
     * 该金额由当前账单周期内已确认的还款分支汇总计算。
     *
     * @param accountNo 爱花账户号
     * @param startInclusive 账单出账时间（含）
     * @param endExclusive 当前查询时间（不含）
     * @return 已确认的爱花还款分支列表
     */
    @Select("""
            SELECT
                t.id AS id,
                t.xid AS xid,
                t.branch_id AS branch_id,
                t.account_no AS account_no,
                t.operation_type AS operation_type,
                t.asset_category AS asset_category,
                t.branch_status AS branch_status,
                t.amount AS amount,
                t.business_no AS business_no,
                t.lock_version AS lock_version,
                t.created_at AS created_at,
                t.updated_at AS updated_at
            FROM credit_tcc_transaction t
            LEFT JOIN pay_order p ON p.pay_order_no = t.business_no
            LEFT JOIN trade_order tr ON tr.trade_order_no = p.trade_order_no
            WHERE t.account_no = #{accountNo}
              AND t.operation_type = 'REPAY'
              AND t.asset_category = 'PRINCIPAL'
              AND t.branch_status = 'CONFIRMED'
              AND t.created_at >= #{startInclusive}
              AND t.created_at < #{endExclusive}
              AND UPPER(COALESCE(tr.metadata, '')) NOT LIKE '%REPAYBILLMODE=NEXT%'
            ORDER BY t.created_at ASC
            """)
    List<CreditTccBranchDO> findConfirmedPrincipalRepayBranchesForCurrentBill(@Param("accountNo") String accountNo,
                                                                              @Param("startInclusive") LocalDateTime startInclusive,
                                                                              @Param("endExclusive") LocalDateTime endExclusive);

    @Select("""
            SELECT
                t.id AS id,
                t.xid AS xid,
                t.branch_id AS branch_id,
                t.account_no AS account_no,
                t.operation_type AS operation_type,
                t.asset_category AS asset_category,
                t.branch_status AS branch_status,
                t.amount AS amount,
                t.business_no AS business_no,
                t.lock_version AS lock_version,
                t.created_at AS created_at,
                t.updated_at AS updated_at
            FROM credit_tcc_transaction t
            LEFT JOIN pay_order p ON p.pay_order_no = t.business_no
            LEFT JOIN trade_order tr ON tr.trade_order_no = p.trade_order_no
            WHERE t.account_no = #{accountNo}
              AND t.operation_type = 'REPAY'
              AND t.asset_category = 'PRINCIPAL'
              AND t.branch_status = 'CONFIRMED'
              AND t.created_at >= #{startInclusive}
              AND t.created_at < #{endExclusive}
              AND UPPER(COALESCE(tr.metadata, '')) LIKE '%REPAYBILLMODE=NEXT%'
            ORDER BY t.created_at ASC
            """)
    List<CreditTccBranchDO> findConfirmedPrincipalRepayBranchesForNextBill(@Param("accountNo") String accountNo,
                                                                           @Param("startInclusive") LocalDateTime startInclusive,
                                                                           @Param("endExclusive") LocalDateTime endExclusive);
}
