package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;
import cn.openaipay.application.outbound.facade.OutboundFacade;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台出金中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/outbound")
public class AdminOutboundController {

    /** 出金门面。 */
    private final OutboundFacade outboundFacade;

    public AdminOutboundController(OutboundFacade outboundFacade) {
        this.outboundFacade = outboundFacade;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("outbound.order.view")
    public ApiResponse<OutboundOrderOverviewDTO> overview() {
        return ApiResponse.success(outboundFacade.getOverview());
    }

    /**
     * 查询订单信息列表。
     */
    @GetMapping("/orders")
    @RequireAdminPermission("outbound.order.view")
    public ApiResponse<List<OutboundOrderDTO>> listOrders(
            @RequestParam(value = "outboundId", required = false) String outboundId,
            @RequestParam(value = "requestBizNo", required = false) String requestBizNo,
            @RequestParam(value = "payOrderNo", required = false) String payOrderNo,
            @RequestParam(value = "outboundStatus", required = false) String outboundStatus,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(outboundFacade.listOrders(
                outboundId,
                requestBizNo,
                payOrderNo,
                outboundStatus,
                pageNo,
                resolvedPageSize
        ));
    }

    /**
     * 获取订单信息。
     */
    @GetMapping("/orders/{outboundId}")
    @RequireAdminPermission("outbound.order.view")
    public ApiResponse<OutboundOrderDTO> getOrder(@PathVariable("outboundId") String outboundId) {
        try {
            return ApiResponse.success(outboundFacade.queryByOutboundId(requireText(outboundId, "outboundId")));
        } catch (NoSuchElementException exception) {
            throw new NoSuchElementException("outbound order not found: " + outboundId);
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
