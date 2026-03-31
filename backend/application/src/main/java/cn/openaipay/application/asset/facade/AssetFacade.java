package cn.openaipay.application.asset.facade;

import cn.openaipay.application.asset.dto.AssetOverviewDTO;

/**
 * 资产门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AssetFacade {

    /**
     * 查询用户资源概览信息。
     */
    AssetOverviewDTO queryUserAssetOverview(Long userId);
}
