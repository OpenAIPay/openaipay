package cn.openaipay.application.bill.facade.impl;

import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import cn.openaipay.application.bill.facade.BillFacade;
import cn.openaipay.application.bill.service.BillService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 账单门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class BillFacadeImpl implements BillFacade {

    /** 账单应用服务。 */
    private final BillService billService;

    public BillFacadeImpl(BillService billService) {
        this.billService = billService;
    }

    /**
     * 查询统一账单读模型条目。
     */
    @Override
    public List<BillEntryDTO> queryUserBillEntries(Long userId,
                                                   String billMonth,
                                                   String businessDomainCode,
                                                   Integer limit) {
        return billService.queryUserBillEntries(userId, billMonth, businessDomainCode, limit);
    }

    /**
     * 分页查询统一账单读模型条目。
     */
    @Override
    public BillEntryPageDTO queryUserBillEntriesPage(Long userId,
                                                     String billMonth,
                                                     String businessDomainCode,
                                                     Integer pageNo,
                                                     Integer pageSize,
                                                     String cursorTradeTime,
                                                     Long cursorId) {
        return billService.queryUserBillEntriesPage(
                userId,
                billMonth,
                businessDomainCode,
                pageNo,
                pageSize,
                cursorTradeTime,
                cursorId
        );
    }
}
