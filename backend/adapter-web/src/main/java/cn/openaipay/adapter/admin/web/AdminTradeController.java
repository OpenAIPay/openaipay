package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理交易控制器
 *
 * 业务场景：后台运营在交易中心按统一交易号、请求号或业务单号查询交易编排主单。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/trades")
public class AdminTradeController {

    /** TradeFacade组件 */
    private final TradeFacade tradeFacade;

    public AdminTradeController(TradeFacade tradeFacade) {
        this.tradeFacade = tradeFacade;
    }

    /**
     * 按交易订单单号查询记录。
     */
    @GetMapping("/{tradeOrderNo}")
    @RequireAdminPermission("trade.order.view")
    public ApiResponse<TradeOrderDTO> queryByTradeOrderNo(@PathVariable("tradeOrderNo") String tradeOrderNo) {
        return ApiResponse.success(tradeFacade.queryByTradeOrderNo(tradeOrderNo));
    }

    /**
     * 按请求单号查询记录。
     */
    @GetMapping("/by-request/{requestNo}")
    @RequireAdminPermission("trade.order.view")
    public ApiResponse<TradeOrderDTO> queryByRequestNo(@PathVariable("requestNo") String requestNo) {
        return ApiResponse.success(tradeFacade.queryByRequestNo(requestNo));
    }

    /**
     * 按订单查询记录。
     */
    @GetMapping("/by-business/{businessDomainCode}/{bizOrderNo}")
    @RequireAdminPermission("trade.order.view")
    public ApiResponse<TradeOrderDTO> queryByBusinessOrder(@PathVariable("businessDomainCode") String businessDomainCode,
                                                           @PathVariable("bizOrderNo") String bizOrderNo) {
        return ApiResponse.success(tradeFacade.queryByBusinessOrder(businessDomainCode, bizOrderNo));
    }
}
