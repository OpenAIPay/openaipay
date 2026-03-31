package cn.openaipay.adapter.bill.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import cn.openaipay.application.bill.facade.BillFacade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账单控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@RestController
@RequestMapping("/api/bill")
public class BillController {

    /** 账单门面。 */
    private final BillFacade billFacade;

    public BillController(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    /**
     * 查询统一账单条目。
     */
    @GetMapping("/users/{userId}/entries")
    public ApiResponse<List<BillEntryDTO>> queryUserBillEntries(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "billMonth", required = false) String billMonth,
            @RequestParam(value = "businessDomainCode", required = false) String businessDomainCode,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(billFacade.queryUserBillEntries(userId, billMonth, businessDomainCode, limit));
    }

    /**
     * 分页查询统一账单条目。
     */
    @GetMapping("/users/{userId}/entries/page")
    public ApiResponse<BillEntryPageDTO> queryUserBillEntriesPage(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "billMonth", required = false) String billMonth,
            @RequestParam(value = "businessDomainCode", required = false) String businessDomainCode,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "cursorTradeTime", required = false) String cursorTradeTime,
            @RequestParam(value = "cursorId", required = false) Long cursorId) {
        return ApiResponse.success(
                billFacade.queryUserBillEntriesPage(
                        userId,
                        billMonth,
                        businessDomainCode,
                        pageNo,
                        pageSize,
                        cursorTradeTime,
                        cursorId
                )
        );
    }
}
