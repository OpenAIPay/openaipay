package cn.openaipay.application.auth.dto;

/**
 * 登录下拉账号 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
public record LoginPresetAccountDTO(
        /** 登录账号 */
        String loginId,
        /** 展示昵称 */
        String nickname
) {
}
