package cn.openaipay.application.asset.service;

import cn.openaipay.application.asset.dto.AssetOverviewDTO;

/**
 * 资产应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AssetService {

    /**
     * 查询用户资源概览信息。
     */
    AssetOverviewDTO queryUserAssetOverview(Long userId);
}
