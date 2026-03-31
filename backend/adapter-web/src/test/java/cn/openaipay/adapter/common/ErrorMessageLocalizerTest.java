package cn.openaipay.adapter.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorMessageLocalizerTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
class ErrorMessageLocalizerTest {

    @Test
    void shouldTranslateParticipantSplitMismatch() {
        assertThat(ErrorMessageLocalizer.localize("participant split amount must equal payableAmount"))
                .isEqualTo("支付拆分金额必须等于应付金额");
    }

    @Test
    void shouldTranslateGenericFieldValidationMessage() {
        assertThat(ErrorMessageLocalizer.localize("userId must be greater than 0"))
                .isEqualTo("用户标识必须大于0");
    }

    @Test
    void shouldTranslateNotFoundMessage() {
        assertThat(ErrorMessageLocalizer.localize("wallet account not found: 88010001"))
                .isEqualTo("未找到余额账户");
    }
}
