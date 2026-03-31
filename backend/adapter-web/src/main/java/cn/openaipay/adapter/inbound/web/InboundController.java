package cn.openaipay.adapter.inbound.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.inbound.web.request.CancelInboundDepositRequest;
import cn.openaipay.adapter.inbound.web.request.SubmitInboundDepositRequest;
import cn.openaipay.application.inbound.command.CancelInboundDepositCommand;
import cn.openaipay.application.inbound.command.SubmitInboundDepositCommand;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.facade.InboundFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入金控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/inbound")
public class InboundController {
    /** 入金门面。 */
    private final InboundFacade inboundFacade;

    /** 创建入金控制器并注入入金门面。 */
    public InboundController(InboundFacade inboundFacade) {
        this.inboundFacade = inboundFacade;
    }

    /**
     * 提交业务数据。
     */
    @PostMapping("/deposit/submit")
    public ApiResponse<InboundOrderDTO> submitDeposit(@Valid @RequestBody SubmitInboundDepositRequest request) {
        InboundOrderDTO result = inboundFacade.submitDeposit(new SubmitInboundDepositCommand(
                request.requestBizNo(),
                request.requestBizNo(),
                null,
                request.payOrderNo(),
                request.payerUserId(),
                request.payerAccountNo(),
                request.amount(),
                request.payChannelCode(),
                request.instChannelCode(),
                request.requestIdentify(),
                request.bizIdentity()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/deposit/cancel")
    public ApiResponse<InboundOrderDTO> cancelDeposit(@Valid @RequestBody CancelInboundDepositRequest request) {
        return ApiResponse.success(inboundFacade.cancelDeposit(new CancelInboundDepositCommand(request.inboundId(), request.reason())));
    }

    /**
     * 按入金ID查询记录。
     */
    @GetMapping("/{inboundId}")
    public ApiResponse<InboundOrderDTO> queryByInboundId(@PathVariable("inboundId") String inboundId) {
        return ApiResponse.success(inboundFacade.queryByInboundId(inboundId));
    }

    /**
     * 按请求业务单号查询记录。
     */
    @GetMapping("/by-request/{requestBizNo}")
    public ApiResponse<InboundOrderDTO> queryByRequestBizNo(@PathVariable("requestBizNo") String requestBizNo) {
        return ApiResponse.success(inboundFacade.queryByRequestBizNo(requestBizNo));
    }
}
