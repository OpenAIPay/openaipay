package cn.openaipay.domain.creditaccount.model;

import org.joda.money.Money;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 信用AssetCategory枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CreditAssetCategory {
    /**
      * 普通本金账务分类。
       */
    PRINCIPAL {
        @Override
        void holdLend(CreditAccount account, Money amount, LocalDateTime now) {
            account.holdPrincipalForLend(amount, now);
        }

        @Override
        void confirmLend(CreditAccount account, Money amount, LocalDateTime now) {
            account.confirmPrincipalForLend(amount, now);
        }

        @Override
        void cancelLend(CreditAccount account, Money amount, LocalDateTime now) {
            account.cancelPrincipalForLend(amount, now);
        }

        @Override
        void holdRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.holdPrincipalForRepay(amount, now);
        }

        @Override
        void confirmRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.confirmPrincipalForRepay(amount, now);
        }

        @Override
        void cancelRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.cancelPrincipalForRepay(amount, now);
        }
    },
    /**
      * 逾期本金账务分类。
       */
    OVERDUE_PRINCIPAL {
        @Override
        void holdLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("OVERDUE_PRINCIPAL does not support lend operation");
        }

        @Override
        void confirmLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("OVERDUE_PRINCIPAL does not support lend operation");
        }

        @Override
        void cancelLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("OVERDUE_PRINCIPAL does not support lend operation");
        }

        @Override
        void holdRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.holdOverduePrincipalForRepay(amount, now);
        }

        @Override
        void confirmRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.confirmOverduePrincipalForRepay(amount, now);
        }

        @Override
        void cancelRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.cancelOverduePrincipalForRepay(amount, now);
        }
    },
    /**
      * 利息账务分类。
       */
    INTEREST {
        @Override
        void holdLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("INTEREST does not support lend operation");
        }

        @Override
        void confirmLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("INTEREST does not support lend operation");
        }

        @Override
        void cancelLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("INTEREST does not support lend operation");
        }

        @Override
        void holdRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.holdInterestForRepay(amount, now);
        }

        @Override
        void confirmRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.confirmInterestForRepay(amount, now);
        }

        @Override
        void cancelRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.cancelInterestForRepay(amount, now);
        }
    },
    /**
      * 罚息账务分类。
       */
    FINE {
        @Override
        void holdLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("FINE does not support lend operation");
        }

        @Override
        void confirmLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("FINE does not support lend operation");
        }

        @Override
        void cancelLend(CreditAccount account, Money amount, LocalDateTime now) {
            throw new IllegalArgumentException("FINE does not support lend operation");
        }

        @Override
        void holdRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.holdFineForRepay(amount, now);
        }

        @Override
        void confirmRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.confirmFineForRepay(amount, now);
        }

        @Override
        void cancelRepay(CreditAccount account, Money amount, LocalDateTime now) {
            account.cancelFineForRepay(amount, now);
        }
    };

    /**
     * 处理业务数据。
     */
    public static CreditAssetCategory from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("assetCategory must not be blank");
        }
        try {
            return CreditAssetCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported assetCategory: " + raw);
        }
    }

    abstract void holdLend(CreditAccount account, Money amount, LocalDateTime now);

    abstract void confirmLend(CreditAccount account, Money amount, LocalDateTime now);

    abstract void cancelLend(CreditAccount account, Money amount, LocalDateTime now);

    abstract void holdRepay(CreditAccount account, Money amount, LocalDateTime now);

    abstract void confirmRepay(CreditAccount account, Money amount, LocalDateTime now);

    abstract void cancelRepay(CreditAccount account, Money amount, LocalDateTime now);
}
