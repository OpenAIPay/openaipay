package cn.openaipay.adapter.deliver.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.deliver.web.request.DeliverEventRequest;
import cn.openaipay.adapter.deliver.web.request.DeliverRequest;
import cn.openaipay.application.deliver.command.DeliverCommand;
import cn.openaipay.application.deliver.command.DeliverEventCommand;
import cn.openaipay.application.deliver.dto.DeliverPositionDTO;
import cn.openaipay.application.deliver.facade.DeliverFacade;
import cn.openaipay.domain.deliver.model.DeliverEventType;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投放服务控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@RestController
@RequestMapping("/api/deliver")
public class DeliverController {

    /** 投放应用服务。 */
    private final DeliverFacade deliverFacade;

    /** DeliverController 业务接口。 */
    public DeliverController(DeliverFacade deliverFacade) {
        this.deliverFacade = deliverFacade;
    }

    /**
     * 处理投放信息。
     */
    @GetMapping
    public ApiResponse<Map<String, DeliverPositionDTO>> deliver(@Valid DeliverRequest request) {
        Map<String, DeliverPositionDTO> response = deliverFacade.deliver(new DeliverCommand(
                request.getPositionCodeList(),
                request.getClientId(),
                request.getUserId(),
                request.getSceneCode(),
                request.getChannel(),
                request.getUserTags(),
                request.getRequestTime()
        ));
        return ApiResponse.success(response);
    }

    /**
     * 记录事件信息。
     */
    @PostMapping("/events")
    public ApiResponse<Boolean> recordEvent(@Valid @RequestBody DeliverEventRequest request) {
        deliverFacade.recordEvent(new DeliverEventCommand(
                request.getClientId(),
                request.getUserId(),
                request.getSceneCode(),
                request.getChannel(),
                request.getPositionCode(),
                request.getUnitCode(),
                request.getCreativeCode(),
                request.getEventType() == null || request.getEventType().isBlank()
                        ? DeliverEventType.CLICK
                        : DeliverEventType.valueOf(request.getEventType().trim().toUpperCase()),
                request.getEventTime()
        ));
        return ApiResponse.success(Boolean.TRUE);
    }
}
