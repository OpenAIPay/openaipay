package cn.openaipay.domain.creditaccount.model;

import org.joda.money.Money;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 信用TCCOperation类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CreditTccOperationType {
    /**
      * 借款占额操作，增加应还本金。
       */
    LEND {
        /**
         * 处理业务数据。
         */
        @Override
        public void hold(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.holdLend(account, amount, now);
        }

        /**
         * 确认业务数据。
         */
        @Override
        public void confirm(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.confirmLend(account, amount, now);
        }

        /**
         * 取消业务数据。
         */
        @Override
        public void cancel(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.cancelLend(account, amount, now);
        }
    },
    /**
      * 还款回补操作，减少应还账务。
       */
    REPAY {
        /**
         * 处理业务数据。
         */
        @Override
        public void hold(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.holdRepay(account, amount, now);
        }

        /**
         * 确认业务数据。
         */
        @Override
        public void confirm(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.confirmRepay(account, amount, now);
        }

        /**
         * 取消业务数据。
         */
        @Override
        public void cancel(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now) {
            assetCategory.cancelRepay(account, amount, now);
        }
    };

    /**
     * 处理业务数据。
     */
    public static CreditTccOperationType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("operationType must not be blank");
        }
        try {
            return CreditTccOperationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("operationType must be LEND or REPAY");
        }
    }

    /**
     * 处理业务数据。
     */
    public abstract void hold(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now);

    /**
     * 确认业务数据。
     */
    public abstract void confirm(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now);

    /**
     * 取消业务数据。
     */
    public abstract void cancel(CreditAccount account, CreditAssetCategory assetCategory, Money amount, LocalDateTime now);
}
