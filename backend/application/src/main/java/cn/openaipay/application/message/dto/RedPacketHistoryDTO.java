package cn.openaipay.application.message.dto;

import java.util.List;
import org.joda.money.Money;

/**
 * 红包记录页数据传输对象
 *
 * 业务场景：聚合红包记录页顶部汇总信息和列表明细，供客户端直接渲染。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record RedPacketHistoryDTO(
        /** 用户ID */
        Long userId,
        /** 方向 */
        String direction,
        /** 年份 */
        Integer year,
        /** 总数 */
        Long totalCount,
        /** 总金额 */
        Money totalAmount,
        /** 条目列表 */
        List<RedPacketHistoryItemDTO> items
) {
}
