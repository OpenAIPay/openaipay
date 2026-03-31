package cn.openaipay.domain.user.model;

/**
 * KYC等级枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum KycLevel {
    /**
      * 游客级别，未实名。
       */
    L0,
    /**
      * 基础实名级别。
       */
    L1,
    /**
      * 增强实名级别。
       */
    L2,
    /**
      * 高等级实名级别。
       */
    L3
}
