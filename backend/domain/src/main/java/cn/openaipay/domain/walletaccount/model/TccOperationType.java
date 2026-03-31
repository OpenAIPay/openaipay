package cn.openaipay.domain.walletaccount.model;

/**
 * TCCOperation类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum TccOperationType {
    /**
      * 扣减操作，减少可用余额。
       */
    DEBIT,
    /**
      * 加款操作，增加可用余额。
       */
    CREDIT
}
