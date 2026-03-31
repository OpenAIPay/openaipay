package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 交易流程步骤状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum TradeFlowStepStatus {
    /**
      * 步骤正在执行中。
       */
    RUNNING,
    /**
      * 步骤执行成功。
       */
    SUCCESS,
    /**
      * 流程执行失败，需人工或系统补偿处理。
       */
    FAILED,
    /**
      * 当前分支按条件跳过，无需执行。
       */
    SKIPPED;

    /**
     * 处理业务数据。
     */
    public static TradeFlowStepStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("stepStatus must not be blank");
        }
        try {
            return TradeFlowStepStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported stepStatus: " + raw);
        }
    }
}
