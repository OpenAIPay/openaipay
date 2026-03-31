package cn.openaipay.application.settle.dto;

/**
 * 结算执行结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettleResultDTO(
        /** 状态编码 */
        String status,
        /** 消息内容 */
        String message
) {

    /**
     * 处理业务数据。
     */
    public static SettleResultDTO success() {
        return new SettleResultDTO("SUCCESS", null);
    }

    /**
     * 处理业务数据。
     */
    public static SettleResultDTO failed(String message) {
        return new SettleResultDTO("FAILED", message);
    }

    /**
     * 处理业务数据。
     */
    public static SettleResultDTO reconPending(String message) {
        return new SettleResultDTO("RECON_PENDING", message);
    }
}
