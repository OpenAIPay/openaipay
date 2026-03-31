package cn.openaipay.domain.creditaccount.service.impl;

import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditTccBranch;
import cn.openaipay.domain.creditaccount.model.CreditTccOperationType;
import cn.openaipay.domain.creditaccount.service.CreditBillCurrentWindow;
import cn.openaipay.domain.creditaccount.service.CreditBillDetailItem;
import cn.openaipay.domain.creditaccount.service.CreditBillDomainService;
import cn.openaipay.domain.creditaccount.service.CreditBillSummary;
import cn.openaipay.domain.creditaccount.service.CreditBillUpcomingWindow;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * 信用账单领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class CreditBillDomainServiceImpl implements CreditBillDomainService {

    private static final Map<String, String> AICREDIT_BILL_MERCHANT_NAMES = Map.ofEntries(
            Map.entry("M01", "外婆家·西湖店"),
            Map.entry("M02", "喜茶·湖滨银泰店"),
            Map.entry("M03", "海底捞·龙湖天街店"),
            Map.entry("M04", "绿茶餐厅·西溪店"),
            Map.entry("M05", "奈雪的茶·in77店"),
            Map.entry("M06", "杭州大剧院票务"),
            Map.entry("M07", "猫眼电影·西湖区"),
            Map.entry("M08", "西西弗书店·银泰店"),
            Map.entry("M09", "网易云音乐会员"),
            Map.entry("M10", "杭州乐园门票"),
            Map.entry("M11", "叮咚买菜·古墩路站"),
            Map.entry("M12", "盒马鲜生·文一店"),
            Map.entry("M13", "永辉超市·湖滨店"),
            Map.entry("M14", "全家便利店·延安路店"),
            Map.entry("M15", "屈臣氏·城西银泰店"),
            Map.entry("M16", "滴滴出行·杭州"),
            Map.entry("M17", "曹操出行·杭州"),
            Map.entry("M18", "12306 铁路出行"),
            Map.entry("M19", "哈啰单车·杭州"),
            Map.entry("M20", "杭州公交电子客票"),
            Map.entry("M21", "阿里健康大药房"),
            Map.entry("M22", "杭州口腔医院"),
            Map.entry("M23", "微医互联网医院"),
            Map.entry("M24", "九洲大药房·天目里店"),
            Map.entry("M25", "国家电网·杭州"),
            Map.entry("M26", "杭州燃气集团"),
            Map.entry("M27", "中国移动·话费充值"),
            Map.entry("M28", "e袋洗·黄龙店"),
            Map.entry("M29", "美团家政保洁"),
            Map.entry("M30", "闪送同城急送")
    );

    private static final Map<String, String> AICREDIT_BILL_CATEGORY_NAMES = Map.ofEntries(
            Map.entry("C01", "餐饮美食"),
            Map.entry("C02", "文化休闲"),
            Map.entry("C03", "生活服务"),
            Map.entry("C04", "医疗健康"),
            Map.entry("C05", "公共服务"),
            Map.entry("C06", "交通出行"),
            Map.entry("C07", "日用百货")
    );

    /**
     * 解析当前信息。
     */
    @Override
    public CreditBillCurrentWindow resolveCurrentBillWindow(LocalDate today, Integer repayDayOfMonth) {
        LocalDate normalizedToday = today == null ? LocalDate.now() : today;
        int normalizedRepayDayOfMonth = normalizeRepayDayOfMonth(repayDayOfMonth);
        LocalDate candidateStatementDate = normalizedToday.withDayOfMonth(normalizedRepayDayOfMonth);
        LocalDate statementDate = normalizedToday.isBefore(candidateStatementDate)
                ? candidateStatementDate.minusMonths(1)
                : candidateStatementDate;
        LocalDate statementMonthStart = statementDate.withDayOfMonth(1);
        LocalDate cycleMonthStart = statementMonthStart.minusMonths(1);
        return new CreditBillCurrentWindow(
                statementDate,
                cycleMonthStart.atStartOfDay(),
                statementMonthStart.atStartOfDay(),
                statementMonthStart.atStartOfDay(),
                normalizedToday.plusDays(1).atStartOfDay()
        );
    }

    /**
     * 解析业务数据。
     */
    @Override
    public CreditBillUpcomingWindow resolveUpcomingBillWindow(LocalDate today, Integer repayDayOfMonth) {
        LocalDate normalizedToday = today == null ? LocalDate.now() : today;
        int normalizedRepayDayOfMonth = normalizeRepayDayOfMonth(repayDayOfMonth);
        LocalDate currentMonthStart = normalizedToday.withDayOfMonth(1);
        LocalDate nextMonthStart = currentMonthStart.plusMonths(1);
        LocalDate statementDate = nextMonthStart.withDayOfMonth(Math.min(normalizedRepayDayOfMonth, nextMonthStart.lengthOfMonth()));
        return new CreditBillUpcomingWindow(
                statementDate,
                currentMonthStart.atStartOfDay(),
                nextMonthStart.atStartOfDay()
        );
    }

    /**
     * 处理当前信息。
     */
    @Override
    public CreditBillSummary summarizeCurrentBill(CreditBillCurrentWindow window,
                                                  CreditAccount creditAccount,
                                                  List<CreditTccBranch> statementBranches,
                                                  List<CreditTccBranch> unbilledBranches,
                                                  List<CreditTccBranch> repaidBranches) {
        List<CreditTccBranch> normalizedStatementBranches = sortedBranches(statementBranches);
        Money statementTotalAmount = sumBranchAmount(normalizedStatementBranches, creditAccount);
        Money normalizedRepaidAmount = sumBranchAmount(repaidBranches, creditAccount);
        Money refundedAmount = zeroMoney(creditAccount);
        // 爱花还款页应还金额必须随“当前账单已还金额”实时减少，不再依赖账户总欠款推导。
        Money dueAmount = statementTotalAmount
                .minus(refundedAmount)
                .minus(normalizedRepaidAmount)
                .rounded(2, RoundingMode.HALF_UP);
        if (dueAmount.isNegative()) {
            dueAmount = zeroMoney(creditAccount);
        }
        Money repaidAmount = statementTotalAmount
                .minus(refundedAmount)
                .minus(dueAmount)
                .rounded(2, RoundingMode.HALF_UP);
        if (repaidAmount.isNegative()) {
            repaidAmount = zeroMoney(creditAccount);
        }
        LocalDate periodStartDate = window.cycleStartInclusive().toLocalDate();
        LocalDate periodEndDate = window.cycleEndExclusive().toLocalDate().minusDays(1);
        String title = window.statementDate().getMonthValue() + "月总计账单";
        String periodText = "入账周期："
                + periodStartDate.getMonthValue() + "月" + periodStartDate.getDayOfMonth() + "日 - "
                + periodEndDate.getMonthValue() + "月" + periodEndDate.getDayOfMonth() + "日";
        return new CreditBillSummary(
                title,
                periodText,
                dueAmount,
                statementTotalAmount,
                refundedAmount,
                repaidAmount,
                normalizedStatementBranches.stream().map(this::toDetailItem).toList()
        );
    }

    /**
     * 处理业务数据。
     */
    @Override
    public CreditBillSummary summarizeUpcomingBill(CreditBillUpcomingWindow window,
                                                   CreditAccount creditAccount,
                                                   List<CreditTccBranch> statementBranches,
                                                   List<CreditTccBranch> repaidBranches) {
        List<CreditTccBranch> normalizedStatementBranches = sortedBranches(statementBranches);
        Money statementTotalAmount = sumBranchAmount(normalizedStatementBranches, creditAccount);
        Money normalizedRepaidAmount = sumBranchAmount(repaidBranches, creditAccount);
        Money dueAmount = statementTotalAmount
                .minus(normalizedRepaidAmount)
                .rounded(2, RoundingMode.HALF_UP);
        if (dueAmount.isNegative()) {
            dueAmount = zeroMoney(statementTotalAmount.getCurrencyUnit());
        }
        Money repaidAmount = statementTotalAmount
                .minus(dueAmount)
                .rounded(2, RoundingMode.HALF_UP);
        if (repaidAmount.isNegative()) {
            repaidAmount = zeroMoney(statementTotalAmount.getCurrencyUnit());
        }
        LocalDate periodStartDate = window.cycleStartInclusive().toLocalDate();
        LocalDate periodEndDate = window.cycleEndExclusive().toLocalDate().minusDays(1);
        String title = window.statementDate().getMonthValue() + "月总计账单";
        String periodText = "入账周期："
                + periodStartDate.getMonthValue() + "月" + periodStartDate.getDayOfMonth() + "日 - "
                + periodEndDate.getMonthValue() + "月" + periodEndDate.getDayOfMonth() + "日";
        return new CreditBillSummary(
                title,
                periodText,
                dueAmount,
                statementTotalAmount,
                zeroMoney(statementTotalAmount.getCurrencyUnit()),
                repaidAmount,
                normalizedStatementBranches.stream().map(this::toDetailItem).toList()
        );
    }

    /**
     * 处理金额。
     */
    @Override
    public Money calculateNextMonthAccumulatedAmount(CreditAccount creditAccount,
                                                     List<CreditTccBranch> currentMonthBranches,
                                                     List<CreditTccBranch> currentMonthRepaidBranches) {
        if (creditAccount == null) {
            throw new IllegalArgumentException("creditAccount must not be null");
        }
        if (CreditAccountType.fromAccountNo(creditAccount.getAccountNo()) != CreditAccountType.AICREDIT) {
            return zeroMoney(creditAccount);
        }
        Money consumedAmount = sumBranchAmount(currentMonthBranches, creditAccount);
        Money repaidAmount = sumBranchAmount(currentMonthRepaidBranches, creditAccount);
        Money remainingAmount = consumedAmount.minus(repaidAmount).rounded(2, RoundingMode.HALF_UP);
        if (remainingAmount.isNegative()) {
            return zeroMoney(creditAccount);
        }
        return remainingAmount;
    }

    private List<CreditTccBranch> sortedBranches(List<CreditTccBranch> branches) {
        List<CreditTccBranch> normalizedBranches = new ArrayList<>(branches == null ? List.of() : branches);
        normalizedBranches.sort(Comparator.comparing(CreditTccBranch::getCreatedAt).reversed());
        return normalizedBranches;
    }

    private CreditBillDetailItem toDetailItem(CreditTccBranch branch) {
        LocalDateTime createdAt = branch.getCreatedAt();
        BillDisplayMetadata metadata = resolveDisplayMetadata(branch);
        Money normalizedAmount = normalizeDetailItemAmount(branch);
        return new CreditBillDetailItem(
                createdAt.getMonthValue() + "月" + createdAt.getDayOfMonth() + "日",
                metadata.displayTitle(),
                metadata.displaySubtitle(),
                normalizedAmount,
                branch.getBusinessNo()
        );
    }

    private BillDisplayMetadata resolveDisplayMetadata(CreditTccBranch branch) {
        BillDisplayMetadata aicreditMetadata = resolveAiCreditBillDisplayMetadata(branch);
        if (aicreditMetadata != null) {
            return aicreditMetadata;
        }
        String businessNo = branch.getBusinessNo() == null ? "" : branch.getBusinessNo().trim();
        if (businessNo.startsWith("HB-CONSUME-") || businessNo.startsWith("QX-HB-CONSUME-")) {
            return new BillDisplayMetadata("爱花消费", "信用消费");
        }
        if (branch.getOperationType() == CreditTccOperationType.REPAY) {
            return new BillDisplayMetadata("爱花还款", "信用借还");
        }
        return new BillDisplayMetadata("爱花消费订单", "信用消费");
    }

    private BillDisplayMetadata resolveAiCreditBillDisplayMetadata(CreditTccBranch branch) {
        if (branch.getOperationType() == CreditTccOperationType.REPAY) {
            return null;
        }
        String businessNo = branch.getBusinessNo() == null ? "" : branch.getBusinessNo().trim().toUpperCase();
        if (!(businessNo.startsWith("HB-CONSUME-") || businessNo.startsWith("QX-HB-CONSUME-"))) {
            return null;
        }
        String[] tokens = businessNo.split("-");
        if (tokens.length < 5) {
            return null;
        }
        String merchantCode = tokens[tokens.length - 2];
        String categoryCode = tokens[tokens.length - 1];
        String merchantName = AICREDIT_BILL_MERCHANT_NAMES.get(merchantCode);
        String categoryName = AICREDIT_BILL_CATEGORY_NAMES.get(categoryCode);
        if (merchantName == null && categoryName == null) {
            return null;
        }
        return new BillDisplayMetadata(
                merchantName == null ? "爱花消费" : merchantName,
                categoryName == null ? "信用消费" : categoryName
        );
    }

    private Money sumBranchAmount(List<CreditTccBranch> branches, CreditAccount creditAccount) {
        CurrencyUnit currencyUnit = creditAccount == null
                ? CurrencyUnit.of("CNY")
                : creditAccount.getTotalLimit().getCurrencyUnit();
        Money amount = zeroMoney(currencyUnit);
        if (branches == null || branches.isEmpty()) {
            return amount;
        }
        for (CreditTccBranch branch : branches) {
            amount = amount.plus(branch.getAmount());
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroMoney(CreditAccount creditAccount) {
        return zeroMoney(creditAccount.getTotalLimit().getCurrencyUnit());
    }

    private Money zeroMoney(CurrencyUnit currencyUnit) {
        return Money.zero(currencyUnit == null ? CurrencyUnit.of("CNY") : currencyUnit).rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeDetailItemAmount(CreditTccBranch branch) {
        Money normalizedAmount = branch.getAmount().rounded(2, RoundingMode.HALF_UP);
        if (branch.getOperationType() == CreditTccOperationType.REPAY && normalizedAmount.isNegative()) {
            return normalizedAmount.negated();
        }
        return normalizedAmount;
    }

    private int normalizeRepayDayOfMonth(Integer source) {
        if (source == null) {
            return 10;
        }
        if (source < 1 || source > 28) {
            throw new IllegalArgumentException("repayDayOfMonth must be between 1 and 28");
        }
        return source;
    }

    private record BillDisplayMetadata(
        /** 展示标题信息 */
        String displayTitle,
        /** 展示subtitle信息 */
        String displaySubtitle
    ) {
    }
}
