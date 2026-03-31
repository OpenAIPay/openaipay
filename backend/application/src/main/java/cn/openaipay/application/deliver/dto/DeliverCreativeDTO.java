package cn.openaipay.application.deliver.dto;

/**
 * 投放创意返回DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverCreativeDTO(
        /** 业务编码 */
        String creativeCode,
        /** 业务名称 */
        String creativeName,
        /** 单元编码 */
        String unitCode,
        /** 业务编码 */
        String materialCode,
        /** 业务类型 */
        String materialType,
        /** 素材标题 */
        String materialTitle,
        /** 业务地址 */
        String imageUrl,
        /** 业务地址 */
        String landingUrl,
        /** 业务JSON */
        String schemaJson,
        /** 优先级 */
        Integer priority,
        /** 权重 */
        Integer weight,
        /** 订单信息 */
        Integer displayOrder,
        /** 兜底标记 */
        boolean fallback,
        /** 预览图地址 */
        String previewImage
) {
}
