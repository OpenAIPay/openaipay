package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 交易状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum TradeStatus {
    /**
      * 单据已创建，等待后续业务编排。
       */
    CREATED,
    /**
      * 交易已完成报价并锁定计费快照。
       */
    QUOTED,
    /**
      * 交易已提交支付请求，等待支付域异步执行。
       */
    PAY_SUBMITTED,
    /**
      * 交易对应的支付正在异步处理中。
       */
    PAY_PROCESSING,
    /**
      * 交易正在执行支付预处理。
       */
    PAY_PREPARING,
    /**
      * 交易支付预处理完成，等待提交。
       */
    PAY_PREPARED,
    /**
      * 交易正在执行支付提交。
       */
    PAY_COMMITTING,
    /**
      * 交易执行成功并完成落账。
       */
    SUCCEEDED,
    /**
      * 交易正在执行支付回滚。
       */
    PAY_ROLLING_BACK,
    /**
      * 回滚完成，状态恢复到失败前。
       */
    ROLLED_BACK,
    /**
      * 流程执行失败，需人工或系统补偿处理。
       */
    FAILED,
    /**
      * 交易进入对账或补偿待处理状态。
       */
    RECON_PENDING;

    /**
     * 处理业务数据。
     */
    public static TradeStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("tradeStatus must not be blank");
        }
        try {
            return TradeStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported tradeStatus: " + raw);
        }
    }
}
