package cn.openaipay.adapter.asset.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.asset.dto.AssetOverviewDTO;
import cn.openaipay.application.asset.facade.AssetFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资产控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    /** AssetFacade组件 */
    private final AssetFacade assetFacade;

    public AssetController(AssetFacade assetFacade) {
        this.assetFacade = assetFacade;
    }

    /**
     * 查询用户资源概览信息。
     */
    @GetMapping("/users/{userId}/overview")
    public ApiResponse<AssetOverviewDTO> queryUserAssetOverview(@PathVariable("userId") Long userId) {
        return ApiResponse.success(assetFacade.queryUserAssetOverview(userId));
    }
}
