package cn.openaipay.application.fundaccount.service;

/**
 * 基金收益分发应用服务。
 *
 * 业务场景：
 * 1. 定时任务在交易日凌晨 2 点后批量发放爱存收益。
 * 2. 用户进入爱存页面时，若当天尚未发放，则执行兜底发放。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface FundIncomeDistributionService {

    /**
     * 交易日凌晨批量发放指定基金的当日收益。
     *
     * @param fundCode 基金编码
     * @return 本次实际发放账户数
     */
    int settleTodayIncomeForFundIfNeeded(String fundCode);

    /**
     * 用户维度兜底发放当日收益。
     *
     * @param userId 用户ID
     * @param fundCode 基金编码
     * @return true 表示本次发生了实际发放；false 表示无需发放
     */
    boolean settleTodayIncomeForUserIfNeeded(Long userId, String fundCode);
}
