package cn.openaipay.application.bill.facade;

import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import java.util.List;

/**
 * 账单门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface BillFacade {

    /**
     * 查询统一账单读模型条目。
     */
    List<BillEntryDTO> queryUserBillEntries(Long userId, String billMonth, String businessDomainCode, Integer limit);

    /**
     * 分页查询统一账单读模型条目。
     */
    BillEntryPageDTO queryUserBillEntriesPage(Long userId,
                                              String billMonth,
                                              String businessDomainCode,
                                              Integer pageNo,
                                              Integer pageSize,
                                              String cursorTradeTime,
                                              Long cursorId);
}
