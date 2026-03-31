package cn.openaipay.domain.pay.model;

import java.util.Locale;

/**
 * 支付参与方状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PayParticipantStatus {
    /**
      * 分支已创建，尚未执行TRY逻辑。
       */
    INIT,
    /**
      * TRY阶段执行成功，资源已预占。
       */
    TRY_OK,
    /**
      * TRY阶段执行失败，未形成可提交状态。
       */
    TRY_FAILED,
    /**
      * CONFIRM阶段执行成功，预占资源已正式落账。
       */
    CONFIRM_OK,
    /**
      * CANCEL阶段执行成功，预占资源已释放。
       */
    CANCEL_OK,
    /**
      * 当前分支按条件跳过，无需执行。
       */
    SKIPPED;

    /**
     * 处理业务数据。
     */
    public static PayParticipantStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("participant status must not be blank");
        }
        try {
            return PayParticipantStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported participant status: " + raw);
        }
    }
}
