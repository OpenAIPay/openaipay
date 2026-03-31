package cn.openaipay.application.fundaccount.facade.impl;

import cn.openaipay.application.fundaccount.command.CreateFundAccountCommand;
import cn.openaipay.application.fundaccount.command.FundFastRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundIncomeSettleCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCancelCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchConfirmCommand;
import cn.openaipay.application.fundaccount.command.PublishFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.SettleFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.UpsertFundProductCommand;
import cn.openaipay.application.fundaccount.facade.FundAccountFacade;
import cn.openaipay.application.fundaccount.facade.FundFreezeResult;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.dto.FundIncomeCalendarDTO;
import cn.openaipay.application.fundaccount.dto.FundProductDTO;
import cn.openaipay.application.fundaccount.dto.FundTradeBackfillResultDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDetailDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskCheckContext;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.model.RiskSceneCode;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeFundOrder;
import cn.openaipay.domain.trade.model.TradeFundProductType;
import cn.openaipay.domain.trade.model.TradeFundTradeType;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.model.TradeType;
import cn.openaipay.domain.trade.repository.TradeRepository;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import cn.openaipay.domain.shared.number.FundAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基金账户门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class FundAccountFacadeImpl implements FundAccountFacade {
    /** 资金编码 */
    private static final String DEFAULT_FUND_CODE = FundProductCodes.DEFAULT_FUND_CODE;
    /** 业务编码 */
    private static final String DEFAULT_CURRENCY_CODE = "CNY";
    /** 支付场景编码 */
    private static final String PAY_FREEZE_SCENE_CODE = "FUND_PAY_FREEZE";
    /** 默认信息 */
    private static final int DEFAULT_BACKFILL_LIMIT = 500;
    /** 最大信息 */
    private static final int MAX_BACKFILL_LIMIT = 2000;

    /** 资金信息 */
    private final FundAccountService fundAccountService;
    /** FundAccountRepository组件 */
    private final FundAccountRepository fundAccountRepository;
    /** FundTradeRepository组件 */
    private final FundTradeRepository fundTradeRepository;
    /** 统一交易仓储 */
    private final TradeRepository tradeRepository;
    /** 风控策略信息 */
    private final RiskPolicyDomainService riskPolicyDomainService;

    public FundAccountFacadeImpl(FundAccountService fundAccountService,
                                 FundAccountRepository fundAccountRepository,
                                 FundTradeRepository fundTradeRepository,
                                 TradeRepository tradeRepository,
                                 RiskPolicyDomainService riskPolicyDomainService) {
        this.fundAccountService = fundAccountService;
        this.fundAccountRepository = fundAccountRepository;
        this.fundTradeRepository = fundTradeRepository;
        this.tradeRepository = tradeRepository;
        this.riskPolicyDomainService = riskPolicyDomainService;
    }

    /**
     * 创建基金账户信息。
     */
    @Override
    public Long createFundAccount(CreateFundAccountCommand command) {
        return fundAccountService.createFundAccount(command);
    }

    /**
     * 获取基金账户信息。
     */
    @Override
    public FundAccountDTO getFundAccount(Long userId) {
        return fundAccountService.getFundAccount(userId);
    }

    /**
     * 获取基金账户信息。
     */
    @Override
    public FundAccountDTO getFundAccount(Long userId, String fundCode) {
        return fundAccountService.getFundAccount(userId, fundCode);
    }

    /**
     * 保存或更新基金信息。
     */
    @Override
    public FundProductDTO upsertFundProduct(UpsertFundProductCommand command) {
        return fundAccountService.upsertFundProduct(command);
    }

    /**
     * 获取基金信息。
     */
    @Override
    public FundProductDTO getFundProduct(String fundCode) {
        return fundAccountService.getFundProduct(fundCode);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public FundTransactionDTO subscribe(FundSubscribeCommand command) {
        return fundAccountService.subscribe(command);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public FundTransactionDTO confirmSubscribe(FundSubscribeConfirmCommand command) {
        return fundAccountService.confirmSubscribe(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public FundTransactionDTO cancelSubscribe(FundSubscribeCancelCommand command) {
        return fundAccountService.cancelSubscribe(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public FundTransactionDTO redeem(FundRedeemCommand command) {
        return fundAccountService.redeem(command);
    }

    /**
     * 确认赎回信息。
     */
    @Override
    public FundTransactionDTO confirmRedeem(FundRedeemConfirmCommand command) {
        return fundAccountService.confirmRedeem(command);
    }

    /**
     * 取消赎回信息。
     */
    @Override
    public FundTransactionDTO cancelRedeem(FundRedeemCancelCommand command) {
        return fundAccountService.cancelRedeem(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public FundTransactionDTO fastRedeem(FundFastRedeemCommand command) {
        return fundAccountService.fastRedeem(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public FundTransactionDTO switchProduct(FundSwitchCommand command) {
        return fundAccountService.switchProduct(command);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public FundTransactionDTO confirmSwitch(FundSwitchConfirmCommand command) {
        return fundAccountService.confirmSwitch(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public FundTransactionDTO cancelSwitch(FundSwitchCancelCommand command) {
        return fundAccountService.cancelSwitch(command);
    }

    /**
     * 处理结算收益信息。
     */
    @Override
    public FundTransactionDTO settleIncome(FundIncomeSettleCommand command) {
        return fundAccountService.settleIncome(command);
    }

    /**
     * 发布收益日历信息。
     */
    @Override
    public FundIncomeCalendarDTO publishIncomeCalendar(PublishFundIncomeCalendarCommand command) {
        return fundAccountService.publishIncomeCalendar(command);
    }

    /**
     * 处理结算收益日历信息。
     */
    @Override
    public FundIncomeCalendarDTO settleIncomeCalendar(SettleFundIncomeCalendarCommand command) {
        return fundAccountService.settleIncomeCalendar(command);
    }

    /**
     * 解析基金编码。
     */
    @Override
    @Transactional(readOnly = true)
    public String resolveFundCode(Long userId, String preferredFundCode) {
        validateUserId(userId);
        String normalizedFundCode = normalizeFundCode(preferredFundCode);
        if (normalizedFundCode != null) {
            fundAccountRepository.findByUserIdAndFundCode(userId, normalizedFundCode)
                    .orElseThrow(() -> new NoSuchElementException(
                            "fund account not found for userId=" + userId + ", fundCode=" + normalizedFundCode
                    ));
            return normalizedFundCode;
        }

        List<FundAccount> fundAccounts = fundAccountRepository.findAllByUserId(userId);
        if (fundAccounts.isEmpty()) {
            throw new NoSuchElementException("fund account not found for userId=" + userId);
        }
        return fundAccounts.getFirst().getFundCode();
    }

    /**
     * 处理份额用于支付信息。
     */
    @Override
    @Transactional
    public FundFreezeResult freezeShareForPay(String fundTradeOrderNo,
                                              Long userId,
                                              String preferredFundCode,
                                              Money amount,
                                              String businessNo) {
        String normalizedFundTradeOrderNo = validateFundTradeOrderNo(fundTradeOrderNo);
        validateUserId(userId);
        Money normalizedAmount = normalizePositive(amount, "amount");
        ensureRiskAllowed(
                RiskSceneCode.FUND_PAY_FREEZE,
                userId,
                normalizeOptionalText(preferredFundCode),
                normalizedAmount.getAmount(),
                Map.of("scene", PAY_FREEZE_SCENE_CODE)
        );
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existingTransaction = fundTradeRepository.findTransactionForUpdate(normalizedFundTradeOrderNo).orElse(null);
        if (existingTransaction != null) {
            if (existingTransaction.getTransactionType() != FundTransactionType.FREEZE) {
                throw new IllegalArgumentException("fundTradeOrderNo already used by another transaction type");
            }
            if (existingTransaction.getTransactionStatus() == FundTransactionStatus.COMPENSATED
                    || existingTransaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
                throw new IllegalStateException("fund trade has already been compensated, freeze is not allowed");
            }
            if (!userId.equals(existingTransaction.getUserId())) {
                throw new IllegalArgumentException("userId does not match existing fund trade");
            }
            String existingFundCode = normalizeRequiredFundCode(existingTransaction.getFundCode());
            FundAmount existingShare = normalizePositive(existingTransaction.getRequestShare(), "share");
            FundAmount existingNav = parsePayFreezeNav(existingTransaction.getExtInfo());
            if (existingNav == null) {
                FundAccount existingFundAccount = fundAccountRepository.findByUserIdAndFundCode(userId, existingFundCode)
                        .orElseThrow(() -> new NoSuchElementException(
                                "fund account not found for userId=" + userId + ", fundCode=" + existingFundCode
                        ));
                existingNav = resolveEffectiveNav(existingFundAccount.getLatestNav());
            }
            return new FundFreezeResult(existingFundCode, existingShare, existingNav);
        }

        String fundCode = resolveFundCode(userId, preferredFundCode);
        FundAccount fundAccount = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "fund account not found for userId=" + userId + ", fundCode=" + fundCode
                ));

        FundAmount nav = resolveEffectiveNav(fundAccount.getLatestNav());
        FundAmount share = FundAmount.of(
                normalizedAmount.getAmount().divide(nav.toBigDecimal(), FundAmount.SCALE, RoundingMode.HALF_UP)
        );
        FundAmount requestAmount = FundAmount.of(normalizedAmount.getAmount());
        String normalizedBusinessNo = normalizeOptionalText(businessNo);

        fundAccount.freezeShare(share, now);
        FundTransaction transaction = FundTransaction.pendingFreeze(
                normalizedFundTradeOrderNo,
                userId,
                fundCode,
                requestAmount,
                share,
                normalizedBusinessNo,
                buildPayFreezeExtInfo(nav, normalizedBusinessNo),
                now
        );
        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncAcceptedPayFreezeTrade(transaction, toMoney(requestAmount, fundAccount.getCurrencyCode()), now);
        return new FundFreezeResult(fundCode, share, nav);
    }

    /**
     * 确认份额用于支付信息。
     */
    @Override
    @Transactional
    public void confirmFrozenShareForPay(Long userId, String fundTradeOrderNo) {
        String normalizedFundTradeOrderNo = validateFundTradeOrderNo(fundTradeOrderNo);
        validateUserId(userId);
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(normalizedFundTradeOrderNo).orElse(null);
        if (transaction == null) {
            return;
        }
        if (transaction.getTransactionType() != FundTransactionType.FREEZE) {
            throw new IllegalArgumentException("fundTradeOrderNo is not a freeze transaction");
        }
        if (!userId.equals(transaction.getUserId())) {
            throw new IllegalArgumentException("userId does not match freeze transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return;
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.COMPENSATED
                || transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return;
        }

        FundAmount share = normalizePositive(transaction.getRequestShare(), "share");
        String fundCode = normalizeRequiredFundCode(transaction.getFundCode());
        FundAmount confirmedAmount = firstPositiveFundAmount(transaction.getRequestAmount(), transaction.getConfirmedAmount());
        LocalDateTime now = LocalDateTime.now();

        FundAccount fundAccount = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "fund account not found for userId=" + userId + ", fundCode=" + fundCode
                ));
        fundAccount.settleFrozenShare(share, now);
        transaction.markConfirmed(confirmedAmount, share, now);

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncSucceededPayFreezeTrade(transaction, toMoney(confirmedAmount, fundAccount.getCurrencyCode()), now);
    }

    /**
     * 处理份额用于支付信息。
     */
    @Override
    @Transactional
    public void compensateFrozenShareForPay(Long userId,
                                            String fundTradeOrderNo,
                                            String preferredFundCode,
                                            String businessNo) {
        String normalizedFundTradeOrderNo = validateFundTradeOrderNo(fundTradeOrderNo);
        validateUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        String normalizedBusinessNo = normalizeOptionalText(businessNo);

        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(normalizedFundTradeOrderNo).orElse(null);
        if (transaction == null) {
            String fenceFundCode = normalizeFundCode(preferredFundCode);
            if (fenceFundCode == null) {
                fenceFundCode = DEFAULT_FUND_CODE;
            }
            FundTransaction compensateFence = new FundTransaction(
                    normalizedFundTradeOrderNo,
                    userId,
                    fenceFundCode,
                    FundTransactionType.FREEZE,
                    FundTransactionStatus.COMPENSATED,
                    FundAmount.ZERO,
                    FundAmount.ZERO,
                    FundAmount.ZERO,
                    FundAmount.ZERO,
                    normalizedBusinessNo,
                    buildPayCompensateFenceExtInfo(normalizedBusinessNo),
                    now,
                    now
            );
            fundTradeRepository.saveTransaction(compensateFence);
            return;
        }
        if (transaction.getTransactionType() != FundTransactionType.FREEZE) {
            throw new IllegalArgumentException("fundTradeOrderNo is not a freeze transaction");
        }
        if (!userId.equals(transaction.getUserId())) {
            throw new IllegalArgumentException("userId does not match freeze transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.COMPENSATED
                || transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return;
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return;
        }

        FundAmount share = normalizePositive(transaction.getRequestShare(), "share");
        String fundCode = normalizeRequiredFundCode(transaction.getFundCode());
        FundAccount fundAccount = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "fund account not found for userId=" + userId + ", fundCode=" + fundCode
                ));
        fundAccount.unfreezeShare(share, now);
        transaction.markCompensated(now);

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncCompensatedPayFreezeTrade(
                transaction,
                toMoney(firstPositiveFundAmount(transaction.getRequestAmount(), transaction.getConfirmedAmount()), fundAccount.getCurrencyCode()),
                now
        );
    }

    /**
     * 获取基金明细信息。
     */
    @Override
    @Transactional(readOnly = true)
    public FundTransactionDetailDTO getFundTransactionDetail(String fundTradeOrderNo) {
        String normalizedFundTradeOrderNo = validateFundTradeOrderNo(fundTradeOrderNo);
        FundTransaction transaction = fundTradeRepository.findTransaction(normalizedFundTradeOrderNo)
                .orElseThrow(() -> new NoSuchElementException("fund transaction not found: " + normalizedFundTradeOrderNo));
        return toFundTransactionDetailDTO(transaction);
    }

    /**
     * 处理交易基金订单信息。
     */
    @Override
    @Transactional
    public FundTradeBackfillResultDTO backfillTradeFundOrders(String fundCode,
                                                              List<String> transactionTypes,
                                                              Integer limit) {
        String normalizedFundCode = normalizeFundCode(fundCode);
        int normalizedLimit = normalizeBackfillLimit(limit);
        List<FundTransactionType> normalizedTypes = resolveBackfillTransactionTypes(transactionTypes);
        List<FundTransaction> transactions = fundTradeRepository.findRecentTransactionsByTypes(
                normalizedFundCode,
                normalizedTypes,
                normalizedLimit
        );

        int scannedCount = 0;
        int repairedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        List<String> failedOrderNos = new ArrayList<>();
        for (FundTransaction transaction : transactions) {
            scannedCount++;
            if (tradeRepository.findTradeFundOrderByTradeOrderNo(transaction.getOrderNo()).isPresent()) {
                skippedCount++;
                continue;
            }
            TradeOrder tradeOrder = findTradeOrderSafely(transaction.getOrderNo());
            if (tradeOrder == null) {
                failedCount++;
                collectFailedOrderNo(failedOrderNos, transaction.getOrderNo());
                continue;
            }
            LocalDateTime occurredAt = resolveBackfillOccurredAt(transaction, tradeOrder);
            try {
                tradeRepository.saveTradeFundOrder(buildBackfillTradeFundOrder(transaction, tradeOrder, occurredAt));
                repairedCount++;
            } catch (RuntimeException ex) {
                failedCount++;
                collectFailedOrderNo(failedOrderNos, transaction.getOrderNo());
            }
        }

        return new FundTradeBackfillResultDTO(
                normalizedFundCode,
                normalizedTypes.stream().map(Enum::name).toList(),
                normalizedLimit,
                scannedCount,
                repairedCount,
                skippedCount,
                failedCount,
                List.copyOf(failedOrderNos),
                LocalDateTime.now()
        );
    }

    private void syncAcceptedPayFreezeTrade(FundTransaction transaction, Money amount, LocalDateTime occurredAt) {
        syncPayFreezeTrade(transaction, amount, TradeStatus.CREATED, null, occurredAt);
    }

    private void syncSucceededPayFreezeTrade(FundTransaction transaction, Money amount, LocalDateTime occurredAt) {
        syncPayFreezeTrade(transaction, amount, TradeStatus.SUCCEEDED, null, occurredAt);
    }

    private void syncCompensatedPayFreezeTrade(FundTransaction transaction, Money amount, LocalDateTime occurredAt) {
        syncPayFreezeTrade(transaction, amount, TradeStatus.FAILED, "pay freeze compensated", occurredAt);
    }

    private void syncPayFreezeTrade(FundTransaction transaction,
                                    Money amount,
                                    TradeStatus targetStatus,
                                    String failureReason,
                                    LocalDateTime occurredAt) {
        tradeRepository.saveTradeOrder(buildPayFreezeTradeOrderSnapshot(
                transaction,
                amount,
                targetStatus,
                failureReason,
                occurredAt
        ));
        tradeRepository.saveTradeFundOrder(buildPayFreezeTradeOrderExtension(transaction, amount, targetStatus, occurredAt));
    }

    private TradeOrder buildPayFreezeTradeOrderSnapshot(FundTransaction transaction,
                                                        Money amount,
                                                        TradeStatus targetStatus,
                                                        String failureReason,
                                                        LocalDateTime occurredAt) {
        Money normalizedAmount = normalizeTradeMoney(amount);
        TradeOrder existingTradeOrder = findTradeOrderSafely(transaction.getOrderNo());
        String bizOrderNo = resolvePayFreezeBizOrderNo(transaction, existingTradeOrder);
        Money zero = Money.zero(normalizedAmount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
        Money originalAmount = existingTradeOrder != null && existingTradeOrder.getOriginalAmount().isPositive()
                ? existingTradeOrder.getOriginalAmount().rounded(2, RoundingMode.HALF_UP)
                : normalizedAmount;
        Money payableAmount = existingTradeOrder != null && existingTradeOrder.getPayableAmount().isPositive()
                ? existingTradeOrder.getPayableAmount().rounded(2, RoundingMode.HALF_UP)
                : normalizedAmount;
        Money settleAmount = targetStatus == TradeStatus.SUCCEEDED
                ? normalizedAmount
                : existingTradeOrder == null
                ? zero
                : existingTradeOrder.getSettleAmount().rounded(2, RoundingMode.HALF_UP);
        Money fundDebitAmount = targetStatus == TradeStatus.SUCCEEDED
                ? normalizedAmount
                : payableAmount;
        TradeSplitPlan splitPlan = existingTradeOrder == null
                ? TradeSplitPlan.of(normalizedAmount.getCurrencyUnit(), zero, fundDebitAmount, zero, zero)
                : existingTradeOrder.getSplitPlan();
        return new TradeOrder(
                existingTradeOrder == null ? null : existingTradeOrder.getId(),
                transaction.getOrderNo(),
                existingTradeOrder == null ? buildFundRequestNo(transaction.getOrderNo()) : existingTradeOrder.getRequestNo(),
                existingTradeOrder == null ? TradeType.PAY : existingTradeOrder.getTradeType(),
                existingTradeOrder == null ? PAY_FREEZE_SCENE_CODE : existingTradeOrder.getBusinessSceneCode(),
                TradeBusinessDomainCode.AICASH.name(),
                bizOrderNo,
                existingTradeOrder == null ? null : existingTradeOrder.getOriginalTradeOrderNo(),
                transaction.getUserId(),
                transaction.getUserId(),
                existingTradeOrder == null ? "FUND_ACCOUNT" : existingTradeOrder.getPaymentMethod(),
                originalAmount,
                zero,
                payableAmount,
                settleAmount,
                splitPlan,
                existingTradeOrder == null ? null : existingTradeOrder.getPricingQuoteNo(),
                existingTradeOrder == null ? null : existingTradeOrder.getPayOrderNo(),
                existingTradeOrder == null ? 0 : existingTradeOrder.getLastPayStatusVersion(),
                existingTradeOrder == null ? null : existingTradeOrder.getPayResultCode(),
                existingTradeOrder == null ? null : existingTradeOrder.getPayResultMessage(),
                targetStatus,
                normalizeOptionalText(failureReason),
                buildPayFreezeTradeMetadata(transaction),
                existingTradeOrder == null ? occurredAt : existingTradeOrder.getCreatedAt(),
                occurredAt
        );
    }

    private TradeFundOrder buildPayFreezeTradeOrderExtension(FundTransaction transaction,
                                                             Money amount,
                                                             TradeStatus targetStatus,
                                                             LocalDateTime occurredAt) {
        String bizOrderNo = resolvePayFreezeBizOrderNo(transaction, null);
        CurrencyUnit currencyUnit = amount == null ? CurrencyUnit.of(DEFAULT_CURRENCY_CODE) : amount.getCurrencyUnit();
        String fundAccountNo = buildFundAccountNo(transaction.getUserId(), transaction.getFundCode());
        String billMonth = resolveFundBillMonth(occurredAt);
        return new TradeFundOrder(
                null,
                bizOrderNo,
                transaction.getOrderNo(),
                TradeFundProductType.AICASH,
                fundAccountNo,
                buildFundBillNo(fundAccountNo, billMonth),
                billMonth,
                TradeFundTradeType.PAY_FREEZE,
                firstPositiveFundAmount(transaction.getConfirmedShare(), transaction.getRequestShare()).toBigDecimal(),
                targetStatus == TradeStatus.SUCCEEDED
                        ? toMoney(firstPositiveFundAmount(transaction.getConfirmedAmount(), transaction.getRequestAmount()), currencyUnit.getCode())
                        : Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP),
                occurredAt == null ? null : occurredAt.toLocalDate(),
                occurredAt,
                null,
                occurredAt
        );
    }

    private TradeOrder findTradeOrderSafely(String tradeOrderNo) {
        Optional<TradeOrder> optional = tradeRepository.findTradeOrderByTradeOrderNo(tradeOrderNo);
        if (optional == null) {
            return null;
        }
        return optional.orElse(null);
    }

    private String resolvePayFreezeBizOrderNo(FundTransaction transaction, TradeOrder existingTradeOrder) {
        String businessNo = normalizeOptionalText(transaction.getBusinessNo());
        if (businessNo != null) {
            return businessNo;
        }
        if (existingTradeOrder != null) {
            String existingBizOrderNo = normalizeOptionalText(existingTradeOrder.getBizOrderNo());
            if (existingBizOrderNo != null) {
                return existingBizOrderNo;
            }
        }
        return transaction.getOrderNo();
    }

    private String buildPayFreezeTradeMetadata(FundTransaction transaction) {
        StringBuilder builder = new StringBuilder();
        appendMetadataSegment(builder, "fundCode", transaction.getFundCode());
        appendMetadataSegment(builder, "fundTransactionType", transaction.getTransactionType().name());
        appendMetadataSegment(builder, "fundTransactionStatus", transaction.getTransactionStatus().name());
        appendMetadataSegment(builder, "fundBusinessNo", transaction.getBusinessNo());
        appendMetadataSegment(builder, "fundExtInfo", transaction.getExtInfo());
        return builder.length() == 0 ? null : builder.toString();
    }

    private FundTransactionDetailDTO toFundTransactionDetailDTO(FundTransaction transaction) {
        return new FundTransactionDetailDTO(
                transaction.getOrderNo(),
                transaction.getUserId(),
                transaction.getFundCode(),
                transaction.getTransactionType().name(),
                transaction.getTransactionStatus().name(),
                formatFundAmount(transaction.getRequestAmount()),
                formatFundAmount(transaction.getRequestShare()),
                formatFundAmount(transaction.getConfirmedAmount()),
                formatFundAmount(transaction.getConfirmedShare()),
                transaction.getBusinessNo(),
                transaction.getExtInfo(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    private TradeFundOrder buildBackfillTradeFundOrder(FundTransaction transaction,
                                                       TradeOrder tradeOrder,
                                                       LocalDateTime occurredAt) {
        String bizOrderNo = resolveBackfillBizOrderNo(transaction, tradeOrder);
        String fundAccountNo = buildFundAccountNo(transaction.getUserId(), transaction.getFundCode());
        String billMonth = resolveFundBillMonth(occurredAt);
        return new TradeFundOrder(
                null,
                bizOrderNo,
                transaction.getOrderNo(),
                TradeFundProductType.AICASH,
                fundAccountNo,
                buildFundBillNo(fundAccountNo, billMonth),
                billMonth,
                resolveBackfillFundTradeType(transaction),
                firstPositiveFundAmount(transaction.getConfirmedShare(), transaction.getRequestShare()).toBigDecimal(),
                resolveBackfillConfirmAmount(transaction, tradeOrder),
                occurredAt.toLocalDate(),
                occurredAt,
                null,
                occurredAt
        );
    }

    private TradeFundTradeType resolveBackfillFundTradeType(FundTransaction transaction) {
        return switch (transaction.getTransactionType()) {
            case SUBSCRIBE -> TradeFundTradeType.PURCHASE;
            case REDEEM -> TradeFundTradeType.REDEEM;
            case FAST_REDEEM -> TradeFundTradeType.FAST_REDEEM;
            case PRODUCT_SWITCH -> TradeFundTradeType.TRANSFER_OUT;
            case INCOME_SETTLE -> TradeFundTradeType.YIELD_SETTLE;
            case FREEZE -> TradeFundTradeType.PAY_FREEZE;
            case UNFREEZE -> TradeFundTradeType.PAY_FREEZE;
        };
    }

    private Money resolveBackfillConfirmAmount(FundTransaction transaction, TradeOrder tradeOrder) {
        CurrencyUnit currencyUnit = resolveBackfillCurrencyUnit(tradeOrder);
        if (transaction.getTransactionStatus() != FundTransactionStatus.CONFIRMED) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        FundAmount confirmedAmount = firstPositiveFundAmount(transaction.getConfirmedAmount(), transaction.getRequestAmount());
        if (confirmedAmount.compareTo(FundAmount.ZERO) > 0) {
            return toMoney(confirmedAmount, currencyUnit.getCode());
        }
        Money existingSettleAmount = tradeOrder.getSettleAmount();
        if (existingSettleAmount != null && existingSettleAmount.isPositive()) {
            return existingSettleAmount.rounded(2, RoundingMode.HALF_UP);
        }
        Money existingPayableAmount = tradeOrder.getPayableAmount();
        if (existingPayableAmount != null && existingPayableAmount.isPositive()) {
            return existingPayableAmount.rounded(2, RoundingMode.HALF_UP);
        }
        return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
    }

    private String resolveBackfillBizOrderNo(FundTransaction transaction, TradeOrder tradeOrder) {
        String businessNo = normalizeOptionalText(transaction.getBusinessNo());
        if (businessNo != null) {
            return businessNo;
        }
        String existingBizOrderNo = normalizeOptionalText(tradeOrder.getBizOrderNo());
        if (existingBizOrderNo != null) {
            return existingBizOrderNo;
        }
        return transaction.getOrderNo();
    }

    private CurrencyUnit resolveBackfillCurrencyUnit(TradeOrder tradeOrder) {
        if (tradeOrder.getOriginalAmount() != null) {
            return tradeOrder.getOriginalAmount().getCurrencyUnit();
        }
        if (tradeOrder.getPayableAmount() != null) {
            return tradeOrder.getPayableAmount().getCurrencyUnit();
        }
        if (tradeOrder.getSettleAmount() != null) {
            return tradeOrder.getSettleAmount().getCurrencyUnit();
        }
        return CurrencyUnit.of(DEFAULT_CURRENCY_CODE);
    }

    private LocalDateTime resolveBackfillOccurredAt(FundTransaction transaction, TradeOrder tradeOrder) {
        if (transaction.getUpdatedAt() != null) {
            return transaction.getUpdatedAt();
        }
        if (transaction.getCreatedAt() != null) {
            return transaction.getCreatedAt();
        }
        if (tradeOrder.getUpdatedAt() != null) {
            return tradeOrder.getUpdatedAt();
        }
        if (tradeOrder.getCreatedAt() != null) {
            return tradeOrder.getCreatedAt();
        }
        return LocalDateTime.now();
    }

    private List<FundTransactionType> resolveBackfillTransactionTypes(List<String> transactionTypes) {
        if (transactionTypes == null || transactionTypes.isEmpty()) {
            return List.of(FundTransactionType.PRODUCT_SWITCH, FundTransactionType.FREEZE);
        }
        List<FundTransactionType> resolvedTypes = new ArrayList<>();
        for (String rawType : transactionTypes) {
            String normalizedType = normalizeOptionalText(rawType);
            if (normalizedType == null) {
                continue;
            }
            resolvedTypes.add(FundTransactionType.valueOf(normalizedType.toUpperCase(Locale.ROOT)));
        }
        if (resolvedTypes.isEmpty()) {
            return List.of(FundTransactionType.PRODUCT_SWITCH, FundTransactionType.FREEZE);
        }
        return resolvedTypes.stream().distinct().toList();
    }

    private int normalizeBackfillLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_BACKFILL_LIMIT;
        }
        return Math.min(limit, MAX_BACKFILL_LIMIT);
    }

    private void collectFailedOrderNo(List<String> failedOrderNos, String orderNo) {
        if (failedOrderNos.size() >= 20) {
            return;
        }
        String normalizedOrderNo = normalizeOptionalText(orderNo);
        if (normalizedOrderNo == null) {
            return;
        }
        failedOrderNos.add(normalizedOrderNo);
    }

    private String formatFundAmount(FundAmount amount) {
        FundAmount normalized = amount == null ? FundAmount.ZERO : amount;
        return normalized.setScale(FundAmount.SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private void appendMetadataSegment(StringBuilder builder, String key, String value) {
        String normalizedValue = normalizeOptionalText(value);
        if (normalizedValue == null) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(';');
        }
        builder.append(key).append('=').append(normalizedValue);
    }

    private Money normalizeTradeMoney(Money amount) {
        if (amount == null || amount.isNegative() || amount.isZero()) {
            throw new IllegalArgumentException("fund trade amount must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money toMoney(FundAmount amount, String currencyCode) {
        FundAmount normalized = amount == null ? FundAmount.ZERO.setScale(FundAmount.SCALE, RoundingMode.HALF_UP)
                : amount.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
        String resolvedCurrencyCode = normalizeCurrency(currencyCode);
        return Money.of(
                CurrencyUnit.of(resolvedCurrencyCode),
                normalized.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        );
    }

    private String normalizeCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY_CODE;
        }
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveTradeCurrencyCode(Money amount) {
        if (amount == null) {
            return DEFAULT_CURRENCY_CODE;
        }
        return amount.getCurrencyUnit().getCode();
    }

    private String buildFundAccountNo(Long userId, String fundCode) {
        return "FUNDACC-" + userId + "-" + normalizeRequiredFundCode(fundCode);
    }

    private String resolveFundBillMonth(LocalDateTime occurredAt) {
        LocalDateTime timestamp = occurredAt == null ? LocalDateTime.now() : occurredAt;
        return String.format(Locale.ROOT, "%04d-%02d", timestamp.getYear(), timestamp.getMonthValue());
    }

    private String buildFundBillNo(String fundAccountNo, String billMonth) {
        return "AICASHBILL-" + fundAccountNo + "-" + billMonth.replace("-", "");
    }

    private String buildFundRequestNo(String orderNo) {
        return "FUND:" + orderNo;
    }

    private void ensureRiskAllowed(RiskSceneCode sceneCode,
                                   Long userId,
                                   String accountNo,
                                   BigDecimal amount,
                                   Map<String, String> metadata) {
        RiskDecision decision = riskPolicyDomainService.evaluate(new RiskCheckContext(
                sceneCode,
                userId,
                normalizeOptionalText(accountNo),
                amount,
                metadata == null ? Map.of() : metadata
        ));
        if (!decision.passed()) {
            throw new IllegalArgumentException(decision.message());
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
    }

    private String validateFundTradeOrderNo(String fundTradeOrderNo) {
        String normalized = normalizeOptionalText(fundTradeOrderNo);
        if (normalized == null) {
            throw new IllegalArgumentException("fundTradeOrderNo must not be blank");
        }
        return normalized;
    }

    private String normalizeRequiredFundCode(String fundCode) {
        String normalized = normalizeFundCode(fundCode);
        if (normalized == null) {
            throw new IllegalArgumentException("fundCode must not be blank");
        }
        return normalized;
    }

    private String normalizeFundCode(String fundCode) {
        if (fundCode == null) {
            return null;
        }
        return FundProductCodes.normalizeNullable(fundCode);
    }

    private Money normalizePositive(Money value, String fieldName) {
        if (value == null || value.compareTo(Money.zero(value.getCurrencyUnit())) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private FundAmount normalizePositive(FundAmount value, String fieldName) {
        if (value == null || value.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private FundAmount resolveEffectiveNav(FundAmount latestNav) {
        if (latestNav == null || latestNav.compareTo(FundAmount.ZERO) <= 0) {
            return FundAmount.ONE;
        }
        return latestNav;
    }

    private FundAmount firstPositiveFundAmount(FundAmount preferred, FundAmount fallback) {
        if (preferred != null && preferred.compareTo(FundAmount.ZERO) > 0) {
            return preferred.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
        }
        if (fallback != null && fallback.compareTo(FundAmount.ZERO) > 0) {
            return fallback.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
        }
        return FundAmount.ZERO.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
    }

    private String buildPayFreezeExtInfo(FundAmount nav, String businessNo) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("flow", "PAY_FUND_FREEZE");
        payload.put("nav", nav.setScale(FundAmount.SCALE, RoundingMode.HALF_UP).toPlainString());
        if (businessNo != null && !businessNo.isBlank()) {
            payload.put("businessNo", businessNo);
        }
        return toExtInfo(payload);
    }

    private String buildPayCompensateFenceExtInfo(String businessNo) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("flow", "PAY_FUND_FREEZE_COMPENSATE_FENCE");
        if (businessNo != null && !businessNo.isBlank()) {
            payload.put("businessNo", businessNo);
        }
        return toExtInfo(payload);
    }

    private FundAmount parsePayFreezeNav(String extInfo) {
        String normalizedExtInfo = normalizeOptionalText(extInfo);
        if (normalizedExtInfo == null) {
            return null;
        }
        String[] segments = normalizedExtInfo.split(";");
        for (String segment : segments) {
            String[] kv = segment.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            if (!"nav".equalsIgnoreCase(kv[0].trim())) {
                continue;
            }
            try {
                return FundAmount.of(new BigDecimal(kv[1].trim()));
            } catch (RuntimeException ex) {
                return null;
            }
        }
        return null;
    }

    private String toExtInfo(Map<String, String> payload) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String value = normalizeOptionalText(entry.getValue());
            if (value == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(value);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String normalizeOptionalText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
