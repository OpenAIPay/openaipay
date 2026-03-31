package cn.openaipay.domain.accounting.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 支付系统标准会计科目目录。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public final class StandardAccountingSubjectCatalog {

    private static final List<SubjectDefinition> DEFINITIONS = List.of(
            new SubjectDefinition("100000", "资产类", SubjectType.ASSET, DebitCreditFlag.DEBIT, null, 1, true, "资产类根科目"),
            new SubjectDefinition("100100", "备付金与银行资金", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100000", 2, true, "平台备付金、银行户、冻结户等资金资产"),
            new SubjectDefinition("100101", "备付金银行存款", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100100", 3, true, "平台自有备付金/出金银行存款"),
            new SubjectDefinition("100102", "备付金冻结存款", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100100", 3, true, "银行冻结或监管冻结的备付金"),
            new SubjectDefinition("100200", "渠道清算资产", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100000", 2, true, "通道已扣款但尚未完成最终清算的资产"),
            new SubjectDefinition("100201", "入金渠道待清算资产", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100200", 3, true, "银行卡充值、收单后待与渠道清算的资金"),
            new SubjectDefinition("100202", "出金渠道待清算资产", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100200", 3, true, "提现出金发起后待渠道回执或对账完成的资金"),
            new SubjectDefinition("100300", "消费信贷应收", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100000", 2, true, "爱花/爱借等内部信用支付形成的应收资产"),
            new SubjectDefinition("100301", "爱花应收", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100300", 3, true, "爱花类信用支付应收"),
            new SubjectDefinition("100302", "爱借应收", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100300", 3, true, "爱借/现金贷类信用支付应收"),
            new SubjectDefinition("100400", "基金申赎在途资产", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100000", 2, true, "基金申购赎回尚未确认或到账的在途资产"),
            new SubjectDefinition("100401", "基金赎回待到账", SubjectType.ASSET, DebitCreditFlag.DEBIT, "100400", 3, true, "爱存等货币基金赎回在途资产"),

            new SubjectDefinition("200000", "负债类", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, null, 1, true, "负债类根科目"),
            new SubjectDefinition("200100", "用户资金负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200000", 2, true, "对用户持有资金承担的负债"),
            new SubjectDefinition("200101", "用户钱包可用余额", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200100", 3, true, "用户钱包可用余额负债"),
            new SubjectDefinition("200102", "用户钱包冻结余额", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200100", 3, true, "用户钱包冻结资金负债"),
            new SubjectDefinition("200200", "待结算负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200000", 2, true, "支付成功后待释放给商户/用户的钱款"),
            new SubjectDefinition("200201", "待结算资金", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200200", 3, true, "待结算资金主科目"),
            new SubjectDefinition("200202", "待结算冻结资金", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200200", 3, true, "待结算但被风控/人工冻结的资金"),
            new SubjectDefinition("200300", "用户提现中负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200000", 2, true, "用户提现流程中尚未兑付完成的负债"),
            new SubjectDefinition("200301", "用户提现处理中", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200300", 3, true, "已提交提现、待到账或待渠道处理"),
            new SubjectDefinition("200400", "理财资金负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200000", 2, true, "爱存等基金份额及申赎在途负债"),
            new SubjectDefinition("200401", "爱存已确认份额", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200400", 3, true, "已确认到用户名下的爱存份额"),
            new SubjectDefinition("200402", "爱存申购在途", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200400", 3, true, "爱存申购待确认份额"),
            new SubjectDefinition("200403", "爱存赎回在途", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200400", 3, true, "爱存赎回待到账负债"),
            new SubjectDefinition("200500", "营销权益负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200000", 2, true, "红包、优惠券等平台营销权益负债"),
            new SubjectDefinition("200501", "红包余额负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200500", 3, true, "红包待领取或待核销金额"),
            new SubjectDefinition("200502", "优惠券资金负债", SubjectType.LIABILITY, DebitCreditFlag.CREDIT, "200500", 3, true, "优惠券补贴待核销金额"),

            new SubjectDefinition("500000", "收入类", SubjectType.INCOME, DebitCreditFlag.CREDIT, null, 1, true, "收入类根科目"),
            new SubjectDefinition("500100", "支付服务收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500000", 2, true, "支付、提现等平台收费收入"),
            new SubjectDefinition("500101", "支付手续费收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500100", 3, true, "支付成功产生的平台手续费收入"),
            new SubjectDefinition("500102", "提现手续费收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500100", 3, true, "提现成功产生的平台手续费收入"),
            new SubjectDefinition("500200", "理财服务收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500000", 2, true, "理财销售与运营类收入"),
            new SubjectDefinition("500201", "基金销售服务费收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500200", 3, true, "爱存/基金销售服务费"),
            new SubjectDefinition("500300", "信贷服务收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500000", 2, true, "信贷服务类收入"),
            new SubjectDefinition("500301", "信贷息费收入", SubjectType.INCOME, DebitCreditFlag.CREDIT, "500300", 3, true, "爱花/爱借等信用业务息费"),

            new SubjectDefinition("600000", "费用类", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, null, 1, true, "费用类根科目"),
            new SubjectDefinition("600100", "通道与清算成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600000", 2, true, "支付通道、银行清算及网络费用"),
            new SubjectDefinition("600101", "入金通道手续费成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600100", 3, true, "银行卡充值/收单通道成本"),
            new SubjectDefinition("600102", "出金通道手续费成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600100", 3, true, "提现出金通道成本"),
            new SubjectDefinition("600200", "营销补贴成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600000", 2, true, "红包、券补贴及营销活动成本"),
            new SubjectDefinition("600201", "红包营销成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600200", 3, true, "红包类营销补贴成本"),
            new SubjectDefinition("600202", "优惠券营销成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600200", 3, true, "优惠券类营销补贴成本"),
            new SubjectDefinition("600300", "资金成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600000", 2, true, "信贷、理财等业务的资金占用成本"),
            new SubjectDefinition("600301", "信贷资金成本", SubjectType.EXPENSE, DebitCreditFlag.DEBIT, "600300", 3, true, "爱花/爱借等内部授信资金成本"),

            new SubjectDefinition("900000", "备查类", SubjectType.MEMO, DebitCreditFlag.DEBIT, null, 1, true, "备查类根科目"),
            new SubjectDefinition("900101", "待映射过账科目", SubjectType.MEMO, DebitCreditFlag.DEBIT, "900000", 2, true, "新业务场景未建科目前的兜底科目")
    );

    private StandardAccountingSubjectCatalog() {
    }

    /**
     * 处理业务数据。
     */
    public static List<SubjectDefinition> definitions() {
        return DEFINITIONS;
    }

    /**
     * 处理科目编码。
     */
    public static Set<String> subjectCodes() {
        Set<String> codes = new LinkedHashSet<>();
        for (SubjectDefinition definition : DEFINITIONS) {
            codes.add(definition.subjectCode());
        }
        return codes;
    }

    public record SubjectDefinition(
            /** 科目编码 */
            String subjectCode,
            /** 科目名称 */
            String subjectName,
            /** 科目类型 */
            SubjectType subjectType,
            /** balance方向信息 */
            DebitCreditFlag balanceDirection,
            /** parent科目编码 */
            String parentSubjectCode,
            /** level单号 */
            Integer levelNo,
            /** 启用标记 */
            boolean enabled,
            /** 备注 */
            String remark
    ) {
        /**
         * 转换为科目信息。
         */
        public AccountingSubject toSubject(LocalDateTime now) {
            LocalDateTime timestamp = now == null ? LocalDateTime.now() : now;
            return new AccountingSubject(
                    null,
                    subjectCode,
                    subjectName,
                    subjectType,
                    balanceDirection,
                    parentSubjectCode,
                    levelNo,
                    enabled,
                    remark,
                    timestamp,
                    timestamp
            );
        }
    }
}
