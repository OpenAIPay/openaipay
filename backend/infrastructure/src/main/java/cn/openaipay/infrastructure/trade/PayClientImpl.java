package cn.openaipay.infrastructure.trade;

import cn.openaipay.application.pay.command.SettlementPlanSnapshot;
import cn.openaipay.application.pay.command.SourceBizSnapshot;
import cn.openaipay.application.pay.command.SubmitPayCommand;
import cn.openaipay.application.pay.dto.PayOrderDTO;
import cn.openaipay.application.pay.dto.PayParticipantBranchDTO;
import cn.openaipay.application.pay.dto.PaySubmitReceiptDTO;
import cn.openaipay.application.pay.dto.PayFundDetailSummaryDTO;
import cn.openaipay.application.pay.facade.PayFacade;
import cn.openaipay.domain.trade.client.PayClient;
import cn.openaipay.domain.trade.client.TradePayOrderSnapshot;
import cn.openaipay.domain.trade.client.TradePayFundDetailSnapshot;
import cn.openaipay.domain.trade.client.TradePayParticipantSnapshot;
import cn.openaipay.domain.trade.client.TradePaySubmitRequest;
import cn.openaipay.domain.trade.client.TradePaySubmitResult;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * PayClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class PayClientImpl implements PayClient {

    /** 支付信息 */
    private final PayFacade payFacade;

    public PayClientImpl(PayFacade payFacade) {
        this.payFacade = payFacade;
    }

    /**
     * 提交业务数据。
     */
    @Override
    public TradePaySubmitResult submit(TradePaySubmitRequest request) {
        PaySubmitReceiptDTO receipt = payFacade.submit(new SubmitPayCommand(
                request.sourceBizType(),
                request.sourceBizNo(),
                request.tradeOrderNo(),
                new SourceBizSnapshot(request.sourceTradeType(), request.settleAmount(), request.requiresPayeeCredit()),
                request.bizOrderNo(),
                request.businessSceneCode(),
                request.payerUserId(),
                request.payeeUserId(),
                new SettlementPlanSnapshot(
                        request.originalAmount(),
                        request.walletDebitAmount(),
                        request.fundDebitAmount(),
                        request.creditDebitAmount(),
                        request.inboundDebitAmount(),
                        request.outboundAmount(),
                        request.couponNo(),
                        request.fundCode(),
                        request.paymentToolCode(),
                        request.paymentMethod()
                )
        ));
        return new TradePaySubmitResult(
                receipt.payOrderNo(),
                receipt.bizOrderNo(),
                receipt.sourceBizType(),
                receipt.sourceBizNo(),
                receipt.attemptNo(),
                receipt.status(),
                receipt.statusVersion(),
                receipt.resultCode(),
                receipt.resultMessage(),
                receipt.createdAt(),
                receipt.updatedAt()
        );
    }

    /**
     * 按支付订单单号查询记录。
     */
    @Override
    public TradePayOrderSnapshot queryByPayOrderNo(String payOrderNo) {
        return toSnapshot(payFacade.queryByPayOrderNo(payOrderNo));
    }

    /**
     * 按业务查询记录。
     */
    @Override
    public List<TradePayOrderSnapshot> queryBySourceBiz(String sourceBizType, String sourceBizNo) {
        return payFacade.listBySourceBiz(sourceBizType, sourceBizNo)
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    /**
     * 查询业务数据。
     */
    @Override
    public TradePayParticipantSnapshot queryParticipantBranch(String payOrderNo, String participantType) {
        PayParticipantBranchDTO participant = payFacade.queryParticipantBranch(payOrderNo, participantType);
        return participant == null ? null : toParticipantSnapshot(participant);
    }

    private TradePayOrderSnapshot toSnapshot(PayOrderDTO payOrder) {
        List<TradePayParticipantSnapshot> participants = payOrder.participants() == null
                ? List.of()
                : payOrder.participants().stream().map(this::toParticipantSnapshot).toList();
        List<TradePayFundDetailSnapshot> fundDetails = payOrder.fundDetails() == null
                ? List.of()
                : payOrder.fundDetails().stream().map(this::toFundDetailSnapshot).toList();
        return new TradePayOrderSnapshot(
                payOrder.payOrderNo(),
                payOrder.tradeOrderNo(),
                payOrder.bizOrderNo(),
                payOrder.sourceBizType(),
                payOrder.sourceBizNo(),
                payOrder.attemptNo(),
                payOrder.businessSceneCode(),
                payOrder.payerUserId(),
                payOrder.payeeUserId(),
                payOrder.originalAmount(),
                payOrder.discountAmount(),
                payOrder.payableAmount(),
                payOrder.actualPaidAmount(),
                payOrder.couponNo(),
                payOrder.globalTxId(),
                payOrder.status(),
                payOrder.statusVersion(),
                payOrder.resultCode(),
                payOrder.resultMessage(),
                payOrder.failureReason(),
                payOrder.createdAt(),
                payOrder.updatedAt(),
                participants,
                fundDetails
        );
    }

    private TradePayParticipantSnapshot toParticipantSnapshot(PayParticipantBranchDTO participant) {
        return new TradePayParticipantSnapshot(
                participant.payOrderNo(),
                participant.participantType(),
                participant.branchId(),
                participant.participantResourceId(),
                participant.requestPayload(),
                participant.status(),
                participant.responseMessage(),
                participant.createdAt(),
                participant.updatedAt()
        );
    }

    private TradePayFundDetailSnapshot toFundDetailSnapshot(PayFundDetailSummaryDTO detail) {
        return new TradePayFundDetailSnapshot(
                detail.payOrderNo(),
                detail.payTool(),
                detail.detailOwner(),
                detail.amount(),
                detail.cumulativeRefundAmount(),
                detail.channel(),
                detail.bankOrderNo(),
                detail.bankCardNo(),
                detail.channelFeeAmount(),
                detail.depositOrderNo(),
                detail.instId(),
                detail.instChannelCode(),
                detail.payChannelCode(),
                detail.bankCode(),
                detail.bankName(),
                detail.cardType(),
                detail.cardHolderName(),
                detail.cardTailNo(),
                detail.toolSnapshot(),
                detail.redPacketId(),
                detail.accountNo(),
                detail.fundCode(),
                detail.fundProductCode(),
                detail.fundAccountIdentity(),
                detail.creditAccountNo(),
                detail.creditAccountType(),
                detail.creditProductCode(),
                detail.createdAt(),
                detail.updatedAt()
        );
    }
}
