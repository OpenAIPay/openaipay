package cn.openaipay.application.bill.service;

import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import java.util.List;

/**
 * 账单应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface BillService {

    /**
     * 查询统一账单读模型条目。
     *
     * 业务场景：账单页面需要按用户维度统一查看 trade 与 fundTrade 聚合流水。
     */
    List<BillEntryDTO> queryUserBillEntries(Long userId, String billMonth, String businessDomainCode, Integer limit);

    /**
     * 分页查询统一账单读模型条目。
     *
     * 业务场景：账单页下拉加载时按页读取 trade_bill_index，稳定承载收益发放等高频流水查询。
     */
    BillEntryPageDTO queryUserBillEntriesPage(Long userId,
                                              String billMonth,
                                              String businessDomainCode,
                                              Integer pageNo,
                                              Integer pageSize,
                                              String cursorTradeTime,
                                              Long cursorId);
}
