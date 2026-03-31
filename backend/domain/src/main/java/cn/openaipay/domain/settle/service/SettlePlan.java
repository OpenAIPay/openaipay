package cn.openaipay.domain.settle.service;

import java.util.List;

/**
 * 结算执行计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettlePlan(
        /** 状态编码 */
        SettlePlanStatus status,
        /** 消息内容 */
        String message,
        /** 主处理动作列表 */
        List<SettleWalletAction> primaryActions,
        /** 补偿动作列表 */
        List<SettleWalletAction> compensationActions,
        /** compensation成功IS失败信息 */
        boolean compensationSuccessIsFailure,
        /** 消息信息 */
        String compensationSuccessMessagePrefix
) {

    /**
     * 处理单号。
     */
    public static SettlePlan noAction() {
        return new SettlePlan(SettlePlanStatus.NO_ACTION, null, List.of(), List.of(), false, null);
    }

    /**
     * 处理业务数据。
     */
    public static SettlePlan reconPending(String message) {
        return new SettlePlan(SettlePlanStatus.RECON_PENDING, message, List.of(), List.of(), false, null);
    }

    /**
     * 处理业务数据。
     */
    public static SettlePlan execute(List<SettleWalletAction> primaryActions) {
        return new SettlePlan(
                SettlePlanStatus.EXECUTE,
                null,
                primaryActions == null ? List.of() : List.copyOf(primaryActions),
                List.of(),
                false,
                null
        );
    }

    /**
     * 处理业务数据。
     */
    public static SettlePlan executeWithCompensation(List<SettleWalletAction> primaryActions,
                                                     List<SettleWalletAction> compensationActions,
                                                     boolean compensationSuccessIsFailure,
                                                     String compensationSuccessMessagePrefix) {
        return new SettlePlan(
                SettlePlanStatus.EXECUTE,
                null,
                primaryActions == null ? List.of() : List.copyOf(primaryActions),
                compensationActions == null ? List.of() : List.copyOf(compensationActions),
                compensationSuccessIsFailure,
                compensationSuccessMessagePrefix
        );
    }
}
