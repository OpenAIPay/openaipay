package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;
import cn.openaipay.application.inbound.facade.InboundFacade;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台入金中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/inbound")
public class AdminInboundController {

    /** 入金门面。 */
    private final InboundFacade inboundFacade;

    public AdminInboundController(InboundFacade inboundFacade) {
        this.inboundFacade = inboundFacade;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("inbound.order.view")
    public ApiResponse<InboundOrderOverviewDTO> overview() {
        return ApiResponse.success(inboundFacade.getOverview());
    }

    /**
     * 查询订单信息列表。
     */
    @GetMapping("/orders")
    @RequireAdminPermission("inbound.order.view")
    public ApiResponse<List<InboundOrderDTO>> listOrders(
            @RequestParam(value = "inboundId", required = false) String inboundId,
            @RequestParam(value = "requestBizNo", required = false) String requestBizNo,
            @RequestParam(value = "payOrderNo", required = false) String payOrderNo,
            @RequestParam(value = "inboundStatus", required = false) String inboundStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(inboundFacade.listOrders(
                inboundId,
                requestBizNo,
                payOrderNo,
                inboundStatus,
                pageNo,
                resolvedPageSize
        ));
    }

    /**
     * 获取订单信息。
     */
    @GetMapping("/orders/{inboundId}")
    @RequireAdminPermission("inbound.order.view")
    public ApiResponse<InboundOrderDTO> getOrder(@PathVariable("inboundId") String inboundId) {
        try {
            return ApiResponse.success(inboundFacade.queryByInboundId(requireText(inboundId, "inboundId")));
        } catch (NoSuchElementException exception) {
            throw new NoSuchElementException("inbound order not found: " + inboundId);
        }
    }

    private String requireText(String value, String label) {
        String normalized = (value == null ? "" : value).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return normalized;
    }

}
