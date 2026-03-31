package cn.openaipay.infrastructure.trade;

import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeBusinessIndex;
import cn.openaipay.domain.trade.model.TradeCreditOrder;
import cn.openaipay.domain.trade.model.TradeCreditProductType;
import cn.openaipay.domain.trade.model.TradeCreditTradeType;
import cn.openaipay.domain.trade.model.TradeFlowStep;
import cn.openaipay.domain.trade.model.TradeFlowStepCode;
import cn.openaipay.domain.trade.model.TradeFlowStepStatus;
import cn.openaipay.domain.trade.model.TradeFundOrder;
import cn.openaipay.domain.trade.model.TradeFundProductType;
import cn.openaipay.domain.trade.model.TradeFundTradeType;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.model.TradeType;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.infrastructure.trade.dataobject.TradeBusinessIndexDO;
import cn.openaipay.infrastructure.trade.dataobject.TradeCreditOrderDO;
import cn.openaipay.infrastructure.trade.dataobject.TradeFlowStepDO;
import cn.openaipay.infrastructure.trade.dataobject.TradeFundOrderDO;
import cn.openaipay.infrastructure.trade.dataobject.TradeOrderDO;
import cn.openaipay.infrastructure.trade.mapper.TradeBusinessIndexMapper;
import cn.openaipay.infrastructure.trade.mapper.TradeCreditOrderMapper;
import cn.openaipay.infrastructure.trade.mapper.TradeFlowStepMapper;
import cn.openaipay.infrastructure.trade.mapper.TradeFundOrderMapper;
import cn.openaipay.infrastructure.trade.mapper.TradeOrderMapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 交易仓储实现。
 *
 * 业务场景：统一维护交易主单、流程步骤、业务扩展单和业务查询索引，保证交易编排与业务查询视图的一致性。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class TradeRepositoryImpl implements TradeRepository {
    /** 默认币种，当前爱付模拟场景统一使用人民币。 */
    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");
    /** 红包领取交易场景编码。 */
    private static final String RED_PACKET_CLAIM_SCENE_CODE = "CHAT_RED_PACKET_CLAIM";

    /** 统一交易主单持久化接口。 */
    private final TradeOrderMapper tradeOrderMapper;
    /** 交易流程步骤持久化接口。 */
    private final TradeFlowStepMapper tradeFlowStepMapper;
    /** 业务交易查询索引持久化接口。 */
    private final TradeBusinessIndexMapper tradeBusinessIndexMapper;
    /** 信用业务交易扩展单持久化接口。 */
    private final TradeCreditOrderMapper tradeCreditOrderMapper;
    /** 基金业务交易扩展单持久化接口。 */
    private final TradeFundOrderMapper tradeFundOrderMapper;

    public TradeRepositoryImpl(TradeOrderMapper tradeOrderMapper,
                               TradeFlowStepMapper tradeFlowStepMapper,
                               TradeBusinessIndexMapper tradeBusinessIndexMapper,
                               TradeCreditOrderMapper tradeCreditOrderMapper,
                               TradeFundOrderMapper tradeFundOrderMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeFlowStepMapper = tradeFlowStepMapper;
        this.tradeBusinessIndexMapper = tradeBusinessIndexMapper;
        this.tradeCreditOrderMapper = tradeCreditOrderMapper;
        this.tradeFundOrderMapper = tradeFundOrderMapper;
    }

    /**
     * 保存交易订单信息。
     */
    @Override
    @Transactional
    public TradeOrder saveTradeOrder(TradeOrder tradeOrder) {
        TradeOrderDO entity = new TradeOrderDO();
        entity.setId(tradeOrder.getId());
        fillTradeOrderDO(entity, tradeOrder);
        boolean created = false;
        if (entity.getId() == null) {
            try {
                tradeOrderMapper.insertTradeOrder(entity);
                created = true;
            } catch (DuplicateKeyException duplicateKeyException) {
                return recoverTradeOrderByRequestNo(entity, tradeOrder.getRequestNo(), duplicateKeyException);
            }
        } else {
            int updatedRows = tradeOrderMapper.updateTradeOrder(entity);
            if (updatedRows <= 0) {
                try {
                    tradeOrderMapper.insertTradeOrder(entity);
                    created = true;
                } catch (DuplicateKeyException duplicateKeyException) {
                    return recoverTradeOrderByRequestNo(entity, tradeOrder.getRequestNo(), duplicateKeyException);
                }
            }
        }
        TradeOrder savedTradeOrder = toDomainTradeOrder(entity);
        if (created) {
            insertInitialTradeBusinessIndex(savedTradeOrder);
        } else if (savedTradeOrder.isTerminal()) {
            syncTradeBusinessIndexes(savedTradeOrder, null, null);
        }
        return savedTradeOrder;
    }

    private TradeOrder recoverTradeOrderByRequestNo(TradeOrderDO entity,
                                                    String requestNo,
                                                    DuplicateKeyException duplicateKeyException) {
        return tradeOrderMapper.findByRequestNo(requestNo)
                .map(this::toDomainTradeOrder)
                .orElseThrow(() -> duplicateKeyException);
    }

    /**
     * 按交易订单单号查找交易订单信息。
     */
    @Override
    public Optional<TradeOrder> findTradeOrderByTradeOrderNo(String tradeOrderNo) {
        return tradeOrderMapper.findByTradeOrderNo(tradeOrderNo).map(this::toDomainTradeOrder);
    }

    /**
     * 按请求单号查找交易订单信息。
     */
    @Override
    public Optional<TradeOrder> findTradeOrderByRequestNo(String requestNo) {
        return tradeOrderMapper.findByRequestNo(requestNo).map(this::toDomainTradeOrder);
    }

    /**
     * 按用户ID查找交易信息。
     */
    @Override
    public List<TradeOrder> findRecentSucceededTradesByUserId(Long userId, int limit) {
        return tradeOrderMapper.findRecentSucceededByUserId(userId, limit)
                .stream()
                .map(this::toDomainTradeOrder)
                .toList();
    }

    /**
     * 汇总成功退款金额。
     */
    @Override
    public Money sumSucceededRefundAmount(String originalTradeOrderNo) {
        BigDecimal total = tradeOrderMapper.sumSucceededRefundAmount(originalTradeOrderNo);
        return toMoney(total);
    }

    /**
     * 保存流程信息。
     */
    @Override
    @Transactional
    public TradeFlowStep saveFlowStep(TradeFlowStep step) {
        TradeFlowStepDO entity = new TradeFlowStepDO();
        entity.setId(step.getId());
        fillFlowStepDO(entity, step);
        return toDomainFlowStep(tradeFlowStepMapper.save(entity));
    }

    /**
     * 查找流程信息。
     */
    @Override
    public List<TradeFlowStep> findFlowSteps(String tradeOrderNo) {
        return tradeFlowStepMapper.findByTradeOrderNoOrderByIdAsc(tradeOrderNo)
                .stream()
                .map(this::toDomainFlowStep)
                .toList();
    }

    /**
     * 保存交易索引信息。
     */
    @Override
    @Transactional
    public TradeBusinessIndex saveTradeBusinessIndex(TradeBusinessIndex tradeBusinessIndex) {
        TradeBusinessIndexDO entity = new TradeBusinessIndexDO();
        entity.setId(tradeBusinessIndex.getId());
        fillTradeBusinessIndexDO(entity, tradeBusinessIndex);
        tradeBusinessIndexMapper.upsert(entity);
        return toDomainTradeBusinessIndex(entity);
    }

    /**
     * 按交易订单单号查找交易索引信息。
     */
    @Override
    public Optional<TradeBusinessIndex> findTradeBusinessIndexByTradeOrderNo(String tradeOrderNo) {
        return tradeBusinessIndexMapper.findByTradeOrderNo(tradeOrderNo).map(this::toDomainTradeBusinessIndex);
    }

    /**
     * 按订单查找交易索引信息。
     */
    @Override
    public Optional<TradeBusinessIndex> findTradeBusinessIndexByBusinessOrder(TradeBusinessDomainCode businessDomainCode,
                                                                              String bizOrderNo) {
        return tradeBusinessIndexMapper.findByBusinessOrder(businessDomainCode.name(), bizOrderNo)
                .map(this::toDomainTradeBusinessIndex);
    }

    /**
     * 按用户ID查找交易信息。
     */
    @Override
    public List<TradeBusinessIndex> findRecentTradeBusinessIndexesByUserId(Long userId,
                                                                            String billMonth,
                                                                            TradeBusinessDomainCode businessDomainCode,
                                                                            int limit) {
        return tradeBusinessIndexMapper.findRecentByUserId(
                        userId,
                        billMonth,
                        businessDomainCode == null ? null : businessDomainCode.name(),
                        limit
                )
                .stream()
                .map(this::toDomainTradeBusinessIndex)
                .toList();
    }

    /**
     * 按用户ID分页查找交易信息。
     */
    @Override
    public List<TradeBusinessIndex> findTradeBusinessIndexesByUserId(Long userId,
                                                                      String billMonth,
                                                                      TradeBusinessDomainCode businessDomainCode,
                                                                      int offset,
                                                                      int limit) {
        return tradeBusinessIndexMapper.findByUserIdWithOffset(
                        userId,
                        billMonth,
                        businessDomainCode == null ? null : businessDomainCode.name(),
                        offset,
                        limit
                )
                .stream()
                .map(this::toDomainTradeBusinessIndex)
                .toList();
    }

    /**
     * 按用户ID和游标分页查找交易信息。
     */
    @Override
    public List<TradeBusinessIndex> findTradeBusinessIndexesByUserIdAfterCursor(Long userId,
                                                                                 String billMonth,
                                                                                 TradeBusinessDomainCode businessDomainCode,
                                                                                 LocalDateTime cursorTradeTime,
                                                                                 Long cursorId,
                                                                                 int limit) {
        return tradeBusinessIndexMapper.findByUserIdAfterCursor(
                        userId,
                        billMonth,
                        businessDomainCode == null ? null : businessDomainCode.name(),
                        cursorTradeTime,
                        cursorId,
                        limit
                )
                .stream()
                .map(this::toDomainTradeBusinessIndex)
                .toList();
    }

    /**
     * 保存交易信用订单信息。
     */
    @Override
    @Transactional
    public TradeCreditOrder saveTradeCreditOrder(TradeCreditOrder tradeCreditOrder) {
        TradeCreditOrderDO entity = tradeCreditOrderMapper.findByBizOrderNo(tradeCreditOrder.getBizOrderNo())
                .or(() -> tradeCreditOrderMapper.findByTradeOrderNo(tradeCreditOrder.getTradeOrderNo()))
                .orElse(new TradeCreditOrderDO());
        fillTradeCreditOrderDO(entity, tradeCreditOrder);
        TradeCreditOrder savedTradeCreditOrder = toDomainTradeCreditOrder(tradeCreditOrderMapper.save(entity));
        findTradeOrderByTradeOrderNo(savedTradeCreditOrder.getTradeOrderNo())
                .ifPresent(tradeOrder -> syncTradeBusinessIndex(tradeOrder, savedTradeCreditOrder, null));
        return savedTradeCreditOrder;
    }

    /**
     * 按交易订单单号查找交易信用订单信息。
     */
    @Override
    public Optional<TradeCreditOrder> findTradeCreditOrderByTradeOrderNo(String tradeOrderNo) {
        return tradeCreditOrderMapper.findByTradeOrderNo(tradeOrderNo).map(this::toDomainTradeCreditOrder);
    }

    /**
     * 按业务订单单号查找交易信用订单信息。
     */
    @Override
    public Optional<TradeCreditOrder> findTradeCreditOrderByBizOrderNo(String bizOrderNo) {
        return tradeCreditOrderMapper.findByBizOrderNo(bizOrderNo).map(this::toDomainTradeCreditOrder);
    }

    /**
     * 按条件查找交易信用订单信息。
     */
    @Override
    public Optional<TradeCreditOrder> findLatestTradeCreditOrderByAccountAndType(String creditAccountNo,
                                                                                  String creditTradeType) {
        return tradeCreditOrderMapper.findLatestByAccountAndTradeType(creditAccountNo, creditTradeType)
                .map(this::toDomainTradeCreditOrder);
    }

    /**
     * 保存交易基金订单信息。
     */
    @Override
    @Transactional
    public TradeFundOrder saveTradeFundOrder(TradeFundOrder tradeFundOrder) {
        TradeFundOrderDO entity = tradeFundOrderMapper.findByBizOrderNo(tradeFundOrder.getBizOrderNo())
                .or(() -> tradeFundOrderMapper.findByTradeOrderNo(tradeFundOrder.getTradeOrderNo()))
                .orElse(new TradeFundOrderDO());
        fillTradeFundOrderDO(entity, tradeFundOrder);
        TradeFundOrder savedTradeFundOrder = toDomainTradeFundOrder(tradeFundOrderMapper.save(entity));
        findTradeOrderByTradeOrderNo(savedTradeFundOrder.getTradeOrderNo())
                .ifPresent(tradeOrder -> syncTradeBusinessIndex(tradeOrder, null, savedTradeFundOrder));
        return savedTradeFundOrder;
    }

    /**
     * 按交易订单单号查找交易基金订单信息。
     */
    @Override
    public Optional<TradeFundOrder> findTradeFundOrderByTradeOrderNo(String tradeOrderNo) {
        return tradeFundOrderMapper.findByTradeOrderNo(tradeOrderNo).map(this::toDomainTradeFundOrder);
    }

    /**
     * 按业务订单单号查找交易基金订单信息。
     */
    @Override
    public Optional<TradeFundOrder> findTradeFundOrderByBizOrderNo(String bizOrderNo) {
        return tradeFundOrderMapper.findByBizOrderNo(bizOrderNo).map(this::toDomainTradeFundOrder);
    }

    private TradeOrder toDomainTradeOrder(TradeOrderDO entity) {
        Money originalAmount = defaultZero(entity.getOriginalAmount());
        return new TradeOrder(
                entity.getId(),
                entity.getTradeOrderNo(),
                entity.getRequestNo(),
                TradeType.from(entity.getTradeType()),
                entity.getBusinessSceneCode(),
                entity.getBusinessDomainCode(),
                entity.getBizOrderNo(),
                entity.getOriginalTradeOrderNo(),
                entity.getPayerUserId(),
                entity.getPayeeUserId(),
                entity.getPaymentMethod(),
                originalAmount,
                defaultZero(entity.getFeeAmount()),
                defaultZero(entity.getPayableAmount()),
                defaultZero(entity.getSettleAmount()),
                parseTradeSplitPlan(entity.getSplitPlanSnapshot(), originalAmount.getCurrencyUnit()),
                entity.getPricingQuoteNo(),
                entity.getPayOrderNo(),
                entity.getLastPayStatusVersion() == null ? 0 : entity.getLastPayStatusVersion(),
                entity.getPayResultCode(),
                entity.getPayResultMessage(),
                TradeStatus.from(entity.getStatus()),
                entity.getFailureReason(),
                entity.getMetadata(),
                entity.getPaymentToolSnapshot(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TradeBusinessIndex toDomainTradeBusinessIndex(TradeBusinessIndexDO entity) {
        return new TradeBusinessIndex(
                entity.getId(),
                entity.getTradeOrderNo(),
                TradeBusinessDomainCode.from(entity.getBusinessDomainCode()),
                entity.getBizOrderNo(),
                entity.getProductType(),
                entity.getBusinessType(),
                entity.getUserId(),
                entity.getCounterpartyUserId(),
                entity.getAccountNo(),
                entity.getBillNo(),
                entity.getBillMonth(),
                entity.getDisplayTitle(),
                entity.getDisplaySubtitle(),
                toMoney(entity.getAmount()),
                TradeStatus.from(entity.getStatus()),
                entity.getTradeTime(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TradeCreditOrder toDomainTradeCreditOrder(TradeCreditOrderDO entity) {
        return new TradeCreditOrder(
                entity.getId(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                TradeCreditProductType.from(entity.getCreditProductType()),
                entity.getCreditAccountNo(),
                entity.getBillNo(),
                entity.getBillMonth(),
                entity.getRepaymentPlanNo(),
                TradeCreditTradeType.from(entity.getCreditTradeType()),
                toMoney(entity.getSubjectAmount()),
                toMoney(entity.getPrincipalAmount()),
                toMoney(entity.getInterestAmount()),
                toMoney(entity.getFeeAmount()),
                entity.getCounterpartyName(),
                entity.getOccurredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TradeFundOrder toDomainTradeFundOrder(TradeFundOrderDO entity) {
        return new TradeFundOrder(
                entity.getId(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                TradeFundProductType.from(entity.getFundProductType()),
                entity.getFundAccountNo(),
                entity.getBillNo(),
                entity.getBillMonth(),
                TradeFundTradeType.from(entity.getFundTradeType()),
                entity.getShareAmount(),
                toMoney(entity.getConfirmAmount()),
                entity.getNavDate(),
                entity.getOccurredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TradeFlowStep toDomainFlowStep(TradeFlowStepDO entity) {
        return new TradeFlowStep(
                entity.getId(),
                entity.getTradeOrderNo(),
                TradeFlowStepCode.from(entity.getStepCode()),
                TradeFlowStepStatus.from(entity.getStepStatus()),
                entity.getRequestPayload(),
                entity.getResponsePayload(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillTradeOrderDO(TradeOrderDO entity, TradeOrder tradeOrder) {
        LocalDateTime now = LocalDateTime.now();
        entity.setTradeOrderNo(tradeOrder.getTradeOrderNo());
        entity.setRequestNo(tradeOrder.getRequestNo());
        entity.setTradeType(tradeOrder.getTradeType().name());
        entity.setBusinessSceneCode(tradeOrder.getBusinessSceneCode());
        entity.setBusinessDomainCode(tradeOrder.getBusinessDomainCode());
        entity.setBizOrderNo(tradeOrder.getBizOrderNo());
        entity.setOriginalTradeOrderNo(tradeOrder.getOriginalTradeOrderNo());
        entity.setPayerUserId(tradeOrder.getPayerUserId());
        entity.setPayeeUserId(tradeOrder.getPayeeUserId());
        entity.setPaymentMethod(tradeOrder.getPaymentMethod());
        entity.setOriginalAmount(defaultZero(tradeOrder.getOriginalAmount()));
        entity.setFeeAmount(defaultZero(tradeOrder.getFeeAmount()));
        entity.setPayableAmount(defaultZero(tradeOrder.getPayableAmount()));
        entity.setSettleAmount(defaultZero(tradeOrder.getSettleAmount()));
        entity.setSplitPlanSnapshot(tradeOrder.getSplitPlan().toPayload());
        entity.setPricingQuoteNo(tradeOrder.getPricingQuoteNo());
        entity.setPayOrderNo(tradeOrder.getPayOrderNo());
        entity.setLastPayStatusVersion(tradeOrder.getLastPayStatusVersion());
        entity.setPayResultCode(tradeOrder.getPayResultCode());
        entity.setPayResultMessage(tradeOrder.getPayResultMessage());
        entity.setStatus(tradeOrder.getStatus().name());
        entity.setFailureReason(tradeOrder.getFailureReason());
        entity.setMetadata(tradeOrder.getMetadata());
        entity.setPaymentToolSnapshot(tradeOrder.getPaymentToolSnapshot());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(tradeOrder.getCreatedAt() == null ? now : tradeOrder.getCreatedAt());
        }
        entity.setUpdatedAt(tradeOrder.getUpdatedAt() == null ? now : tradeOrder.getUpdatedAt());
    }

    private TradeSplitPlan parseTradeSplitPlan(String payload, CurrencyUnit currencyUnit) {
        if (payload == null || payload.isBlank()) {
            return TradeSplitPlan.empty(currencyUnit);
        }
        return TradeSplitPlan.fromPayload(payload);
    }

    private void fillTradeBusinessIndexDO(TradeBusinessIndexDO entity, TradeBusinessIndex tradeBusinessIndex) {
        LocalDateTime now = LocalDateTime.now();
        entity.setTradeOrderNo(tradeBusinessIndex.getTradeOrderNo());
        entity.setBusinessDomainCode(tradeBusinessIndex.getBusinessDomainCode().name());
        entity.setBizOrderNo(tradeBusinessIndex.getBizOrderNo());
        entity.setProductType(tradeBusinessIndex.getProductType());
        entity.setBusinessType(tradeBusinessIndex.getBusinessType());
        entity.setUserId(tradeBusinessIndex.getUserId());
        entity.setCounterpartyUserId(tradeBusinessIndex.getCounterpartyUserId());
        entity.setAccountNo(tradeBusinessIndex.getAccountNo());
        entity.setBillNo(tradeBusinessIndex.getBillNo());
        entity.setBillMonth(tradeBusinessIndex.getBillMonth());
        entity.setDisplayTitle(tradeBusinessIndex.getDisplayTitle());
        entity.setDisplaySubtitle(tradeBusinessIndex.getDisplaySubtitle());
        entity.setAmount(defaultZero(tradeBusinessIndex.getAmount()));
        entity.setStatus(tradeBusinessIndex.getStatus().name());
        entity.setTradeTime(tradeBusinessIndex.getTradeTime());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(tradeBusinessIndex.getCreatedAt() == null ? now : tradeBusinessIndex.getCreatedAt());
        }
        entity.setUpdatedAt(tradeBusinessIndex.getUpdatedAt() == null ? now : tradeBusinessIndex.getUpdatedAt());
    }

    private void fillTradeCreditOrderDO(TradeCreditOrderDO entity, TradeCreditOrder tradeCreditOrder) {
        LocalDateTime now = LocalDateTime.now();
        entity.setBizOrderNo(tradeCreditOrder.getBizOrderNo());
        entity.setTradeOrderNo(tradeCreditOrder.getTradeOrderNo());
        entity.setCreditProductType(tradeCreditOrder.getCreditProductType().name());
        entity.setCreditAccountNo(tradeCreditOrder.getCreditAccountNo());
        entity.setBillNo(tradeCreditOrder.getBillNo());
        entity.setBillMonth(tradeCreditOrder.getBillMonth());
        entity.setRepaymentPlanNo(tradeCreditOrder.getRepaymentPlanNo());
        entity.setCreditTradeType(tradeCreditOrder.getCreditTradeType().name());
        entity.setSubjectAmount(defaultZero(tradeCreditOrder.getSubjectAmount()));
        entity.setPrincipalAmount(defaultZero(tradeCreditOrder.getPrincipalAmount()));
        entity.setInterestAmount(defaultZero(tradeCreditOrder.getInterestAmount()));
        entity.setFeeAmount(defaultZero(tradeCreditOrder.getFeeAmount()));
        entity.setCounterpartyName(tradeCreditOrder.getCounterpartyName());
        entity.setOccurredAt(tradeCreditOrder.getOccurredAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(tradeCreditOrder.getCreatedAt() == null ? now : tradeCreditOrder.getCreatedAt());
        }
        entity.setUpdatedAt(tradeCreditOrder.getUpdatedAt() == null ? now : tradeCreditOrder.getUpdatedAt());
    }

    private void fillTradeFundOrderDO(TradeFundOrderDO entity, TradeFundOrder tradeFundOrder) {
        LocalDateTime now = LocalDateTime.now();
        entity.setBizOrderNo(tradeFundOrder.getBizOrderNo());
        entity.setTradeOrderNo(tradeFundOrder.getTradeOrderNo());
        entity.setFundProductType(tradeFundOrder.getFundProductType().name());
        entity.setFundAccountNo(tradeFundOrder.getFundAccountNo());
        entity.setBillNo(tradeFundOrder.getBillNo());
        entity.setBillMonth(tradeFundOrder.getBillMonth());
        entity.setFundTradeType(tradeFundOrder.getFundTradeType().name());
        entity.setShareAmount(tradeFundOrder.getShareAmount());
        entity.setConfirmAmount(defaultZero(tradeFundOrder.getConfirmAmount()));
        entity.setNavDate(tradeFundOrder.getNavDate());
        entity.setOccurredAt(tradeFundOrder.getOccurredAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(tradeFundOrder.getCreatedAt() == null ? now : tradeFundOrder.getCreatedAt());
        }
        entity.setUpdatedAt(tradeFundOrder.getUpdatedAt() == null ? now : tradeFundOrder.getUpdatedAt());
    }

    private void fillFlowStepDO(TradeFlowStepDO entity, TradeFlowStep step) {
        LocalDateTime now = LocalDateTime.now();
        entity.setTradeOrderNo(step.getTradeOrderNo());
        entity.setStepCode(step.getStepCode().name());
        entity.setStepStatus(step.getStepStatus().name());
        entity.setRequestPayload(step.getRequestPayload());
        entity.setResponsePayload(step.getResponsePayload());
        entity.setErrorMessage(step.getErrorMessage());
        entity.setStartedAt(step.getStartedAt());
        entity.setFinishedAt(step.getFinishedAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(step.getCreatedAt() == null ? now : step.getCreatedAt());
        }
        entity.setUpdatedAt(step.getUpdatedAt() == null ? now : step.getUpdatedAt());
    }

    private void insertInitialTradeBusinessIndex(TradeOrder tradeOrder) {
        TradeBusinessIndexDO entity = new TradeBusinessIndexDO();
        fillTradeBusinessIndexDO(entity, TradeBusinessIndex.fromTradeOrder(tradeOrder));
        tradeBusinessIndexMapper.insert(entity);
    }

    private void syncTradeBusinessIndexes(TradeOrder tradeOrder,
                                          TradeCreditOrder tradeCreditOrder,
                                          TradeFundOrder tradeFundOrder) {
        Long primaryOwnerUserId = resolveTradeBusinessIndexOwnerUserId(tradeOrder);
        syncTradeBusinessIndexForOwner(tradeOrder, tradeCreditOrder, tradeFundOrder, primaryOwnerUserId);
        if (shouldSyncCounterpartyTradeBusinessIndex(tradeOrder)) {
            syncTradeBusinessIndexForOwner(tradeOrder, tradeCreditOrder, tradeFundOrder, tradeOrder.getPayeeUserId());
        }
    }

    private void syncTradeBusinessIndex(TradeOrder tradeOrder,
                                        TradeCreditOrder tradeCreditOrder,
                                        TradeFundOrder tradeFundOrder) {
        Long ownerUserId = resolveTradeBusinessIndexOwnerUserId(tradeOrder);
        syncTradeBusinessIndexForOwner(tradeOrder, tradeCreditOrder, tradeFundOrder, ownerUserId);
    }

    private void syncTradeBusinessIndexForOwner(TradeOrder tradeOrder,
                                                TradeCreditOrder tradeCreditOrder,
                                                TradeFundOrder tradeFundOrder,
                                                Long ownerUserId) {
        if (tradeOrder == null || ownerUserId == null || ownerUserId <= 0) {
            return;
        }
        TradeBusinessIndex existingIndex = tradeBusinessIndexMapper
                .findByTradeOrderNoAndUserId(tradeOrder.getTradeOrderNo(), ownerUserId)
                .map(this::toDomainTradeBusinessIndex)
                .orElse(null);
        TradeBusinessIndex mergedIndex = mergeTradeBusinessIndex(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder, ownerUserId);
        saveTradeBusinessIndex(mergedIndex);
    }

    private boolean shouldSyncCounterpartyTradeBusinessIndex(TradeOrder tradeOrder) {
        if (tradeOrder == null || tradeOrder.getTradeType() != TradeType.TRANSFER) {
            return false;
        }
        Long payerUserId = tradeOrder.getPayerUserId();
        Long payeeUserId = tradeOrder.getPayeeUserId();
        return payeeUserId != null && !payeeUserId.equals(payerUserId);
    }

    private TradeBusinessIndex mergeTradeBusinessIndex(TradeBusinessIndex existingIndex,
                                                       TradeOrder tradeOrder,
                                                       TradeCreditOrder tradeCreditOrder,
                                                       TradeFundOrder tradeFundOrder,
                                                       Long ownerUserId) {
        TradeBusinessDomainCode businessDomainCode = resolveBusinessDomainCode(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder);
        String bizOrderNo = resolveBizOrderNo(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder);
        Money amount = resolveBusinessAmount(tradeOrder, tradeCreditOrder, tradeFundOrder);
        String productType = resolveProductType(existingIndex, tradeCreditOrder, tradeFundOrder, businessDomainCode);
        String businessType = resolveBusinessType(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder);
        String accountNo = resolveAccountNo(existingIndex, tradeCreditOrder, tradeFundOrder);
        String billNo = resolveBillNo(existingIndex, tradeCreditOrder, tradeFundOrder);
        String billMonth = resolveBillMonth(existingIndex, tradeCreditOrder, tradeFundOrder);
        String displayTitle = resolveDisplayTitle(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder);
        String displaySubtitle = resolveDisplaySubtitle(existingIndex, tradeOrder, tradeCreditOrder);
        LocalDateTime tradeTime = resolveTradeTime(existingIndex, tradeOrder, tradeCreditOrder, tradeFundOrder);
        LocalDateTime createdAt = existingIndex == null ? tradeOrder.getCreatedAt() : existingIndex.getCreatedAt();
        LocalDateTime updatedAt = tradeOrder.getUpdatedAt();
        Long counterpartyUserId = resolveTradeBusinessIndexCounterpartyUserId(tradeOrder, ownerUserId, existingIndex);
        String normalizedDisplayTitle = adjustDisplayTitleForOwner(displayTitle, tradeOrder, ownerUserId);
        return new TradeBusinessIndex(
                existingIndex == null ? null : existingIndex.getId(),
                tradeOrder.getTradeOrderNo(),
                businessDomainCode,
                bizOrderNo,
                productType,
                businessType,
                ownerUserId,
                counterpartyUserId,
                accountNo,
                billNo,
                billMonth,
                normalizedDisplayTitle,
                displaySubtitle,
                amount,
                tradeOrder.getStatus(),
                tradeTime,
                createdAt,
                updatedAt
        );
    }

    private String adjustDisplayTitleForOwner(String displayTitle, TradeOrder tradeOrder, Long ownerUserId) {
        if (tradeOrder == null || ownerUserId == null) {
            return displayTitle;
        }
        if (tradeOrder.getTradeType() != TradeType.TRANSFER) {
            return displayTitle;
        }
        Long payeeUserId = tradeOrder.getPayeeUserId();
        Long payerUserId = tradeOrder.getPayerUserId();
        if (payeeUserId != null && payeeUserId.equals(ownerUserId) && !ownerUserId.equals(payerUserId)) {
            return "收到转账";
        }
        return displayTitle;
    }

    private Long resolveTradeBusinessIndexOwnerUserId(TradeOrder tradeOrder) {
        if (tradeOrder == null) {
            return null;
        }
        if (isRedPacketClaimScene(tradeOrder.getBusinessSceneCode()) && tradeOrder.getPayeeUserId() != null) {
            return tradeOrder.getPayeeUserId();
        }
        return tradeOrder.getPayerUserId();
    }

    private Long resolveTradeBusinessIndexCounterpartyUserId(TradeOrder tradeOrder,
                                                             Long ownerUserId,
                                                             TradeBusinessIndex existingIndex) {
        if (tradeOrder == null) {
            return existingIndex == null ? null : existingIndex.getCounterpartyUserId();
        }
        Long payerUserId = tradeOrder.getPayerUserId();
        Long payeeUserId = tradeOrder.getPayeeUserId();
        Long counterpartyUserId;
        if (ownerUserId != null && ownerUserId.equals(payerUserId)) {
            counterpartyUserId = payeeUserId;
        } else if (ownerUserId != null && ownerUserId.equals(payeeUserId)) {
            counterpartyUserId = payerUserId;
        } else {
            counterpartyUserId = payeeUserId == null ? payerUserId : payeeUserId;
        }
        if (counterpartyUserId == null && existingIndex != null) {
            return existingIndex.getCounterpartyUserId();
        }
        return counterpartyUserId;
    }

    private boolean isRedPacketClaimScene(String businessSceneCode) {
        if (!hasText(businessSceneCode)) {
            return false;
        }
        return RED_PACKET_CLAIM_SCENE_CODE.equalsIgnoreCase(businessSceneCode);
    }

    private TradeBusinessDomainCode resolveBusinessDomainCode(TradeBusinessIndex existingIndex,
                                                              TradeOrder tradeOrder,
                                                              TradeCreditOrder tradeCreditOrder,
                                                              TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null) {
            return TradeBusinessDomainCode.from(tradeCreditOrder.getCreditProductType().name());
        }
        if (tradeFundOrder != null) {
            return TradeBusinessDomainCode.from(tradeFundOrder.getFundProductType().name());
        }
        TradeBusinessDomainCode tradeDomainCode = TradeBusinessDomainCode.from(tradeOrder.getBusinessDomainCode());
        if (existingIndex != null
                && existingIndex.getBusinessDomainCode() != TradeBusinessDomainCode.TRADE
                && tradeDomainCode == TradeBusinessDomainCode.TRADE) {
            return existingIndex.getBusinessDomainCode();
        }
        return tradeDomainCode;
    }

    private String resolveBizOrderNo(TradeBusinessIndex existingIndex,
                                          TradeOrder tradeOrder,
                                          TradeCreditOrder tradeCreditOrder,
                                          TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null) {
            return tradeCreditOrder.getBizOrderNo();
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getBizOrderNo();
        }
        if (existingIndex != null
                && hasText(existingIndex.getBizOrderNo())
                && tradeOrder.getTradeOrderNo().equals(tradeOrder.getBizOrderNo())) {
            return existingIndex.getBizOrderNo();
        }
        return tradeOrder.getBizOrderNo();
    }

    private Money resolveBusinessAmount(TradeOrder tradeOrder,
                                        TradeCreditOrder tradeCreditOrder,
                                        TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null && tradeCreditOrder.getSubjectAmount().isPositive()) {
            return tradeCreditOrder.getSubjectAmount();
        }
        if (tradeFundOrder != null && tradeFundOrder.getConfirmAmount().isPositive()) {
            return tradeFundOrder.getConfirmAmount();
        }
        return tradeOrder.getOriginalAmount();
    }

    private String resolveProductType(TradeBusinessIndex existingIndex,
                                      TradeCreditOrder tradeCreditOrder,
                                      TradeFundOrder tradeFundOrder,
                                      TradeBusinessDomainCode businessDomainCode) {
        if (tradeCreditOrder != null) {
            return tradeCreditOrder.getCreditProductType().name();
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getFundProductType().name();
        }
        if (existingIndex != null && hasText(existingIndex.getProductType())) {
            return existingIndex.getProductType();
        }
        return businessDomainCode == TradeBusinessDomainCode.TRADE ? null : businessDomainCode.name();
    }

    private String resolveBusinessType(TradeBusinessIndex existingIndex,
                                       TradeOrder tradeOrder,
                                       TradeCreditOrder tradeCreditOrder,
                                       TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null) {
            return tradeCreditOrder.getCreditTradeType().name();
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getFundTradeType().name();
        }
        if (existingIndex != null && hasText(existingIndex.getBusinessType())) {
            return existingIndex.getBusinessType();
        }
        return tradeOrder.getTradeType().name();
    }

    private String resolveAccountNo(TradeBusinessIndex existingIndex,
                                    TradeCreditOrder tradeCreditOrder,
                                    TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null) {
            return tradeCreditOrder.getCreditAccountNo();
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getFundAccountNo();
        }
        return existingIndex == null ? null : existingIndex.getAccountNo();
    }

    private String resolveBillNo(TradeBusinessIndex existingIndex,
                                 TradeCreditOrder tradeCreditOrder,
                                 TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null && hasText(tradeCreditOrder.getBillNo())) {
            return tradeCreditOrder.getBillNo();
        }
        if (tradeFundOrder != null && hasText(tradeFundOrder.getBillNo())) {
            return tradeFundOrder.getBillNo();
        }
        return existingIndex == null ? null : existingIndex.getBillNo();
    }

    private String resolveBillMonth(TradeBusinessIndex existingIndex,
                                    TradeCreditOrder tradeCreditOrder,
                                    TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null && hasText(tradeCreditOrder.getBillMonth())) {
            return tradeCreditOrder.getBillMonth();
        }
        if (tradeFundOrder != null && hasText(tradeFundOrder.getBillMonth())) {
            return tradeFundOrder.getBillMonth();
        }
        return existingIndex == null ? null : existingIndex.getBillMonth();
    }

    private String resolveDisplayTitle(TradeBusinessIndex existingIndex,
                                       TradeOrder tradeOrder,
                                       TradeCreditOrder tradeCreditOrder,
                                       TradeFundOrder tradeFundOrder) {
        if (existingIndex != null && hasText(existingIndex.getDisplayTitle())) {
            return existingIndex.getDisplayTitle();
        }
        if (tradeCreditOrder != null && hasText(tradeCreditOrder.getBillMonth())) {
            return tradeCreditOrder.getBillMonth() + " 账单";
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getFundProductType().name();
        }
        return tradeOrder.getBusinessSceneCode();
    }

    private String resolveDisplaySubtitle(TradeBusinessIndex existingIndex,
                                          TradeOrder tradeOrder,
                                          TradeCreditOrder tradeCreditOrder) {
        if (existingIndex != null && hasText(existingIndex.getDisplaySubtitle())) {
            return existingIndex.getDisplaySubtitle();
        }
        if (tradeCreditOrder != null && hasText(tradeCreditOrder.getCounterpartyName())) {
            return tradeCreditOrder.getCounterpartyName();
        }
        return tradeOrder.getPaymentMethod();
    }

    private LocalDateTime resolveTradeTime(TradeBusinessIndex existingIndex,
                                           TradeOrder tradeOrder,
                                           TradeCreditOrder tradeCreditOrder,
                                           TradeFundOrder tradeFundOrder) {
        if (tradeCreditOrder != null) {
            return tradeCreditOrder.getOccurredAt();
        }
        if (tradeFundOrder != null) {
            return tradeFundOrder.getOccurredAt();
        }
        if (existingIndex != null && existingIndex.getTradeTime() != null) {
            return existingIndex.getTradeTime();
        }
        return tradeOrder.getCreatedAt();
    }

    private boolean hasText(String raw) {
        return raw != null && !raw.isBlank();
    }

    private Money defaultZero(Money value) {
        if (value == null) {
            return zeroMoney();
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money toMoney(Money value) {
        if (value == null) {
            return zeroMoney();
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money toMoney(BigDecimal value) {
        if (value == null) {
            return zeroMoney();
        }
        return Money.of(DEFAULT_CURRENCY, value, RoundingMode.HALF_UP).rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroMoney() {
        return Money.zero(DEFAULT_CURRENCY).rounded(2, RoundingMode.HALF_UP);
    }
}
