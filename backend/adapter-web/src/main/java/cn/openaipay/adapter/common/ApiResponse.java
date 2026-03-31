package cn.openaipay.adapter.common;
/**
 * 接口响应参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record ApiResponse<T>(
        /** 是否成功 */
        boolean success,
        /** 响应数据 */
        T data,
        /** 错误信息 */
        ErrorBody error
) {

    /**
     * 处理业务数据。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 处理业务数据。
     */
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    /**
     * 错误Body记录
     *
     * @author: tenggk.ai
     * @date: 2026/03/04
     */
    public record ErrorBody(
        /** 编码 */
        String code,
        /** 消息内容 */
        String message
    ) {}
}
