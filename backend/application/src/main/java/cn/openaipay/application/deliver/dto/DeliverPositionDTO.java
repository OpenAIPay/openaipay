package cn.openaipay.application.deliver.dto;

import java.util.List;

/**
 * 展位投放返回DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverPositionDTO(
        /** 位置编码 */
        String positionCode,
        /** 位置名称 */
        String positionName,
        /** 位置类型 */
        String positionType,
        /** 预览图地址 */
        String previewImage,
        /** 轮播间隔秒数 */
        Integer slideInterval,
        /** 最大次数 */
        Integer maxDisplayCount,
        /** 是否命中兜底标记 */
        boolean fallbackReturned,
        /** 业务列表 */
        List<DeliverCreativeDTO> creativeList
) {

    /**
     * 处理业务数据。
     */
    public static DeliverPositionDTO empty(String positionCode) {
        return new DeliverPositionDTO(positionCode, null, null, null, null, null, false, List.of());
    }
}
