package cn.openaipay.domain.creditaccount.service;

import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.model.CreditTccBranch;
import java.time.LocalDate;
import java.util.List;
import org.joda.money.Money;

/**
 * 信用账单领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface CreditBillDomainService {

    /**
     * 解析当前信息。
     */
    CreditBillCurrentWindow resolveCurrentBillWindow(LocalDate today, Integer repayDayOfMonth);

    /**
     * 解析业务数据。
     */
    CreditBillUpcomingWindow resolveUpcomingBillWindow(LocalDate today, Integer repayDayOfMonth);

    CreditBillSummary summarizeCurrentBill(
            CreditBillCurrentWindow window,
            CreditAccount creditAccount,
            List<CreditTccBranch> statementBranches,
            List<CreditTccBranch> unbilledBranches,
            List<CreditTccBranch> repaidBranches
    );

    CreditBillSummary summarizeUpcomingBill(
            CreditBillUpcomingWindow window,
            CreditAccount creditAccount,
            List<CreditTccBranch> statementBranches,
            List<CreditTccBranch> repaidBranches
    );

    /**
     * 处理金额。
     */
    Money calculateNextMonthAccumulatedAmount(
            CreditAccount creditAccount,
            List<CreditTccBranch> currentMonthBranches,
            List<CreditTccBranch> currentMonthRepaidBranches
    );
}
