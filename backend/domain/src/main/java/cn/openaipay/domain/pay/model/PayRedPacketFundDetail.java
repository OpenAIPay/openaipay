package cn.openaipay.domain.pay.model;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 红包资金明细模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PayRedPacketFundDetail extends PayFundDetailSummary {

    /** 红包ID */
    private final String redPacketId;

    public PayRedPacketFundDetail(Long id,
                                  String payOrderNo,
                                  PayFundDetailOwner detailOwner,
                                  Money amount,
                                  Money cumulativeRefundAmount,
                                  String redPacketId,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        super(
                id,
                payOrderNo,
                PayFundDetailTool.RED_PACKET,
                detailOwner,
                amount,
                cumulativeRefundAmount,
                createdAt,
                updatedAt
        );
        this.redPacketId = normalizeRequired(redPacketId, "redPacketId");
    }

    /**
     * 创建业务数据。
     */
    public static PayRedPacketFundDetail create(String payOrderNo,
                                                PayFundDetailOwner detailOwner,
                                                Money amount,
                                                String redPacketId,
                                                LocalDateTime now) {
        return new PayRedPacketFundDetail(
                null,
                payOrderNo,
                detailOwner,
                amount,
                null,
                redPacketId,
                now,
                now
        );
    }

    /**
     * 获取RED红包ID。
     */
    public String getRedPacketId() {
        return redPacketId;
    }
}
