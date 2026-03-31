package cn.openaipay.application.asset.facade.impl;

import cn.openaipay.application.asset.dto.AssetOverviewDTO;
import cn.openaipay.application.asset.facade.AssetFacade;
import cn.openaipay.application.asset.service.AssetService;
import org.springframework.stereotype.Service;

/**
 * 资产门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class AssetFacadeImpl implements AssetFacade {

    /** 资产应用服务组件 */
    private final AssetService assetService;

    public AssetFacadeImpl(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * 查询用户资源概览信息。
     */
    @Override
    public AssetOverviewDTO queryUserAssetOverview(Long userId) {
        return assetService.queryUserAssetOverview(userId);
    }
}
