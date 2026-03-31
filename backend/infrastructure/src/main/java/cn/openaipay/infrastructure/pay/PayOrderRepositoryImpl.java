package cn.openaipay.infrastructure.pay;

import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.pay.model.PayBankCardFundDetail;
import cn.openaipay.domain.pay.model.PayCreditAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundDetailOwner;
import cn.openaipay.domain.pay.model.PayFundAccountFundDetail;
import cn.openaipay.domain.pay.model.PayFundDetailSummary;
import cn.openaipay.domain.pay.model.PayFundDetailTool;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PayOrderStatus;
import cn.openaipay.domain.pay.model.PayParticipantBranch;
import cn.openaipay.domain.pay.model.PayParticipantStatus;
import cn.openaipay.domain.pay.model.PayParticipantType;
import cn.openaipay.domain.pay.model.PayRedPacketFundDetail;
import cn.openaipay.domain.pay.model.PaySplitPlan;
import cn.openaipay.domain.pay.model.PayWalletFundDetail;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import cn.openaipay.infrastructure.pay.dataobject.PayBankCardFundDetailDO;
import cn.openaipay.infrastructure.pay.dataobject.PayCreditAccountFundDetailDO;
import cn.openaipay.infrastructure.pay.dataobject.PayFundDetailSummaryDO;
import cn.openaipay.infrastructure.pay.dataobject.PayFundAccountFundDetailDO;
import cn.openaipay.infrastructure.pay.dataobject.PayOrderDO;
import cn.openaipay.infrastructure.pay.dataobject.PayParticipantBranchDO;
import cn.openaipay.infrastructure.pay.dataobject.PayRedPacketFundDetailDO;
import cn.openaipay.infrastructure.pay.dataobject.PayWalletFundDetailDO;
import cn.openaipay.infrastructure.pay.mapper.PayBankCardFundDetailMapper;
import cn.openaipay.infrastructure.pay.mapper.PayCreditAccountFundDetailMapper;
import cn.openaipay.infrastructure.pay.mapper.PayFundAccountFundDetailMapper;
import cn.openaipay.infrastructure.pay.mapper.PayFundDetailSummaryMapper;
import cn.openaipay.infrastructure.pay.mapper.PayOrderMapper;
import cn.openaipay.infrastructure.pay.mapper.PayParticipantBranchMapper;
import cn.openaipay.infrastructure.pay.mapper.PayRedPacketFundDetailMapper;
import cn.openaipay.infrastructure.pay.mapper.PayWalletFundDetailMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 支付订单仓储实现
 *
 * 业务场景：支付域统一落库支付订单、参与方分支和资金明细。
 * 迁移到MyBatis-Plus后，资金明细由摘要表+扩展明细表显式维护，替代JPA继承联表。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class PayOrderRepositoryImpl implements PayOrderRepository {

    /** PayOrderMapper组件 */
    private final PayOrderMapper payOrderMapper;
    /** Pay参与方分支Persistence组件 */
    private final PayParticipantBranchMapper payParticipantBranchMapper;
    /** Pay资金明细摘要Persistence组件 */
    private final PayFundDetailSummaryMapper payFundDetailSummaryMapper;
    /** 银行卡资金明细Persistence组件 */
    private final PayBankCardFundDetailMapper payBankCardFundDetailMapper;
    /** 红包资金明细Persistence组件 */
    private final PayRedPacketFundDetailMapper payRedPacketFundDetailMapper;
    /** 钱包资金明细Persistence组件 */
    private final PayWalletFundDetailMapper payWalletFundDetailMapper;
    /** 基金账户资金明细Persistence组件 */
    private final PayFundAccountFundDetailMapper payFundAccountFundDetailMapper;
    /** 信用账户资金明细Persistence组件 */
    private final PayCreditAccountFundDetailMapper payCreditAccountFundDetailMapper;

    public PayOrderRepositoryImpl(PayOrderMapper payOrderMapper,
                                  PayParticipantBranchMapper payParticipantBranchMapper,
                                  PayFundDetailSummaryMapper payFundDetailSummaryMapper,
                                  PayBankCardFundDetailMapper payBankCardFundDetailMapper,
                                  PayRedPacketFundDetailMapper payRedPacketFundDetailMapper,
                                  PayWalletFundDetailMapper payWalletFundDetailMapper,
                                  PayFundAccountFundDetailMapper payFundAccountFundDetailMapper,
                                  PayCreditAccountFundDetailMapper payCreditAccountFundDetailMapper) {
        this.payOrderMapper = payOrderMapper;
        this.payParticipantBranchMapper = payParticipantBranchMapper;
        this.payFundDetailSummaryMapper = payFundDetailSummaryMapper;
        this.payBankCardFundDetailMapper = payBankCardFundDetailMapper;
        this.payRedPacketFundDetailMapper = payRedPacketFundDetailMapper;
        this.payWalletFundDetailMapper = payWalletFundDetailMapper;
        this.payFundAccountFundDetailMapper = payFundAccountFundDetailMapper;
        this.payCreditAccountFundDetailMapper = payCreditAccountFundDetailMapper;
    }

    /**
     * 保存订单信息。
     */
    @Override
    @Transactional
    public PayOrder saveOrder(PayOrder payOrder) {
        PayOrderDO entity = new PayOrderDO();
        fillOrderDO(entity, payOrder);
        if (payOrder.getId() != null) {
            entity.setId(payOrder.getId());
            return toDomainOrder(payOrderMapper.save(entity));
        }

        try {
            payOrderMapper.insert(entity);
            return toDomainOrder(entity);
        } catch (DuplicateKeyException duplicateKeyException) {
            UpdateWrapper<PayOrderDO> wrapper = new UpdateWrapper<>();
            wrapper.eq("pay_order_no", payOrder.getPayOrderNo());
            int updatedRows = payOrderMapper.update(entity, wrapper);
            if (updatedRows <= 0) {
                throw duplicateKeyException;
            }
            return payOrderMapper.findByPayOrderNo(payOrder.getPayOrderNo())
                    .map(this::toDomainOrder)
                    .orElseThrow(() -> duplicateKeyException);
        }
    }

    /**
     * 按支付订单单号查找订单信息。
     */
    @Override
    public Optional<PayOrder> findOrderByPayOrderNo(String payOrderNo) {
        return payOrderMapper.findByPayOrderNo(payOrderNo).map(this::toDomainOrder);
    }

    /**
     * 按交易订单号查找最新订单信息。
     */
    @Override
    public Optional<PayOrder> findLatestOrderByTradeOrderNo(String tradeOrderNo) {
        return payOrderMapper.findLatestByTradeOrderNo(tradeOrderNo).map(this::toDomainOrder);
    }

    /**
     * 按业务订单单号查找订单信息。
     */
    @Override
    public Optional<PayOrder> findOrderByBizOrderNo(String bizOrderNo) {
        return payOrderMapper.findByBizOrderNo(bizOrderNo).map(this::toDomainOrder);
    }

    /**
     * 批量按交易订单号查找最新订单信息。
     */
    @Override
    public List<PayOrder> findLatestOrdersByTradeOrderNos(List<String> tradeOrderNos) {
        List<String> normalizedTradeOrderNos = normalizeLookupKeys(tradeOrderNos);
        if (normalizedTradeOrderNos.isEmpty()) {
            return List.of();
        }
        List<PayOrderDO> entities = payOrderMapper.findByTradeOrderNos(normalizedTradeOrderNos);
        Map<String, PayOrderDO> latestByTradeOrderNo = new LinkedHashMap<>();
        for (PayOrderDO entity : entities) {
            String tradeOrderNo = normalizeOptional(entity.getTradeOrderNo());
            if (tradeOrderNo != null) {
                latestByTradeOrderNo.putIfAbsent(tradeOrderNo, entity);
            }
        }
        return latestByTradeOrderNo.values().stream()
                .map(this::toDomainOrder)
                .toList();
    }

    /**
     * 批量按业务订单号查找订单信息。
     */
    @Override
    public List<PayOrder> findOrdersByBizOrderNos(List<String> bizOrderNos) {
        List<String> normalizedBizOrderNos = normalizeLookupKeys(bizOrderNos);
        if (normalizedBizOrderNos.isEmpty()) {
            return List.of();
        }
        List<PayOrderDO> entities = payOrderMapper.findByBizOrderNos(normalizedBizOrderNos);
        Map<String, PayOrderDO> orderByBizOrderNo = new LinkedHashMap<>();
        for (PayOrderDO entity : entities) {
            String bizOrderNo = normalizeOptional(entity.getBizOrderNo());
            if (bizOrderNo != null) {
                orderByBizOrderNo.putIfAbsent(bizOrderNo, entity);
            }
        }
        return orderByBizOrderNo.values().stream()
                .map(this::toDomainOrder)
                .toList();
    }

    /**
     * 批量按支付订单号查找订单信息。
     */
    @Override
    public List<PayOrder> findOrdersByPayOrderNos(List<String> payOrderNos) {
        List<String> normalizedPayOrderNos = normalizeLookupKeys(payOrderNos);
        if (normalizedPayOrderNos.isEmpty()) {
            return List.of();
        }
        List<PayOrderDO> entities = payOrderMapper.findByPayOrderNos(normalizedPayOrderNos);
        Map<String, PayOrderDO> orderByPayOrderNo = new LinkedHashMap<>();
        for (PayOrderDO entity : entities) {
            String payOrderNo = normalizeOptional(entity.getPayOrderNo());
            if (payOrderNo != null) {
                orderByPayOrderNo.putIfAbsent(payOrderNo, entity);
            }
        }
        return orderByPayOrderNo.values().stream()
                .map(this::toDomainOrder)
                .toList();
    }

    /**
     * 批量按来源业务类型和来源业务单号查找最新订单信息。
     */
    @Override
    public List<PayOrder> findLatestOrdersBySourceBizNos(String sourceBizType, List<String> sourceBizNos) {
        String normalizedSourceBizType = normalizeOptional(sourceBizType);
        List<String> normalizedSourceBizNos = normalizeLookupKeys(sourceBizNos);
        if (normalizedSourceBizType == null || normalizedSourceBizNos.isEmpty()) {
            return List.of();
        }
        List<PayOrderDO> entities = payOrderMapper.findBySourceBizNos(normalizedSourceBizType, normalizedSourceBizNos);
        Map<String, PayOrderDO> latestBySourceBizNo = new LinkedHashMap<>();
        for (PayOrderDO entity : entities) {
            String sourceBizNo = normalizeOptional(entity.getSourceBizNo());
            if (sourceBizNo != null) {
                latestBySourceBizNo.putIfAbsent(sourceBizNo, entity);
            }
        }
        return latestBySourceBizNo.values().stream()
                .map(this::toDomainOrder)
                .toList();
    }

    /**
     * 按业务查找订单信息。
     */
    @Override
    public Optional<PayOrder> findOrderBySourceBiz(String sourceBizType, String sourceBizNo) {
        return findLatestOrderBySourceBiz(sourceBizType, sourceBizNo);
    }

    /**
     * 按业务查找订单信息。
     */
    @Override
    public Optional<PayOrder> findLatestOrderBySourceBiz(String sourceBizType, String sourceBizNo) {
        return payOrderMapper.findLatestBySourceBiz(sourceBizType, sourceBizNo).map(this::toDomainOrder);
    }

    /**
     * 按业务与单号查找订单信息。
     */
    @Override
    public Optional<PayOrder> findOrderBySourceBizAndAttemptNo(String sourceBizType, String sourceBizNo, int attemptNo) {
        return payOrderMapper.findBySourceBizAndAttemptNo(sourceBizType, sourceBizNo, attemptNo).map(this::toDomainOrder);
    }

    /**
     * 按业务查找订单信息。
     */
    @Override
    public List<PayOrder> findOrdersBySourceBiz(String sourceBizType, String sourceBizNo) {
        return payOrderMapper.findAllBySourceBiz(sourceBizType, sourceBizNo)
                .stream()
                .map(this::toDomainOrder)
                .toList();
    }

    /**
     * 查找订单信息。
     */
    @Override
    public List<PayOrder> findReconPendingOrders(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        return payOrderMapper.findReconPendingOrders(normalizedLimit)
                .stream()
                .map(this::toDomainOrder)
                .toList();
    }

    private List<String> normalizeLookupKeys(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            String normalized = normalizeOptional(rawValue);
            if (normalized != null) {
                deduplicated.add(normalized);
            }
        }
        if (deduplicated.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(deduplicated);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public PayParticipantBranch saveParticipantBranch(PayParticipantBranch branch) {
        PayParticipantBranchDO entity = new PayParticipantBranchDO();
        fillBranchDO(entity, branch);
        if (branch.getId() != null) {
            entity.setId(branch.getId());
            return toDomainBranch(payParticipantBranchMapper.save(entity));
        }

        try {
            payParticipantBranchMapper.insert(entity);
            return toDomainBranch(entity);
        } catch (DuplicateKeyException duplicateKeyException) {
            UpdateWrapper<PayParticipantBranchDO> wrapper = new UpdateWrapper<>();
            wrapper.eq("pay_order_no", branch.getPayOrderNo());
            wrapper.eq("participant_type", branch.getParticipantType().name());
            int updatedRows = payParticipantBranchMapper.update(entity, wrapper);
            if (updatedRows <= 0) {
                throw duplicateKeyException;
            }
            return payParticipantBranchMapper
                    .findByPayOrderNoAndParticipantType(branch.getPayOrderNo(), branch.getParticipantType().name())
                    .map(this::toDomainBranch)
                    .orElseThrow(() -> duplicateKeyException);
        }
    }

    /**
     * 查找业务数据。
     */
    @Override
    public List<PayParticipantBranch> findParticipantBranches(String payOrderNo) {
        return payParticipantBranchMapper.findByPayOrderNoOrderByIdAsc(payOrderNo)
                .stream()
                .map(this::toDomainBranch)
                .toList();
    }

    /**
     * 查找业务数据。
     */
    @Override
    public Optional<PayParticipantBranch> findParticipantBranch(String payOrderNo, String participantType) {
        return payParticipantBranchMapper.findByPayOrderNoAndParticipantType(payOrderNo, participantType)
                .map(this::toDomainBranch);
    }

    /**
     * 保存基金明细信息。
     */
    @Override
    @Transactional
    public PayFundDetailSummary saveFundDetail(PayFundDetailSummary fundDetail) {
        PayFundDetailSummaryDO summaryDO = new PayFundDetailSummaryDO();
        summaryDO.setId(fundDetail.getId());
        fillFundDetailSummaryDO(summaryDO, fundDetail);
        PayFundDetailSummaryDO savedSummary = payFundDetailSummaryMapper.save(summaryDO);
        saveFundDetailExtension(savedSummary.getId(), fundDetail);
        return toSavedFundDetail(savedSummary, fundDetail);
    }

    /**
     * 查找基金明细信息。
     */
    @Override
    public List<PayFundDetailSummary> findFundDetails(String payOrderNo) {
        return payFundDetailSummaryMapper.findByPayOrderNoOrderByIdAsc(payOrderNo)
                .stream()
                .map(this::toDomainFundDetail)
                .toList();
    }

    /**
     * 查找基金明细信息。
     */
    @Override
    public Optional<PayFundDetailSummary> findFundDetail(String payOrderNo,
                                                         PayFundDetailTool payTool,
                                                         PayFundDetailOwner detailOwner) {
        return payFundDetailSummaryMapper
                .findByPayOrderNoAndPayToolAndDetailOwner(payOrderNo, payTool.name(), detailOwner.name())
                .map(this::toDomainFundDetail);
    }

    private PayOrder toDomainOrder(PayOrderDO entity) {
        PaySplitPlan splitPlan = resolveSplitPlan(entity);
        return new PayOrder(
                entity.getId(),
                entity.getPayOrderNo(),
                entity.getTradeOrderNo(),
                entity.getBizOrderNo(),
                entity.getSourceBizType(),
                entity.getSourceBizNo(),
                entity.getAttemptNo() == null ? 1 : entity.getAttemptNo(),
                entity.getSourceBizSnapshot(),
                entity.getBusinessSceneCode(),
                entity.getPayerUserId(),
                entity.getPayeeUserId(),
                entity.getOriginalAmount(),
                entity.getDiscountAmount(),
                entity.getPayableAmount(),
                entity.getActualPaidAmount(),
                splitPlan,
                entity.getCouponNo(),
                entity.getSettlementPlanSnapshot(),
                entity.getGlobalTxId(),
                PayOrderStatus.from(entity.getStatus()),
                entity.getStatusVersion() == null ? 0 : entity.getStatusVersion(),
                entity.getResultCode(),
                entity.getResultMessage(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PayParticipantBranch toDomainBranch(PayParticipantBranchDO entity) {
        return new PayParticipantBranch(
                entity.getId(),
                entity.getPayOrderNo(),
                PayParticipantType.from(entity.getParticipantType()),
                entity.getBranchId(),
                entity.getParticipantResourceId(),
                entity.getRequestPayload(),
                PayParticipantStatus.from(entity.getStatus()),
                entity.getResponseMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PayFundDetailSummary toDomainFundDetail(PayFundDetailSummaryDO summaryDO) {
        PayFundDetailTool tool = PayFundDetailTool.from(summaryDO.getPayTool());
        PayFundDetailOwner owner = PayFundDetailOwner.from(summaryDO.getDetailOwner());
        Long summaryId = summaryDO.getId();
        if (summaryId == null) {
            throw new IllegalStateException("pay fund detail summary id must not be null");
        }
        if (tool == PayFundDetailTool.BANK_CARD) {
            PayBankCardFundDetailDO bankDO = payBankCardFundDetailMapper.findBySummaryId(summaryId)
                    .orElseThrow(() -> new IllegalStateException("bank card fund detail missing, summaryId=" + summaryId));
            return new PayBankCardFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    owner,
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    bankDO.getChannel(),
                    bankDO.getInstId(),
                    bankDO.getInstChannelCode(),
                    bankDO.getPayChannelCode(),
                    bankDO.getBankCode(),
                    bankDO.getBankName(),
                    bankDO.getCardType(),
                    bankDO.getCardHolderName(),
                    bankDO.getCardTailNo(),
                    bankDO.getToolSnapshot(),
                    bankDO.getBankOrderNo(),
                    bankDO.getBankCardNo(),
                    bankDO.getChannelFeeAmount(),
                    bankDO.getDepositOrderNo(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (tool == PayFundDetailTool.RED_PACKET) {
            PayRedPacketFundDetailDO redPacketDO = payRedPacketFundDetailMapper.findBySummaryId(summaryId)
                    .orElseThrow(() -> new IllegalStateException("red packet fund detail missing, summaryId=" + summaryId));
            return new PayRedPacketFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    owner,
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    redPacketDO.getRedPacketId(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (tool == PayFundDetailTool.WALLET) {
            PayWalletFundDetailDO walletDO = payWalletFundDetailMapper.findBySummaryId(summaryId)
                    .orElseThrow(() -> new IllegalStateException("wallet fund detail missing, summaryId=" + summaryId));
            return new PayWalletFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    owner,
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    walletDO.getAccountNo(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (tool == PayFundDetailTool.FUND) {
            PayFundAccountFundDetailDO fundDO = payFundAccountFundDetailMapper.findBySummaryId(summaryId)
                    .orElseThrow(() -> new IllegalStateException("fund account detail missing, summaryId=" + summaryId));
            return new PayFundAccountFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    owner,
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    fundDO.getFundCode(),
                    fundDO.getFundProductCode(),
                    fundDO.getAccountIdentity(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (tool == PayFundDetailTool.CREDIT) {
            PayCreditAccountFundDetailDO creditDO = payCreditAccountFundDetailMapper.findBySummaryId(summaryId)
                    .orElseThrow(() -> new IllegalStateException("credit account detail missing, summaryId=" + summaryId));
            return new PayCreditAccountFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    owner,
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    creditDO.getAccountNo(),
                    parseCreditAccountType(creditDO.getCreditAccountType(), creditDO.getAccountNo()),
                    creditDO.getCreditProductCode(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        throw new IllegalStateException("unsupported pay fund detail tool: " + tool.name());
    }

    private PayFundDetailSummary toSavedFundDetail(PayFundDetailSummaryDO summaryDO, PayFundDetailSummary fundDetail) {
        Long summaryId = summaryDO.getId();
        if (summaryId == null) {
            throw new IllegalStateException("pay fund detail summary id must not be null");
        }
        if (fundDetail instanceof PayBankCardFundDetail bankDetail) {
            return new PayBankCardFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    bankDetail.getDetailOwner(),
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    bankDetail.getChannel(),
                    bankDetail.getInstId(),
                    bankDetail.getInstChannelCode(),
                    bankDetail.getPayChannelCode(),
                    bankDetail.getBankCode(),
                    bankDetail.getBankName(),
                    bankDetail.getCardType(),
                    bankDetail.getCardHolderName(),
                    bankDetail.getCardTailNo(),
                    bankDetail.getToolSnapshot(),
                    bankDetail.getBankOrderNo(),
                    bankDetail.getBankCardNo(),
                    bankDetail.getChannelFeeAmount(),
                    bankDetail.getDepositOrderNo(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayRedPacketFundDetail redPacketDetail) {
            return new PayRedPacketFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    redPacketDetail.getDetailOwner(),
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    redPacketDetail.getRedPacketId(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayWalletFundDetail walletDetail) {
            return new PayWalletFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    walletDetail.getDetailOwner(),
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    walletDetail.getAccountNo(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayFundAccountFundDetail fundAccountDetail) {
            return new PayFundAccountFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    fundAccountDetail.getDetailOwner(),
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    fundAccountDetail.getFundCode(),
                    fundAccountDetail.getFundProductCode(),
                    fundAccountDetail.getAccountIdentity(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        if (fundDetail instanceof PayCreditAccountFundDetail creditDetail) {
            return new PayCreditAccountFundDetail(
                    summaryId,
                    summaryDO.getPayOrderNo(),
                    creditDetail.getDetailOwner(),
                    summaryDO.getAmount(),
                    summaryDO.getCumulativeRefundAmount(),
                    creditDetail.getAccountNo(),
                    creditDetail.getCreditAccountType(),
                    creditDetail.getCreditProductCode(),
                    summaryDO.getCreatedAt(),
                    summaryDO.getUpdatedAt()
            );
        }
        throw new IllegalStateException("unsupported pay fund detail type: " + fundDetail.getClass().getName());
    }

    private void fillOrderDO(PayOrderDO entity, PayOrder payOrder) {
        LocalDateTime now = LocalDateTime.now();
        PaySplitPlan splitPlan = payOrder.getSplitPlan();
        entity.setPayOrderNo(payOrder.getPayOrderNo());
        entity.setTradeOrderNo(payOrder.getTradeOrderNo());
        entity.setBizOrderNo(payOrder.getBizOrderNo());
        entity.setSourceBizType(payOrder.getSourceBizType());
        entity.setSourceBizNo(payOrder.getSourceBizNo());
        entity.setAttemptNo(payOrder.getAttemptNo());
        entity.setSourceBizSnapshot(payOrder.getSourceBizSnapshot());
        entity.setBusinessSceneCode(payOrder.getBusinessSceneCode());
        entity.setPayerUserId(payOrder.getPayerUserId());
        entity.setPayeeUserId(payOrder.getPayeeUserId());
        entity.setOriginalAmount(payOrder.getOriginalAmount());
        entity.setDiscountAmount(payOrder.getDiscountAmount());
        entity.setPayableAmount(payOrder.getPayableAmount());
        entity.setActualPaidAmount(payOrder.getActualPaidAmount());
        entity.setSplitPlanSnapshot(splitPlan.toPayload());
        entity.setCouponNo(payOrder.getCouponNo());
        entity.setSettlementPlanSnapshot(payOrder.getSettlementPlanSnapshot());
        entity.setGlobalTxId(payOrder.getGlobalTxId());
        entity.setStatus(payOrder.getStatus().name());
        entity.setStatusVersion(payOrder.getStatusVersion());
        entity.setResultCode(payOrder.getResultCode());
        entity.setResultMessage(payOrder.getResultMessage());
        entity.setFailureReason(payOrder.getFailureReason());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(payOrder.getCreatedAt() == null ? now : payOrder.getCreatedAt());
        }
        entity.setUpdatedAt(payOrder.getUpdatedAt() == null ? now : payOrder.getUpdatedAt());
    }

    private void fillBranchDO(PayParticipantBranchDO entity, PayParticipantBranch branch) {
        LocalDateTime now = LocalDateTime.now();
        entity.setPayOrderNo(branch.getPayOrderNo());
        entity.setParticipantType(branch.getParticipantType().name());
        entity.setBranchId(branch.getBranchId());
        entity.setParticipantResourceId(branch.getParticipantResourceId());
        entity.setRequestPayload(branch.getRequestPayload());
        entity.setStatus(branch.getStatus().name());
        entity.setResponseMessage(branch.getResponseMessage());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(branch.getCreatedAt() == null ? now : branch.getCreatedAt());
        }
        entity.setUpdatedAt(branch.getUpdatedAt() == null ? now : branch.getUpdatedAt());
    }

    private void fillFundDetailSummaryDO(PayFundDetailSummaryDO summaryDO, PayFundDetailSummary fundDetail) {
        LocalDateTime now = LocalDateTime.now();
        summaryDO.setPayOrderNo(fundDetail.getPayOrderNo());
        summaryDO.setPayTool(fundDetail.getPayTool().name());
        summaryDO.setDetailOwner(fundDetail.getDetailOwner().name());
        summaryDO.setAmount(fundDetail.getAmount());
        summaryDO.setCumulativeRefundAmount(fundDetail.getCumulativeRefundAmount());
        summaryDO.setDetailType(resolveDetailType(fundDetail));
        if (summaryDO.getCreatedAt() == null) {
            summaryDO.setCreatedAt(fundDetail.getCreatedAt() == null ? now : fundDetail.getCreatedAt());
        }
        summaryDO.setUpdatedAt(fundDetail.getUpdatedAt() == null ? now : fundDetail.getUpdatedAt());
    }

    private String resolveDetailType(PayFundDetailSummary fundDetail) {
        if (fundDetail instanceof PayBankCardFundDetail) {
            return "PAY_BANK_CARD_FUND_DETAIL";
        }
        if (fundDetail instanceof PayRedPacketFundDetail) {
            return "PAY_RED_PACKET_FUND_DETAIL";
        }
        if (fundDetail instanceof PayWalletFundDetail) {
            return "PAY_WALLET_FUND_DETAIL";
        }
        if (fundDetail instanceof PayFundAccountFundDetail) {
            return "PAY_FUND_ACCOUNT_FUND_DETAIL";
        }
        if (fundDetail instanceof PayCreditAccountFundDetail) {
            return "PAY_CREDIT_ACCOUNT_FUND_DETAIL";
        }
        throw new IllegalStateException("unsupported pay fund detail type: " + fundDetail.getClass().getName());
    }

    private void saveFundDetailExtension(Long summaryId, PayFundDetailSummary fundDetail) {
        if (summaryId == null) {
            throw new IllegalStateException("pay fund detail summary id must not be null");
        }
        if (fundDetail instanceof PayBankCardFundDetail bankDetail) {
            PayBankCardFundDetailDO bankDO = new PayBankCardFundDetailDO();
            bankDO.setSummaryId(summaryId);
            bankDO.setChannel(bankDetail.getChannel());
            bankDO.setInstId(bankDetail.getInstId());
            bankDO.setInstChannelCode(bankDetail.getInstChannelCode());
            bankDO.setPayChannelCode(bankDetail.getPayChannelCode());
            bankDO.setBankCode(bankDetail.getBankCode());
            bankDO.setBankName(bankDetail.getBankName());
            bankDO.setCardType(bankDetail.getCardType());
            bankDO.setCardHolderName(bankDetail.getCardHolderName());
            bankDO.setCardTailNo(bankDetail.getCardTailNo());
            bankDO.setToolSnapshot(bankDetail.getToolSnapshot());
            bankDO.setBankOrderNo(bankDetail.getBankOrderNo());
            bankDO.setBankCardNo(bankDetail.getBankCardNo());
            bankDO.setChannelFeeAmount(bankDetail.getChannelFeeAmount());
            bankDO.setDepositOrderNo(bankDetail.getDepositOrderNo());
            payBankCardFundDetailMapper.save(bankDO);
            return;
        }
        if (fundDetail instanceof PayRedPacketFundDetail redPacketDetail) {
            PayRedPacketFundDetailDO redPacketDO = new PayRedPacketFundDetailDO();
            redPacketDO.setSummaryId(summaryId);
            redPacketDO.setRedPacketId(redPacketDetail.getRedPacketId());
            payRedPacketFundDetailMapper.save(redPacketDO);
            return;
        }
        if (fundDetail instanceof PayWalletFundDetail walletDetail) {
            PayWalletFundDetailDO walletDO = new PayWalletFundDetailDO();
            walletDO.setSummaryId(summaryId);
            walletDO.setAccountNo(walletDetail.getAccountNo());
            payWalletFundDetailMapper.save(walletDO);
            return;
        }
        if (fundDetail instanceof PayFundAccountFundDetail fundAccountDetail) {
            PayFundAccountFundDetailDO fundDO = new PayFundAccountFundDetailDO();
            fundDO.setSummaryId(summaryId);
            fundDO.setFundCode(fundAccountDetail.getFundCode());
            fundDO.setFundProductCode(fundAccountDetail.getFundProductCode());
            fundDO.setAccountIdentity(fundAccountDetail.getAccountIdentity());
            payFundAccountFundDetailMapper.save(fundDO);
            return;
        }
        if (fundDetail instanceof PayCreditAccountFundDetail creditDetail) {
            PayCreditAccountFundDetailDO creditDO = new PayCreditAccountFundDetailDO();
            creditDO.setSummaryId(summaryId);
            creditDO.setAccountNo(creditDetail.getAccountNo());
            creditDO.setCreditAccountType(creditDetail.getCreditAccountType().name());
            creditDO.setCreditProductCode(creditDetail.getCreditProductCode());
            payCreditAccountFundDetailMapper.save(creditDO);
            return;
        }
        throw new IllegalStateException("unsupported pay fund detail type: " + fundDetail.getClass().getName());
    }

    private CreditAccountType parseCreditAccountType(String rawType, String accountNo) {
        if (rawType == null || rawType.isBlank()) {
            return CreditAccountType.fromAccountNo(accountNo);
        }
        return CreditAccountType.valueOf(rawType.trim().toUpperCase());
    }

    private PaySplitPlan resolveSplitPlan(PayOrderDO entity) {
        String splitPlanSnapshot = normalizeOptional(entity.getSplitPlanSnapshot());
        if (splitPlanSnapshot != null) {
            return PaySplitPlan.fromPayload(splitPlanSnapshot);
        }
        String settlementPlanSnapshot = normalizeOptional(entity.getSettlementPlanSnapshot());
        if (settlementPlanSnapshot != null) {
            return splitPlanFromSettlementSnapshot(settlementPlanSnapshot, entity.getOriginalAmount().getCurrencyUnit());
        }
        throw new IllegalStateException("split plan snapshot missing, payOrderNo=" + entity.getPayOrderNo());
    }

    private PaySplitPlan splitPlanFromSettlementSnapshot(String snapshot, CurrencyUnit fallbackCurrency) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(snapshot);
        CurrencyUnit currencyUnit = fallbackCurrency == null ? CurrencyUnit.of("CNY") : fallbackCurrency;
        return PaySplitPlan.of(
                currencyUnit,
                moneyValue(raw.get("walletDebitAmount"), currencyUnit),
                moneyValue(raw.get("fundDebitAmount"), currencyUnit),
                moneyValue(raw.get("creditDebitAmount"), currencyUnit),
                moneyValue(raw.get("inboundDebitAmount"), currencyUnit)
        );
    }

    private Money moneyValue(Object rawValue, CurrencyUnit currencyUnit) {
        if (rawValue == null) {
            return Money.zero(currencyUnit);
        }
        return Money.of(currencyUnit, new BigDecimal(String.valueOf(rawValue)));
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
