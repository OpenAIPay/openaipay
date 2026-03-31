package cn.openaipay.domain.creditaccount.model;

/**
 * 信用TCC分支状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CreditTccBranchStatus {
    /**
      * TRY阶段执行成功。
       */
    TRIED,
    /**
      * CONFIRM阶段执行成功。
       */
    CONFIRMED,
    /**
      * CANCEL阶段执行成功。
       */
    CANCELED
}
