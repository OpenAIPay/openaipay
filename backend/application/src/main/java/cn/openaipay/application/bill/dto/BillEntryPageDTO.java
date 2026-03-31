package cn.openaipay.application.bill.dto;

import java.util.List;

/**
 * 统一账单分页结果传输对象。
 *
 * 业务场景：账单页按月份筛选后需要持续下拉加载，服务端返回当前页记录与分页续拉标识，避免客户端自行推断是否还有下一页。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record BillEntryPageDTO(
        /** 当前页账单条目。 */
        List<BillEntryDTO> items,
        /** 当前页码，从 1 开始。 */
        Integer pageNo,
        /** 每页条数。 */
        Integer pageSize,
        /** 是否还有下一页。 */
        boolean hasMore,
        /** 下一页页码，无下一页时为 null。 */
        Integer nextPageNo,
        /** 下一页游标时间，格式 yyyy-MM-dd HH:mm:ss。 */
        String nextCursorTradeTime,
        /** 下一页游标ID。 */
        Long nextCursorId
) {
}
