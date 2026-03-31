package cn.openaipay.application.fundaccount.service.impl;

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
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.dto.FundIncomeCalendarDTO;
import cn.openaipay.application.fundaccount.dto.FundProductDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundFastRedeemQuota;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendar;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.fundaccount.model.FundProduct;
import cn.openaipay.domain.fundaccount.model.FundProductStatus;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.model.FundUserFastRedeemQuota;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskCheckContext;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.model.RiskSceneCode;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.shared.number.FundAmount;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeFundOrder;
import cn.openaipay.domain.trade.model.TradeFundProductType;
import cn.openaipay.domain.trade.model.TradeFundTradeType;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.model.TradeType;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.domain.walletaccount.model.WalletAccount;
import cn.openaipay.domain.walletaccount.repository.WalletAccountRepository;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基金账户应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class FundAccountServiceImpl implements FundAccountService {

    /** 默认基金编码常量 */
    private static final String DEFAULT_FUND_CODE = FundProductCodes.DEFAULT_FUND_CODE;
    /** 默认币种常量 */
    private static final String DEFAULT_CURRENCY = "CNY";
    /** 内部持仓补齐业务单号前缀，允许跳过钱包余额校验。 */
    private static final String SYSTEM_SEED_BUSINESS_NO_PREFIX = "SYSTEM_SEED_";
    /** 默认赎回模式常量 */
    private static final String DEFAULT_REDEEM_MODE = "NORMAL";
    /** 赎回到余额标记。 */
    private static final String REDEEM_DESTINATION_BALANCE = "BALANCE";
    /** 赎回到银行卡标记。 */
    private static final String REDEEM_DESTINATION_BANK_CARD = "BANK_CARD";
    /** 银行名称提取正则。 */
    private static final Pattern BANK_NAME_PATTERN = Pattern.compile("([\\p{IsHan}A-Za-z0-9]{2,20}银行)");

    /** FundAccountRepository组件 */
    private final FundAccountRepository fundAccountRepository;
    /** FundTradeRepository组件 */
    private final FundTradeRepository fundTradeRepository;
    /** 统一交易仓储，用于把爱存真实交易补齐到 trade_order 与 trade_fund_order。 */
    private final TradeRepository tradeRepository;
    /** 钱包账户仓储，用于校验转入前的余额可用性。 */
    private final WalletAccountRepository walletAccountRepository;
    /** 风控策略信息。 */
    private final RiskPolicyDomainService riskPolicyDomainService;
    /** AiPayIdGenerator组件 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** 爱存七日年化收益配置，按比例值存储，例如 0.01045 表示 1.0450%。 */
    private final BigDecimal aicashAnnualizedYieldRate;

    public FundAccountServiceImpl(FundAccountRepository fundAccountRepository,
                                  FundTradeRepository fundTradeRepository,
                                  TradeRepository tradeRepository,
                                  WalletAccountRepository walletAccountRepository,
                                  RiskPolicyDomainService riskPolicyDomainService,
                                  AiPayIdGenerator aiPayIdGenerator,
                                  @Value("${aipay.fund.aicash.annualized-yield-rate:${aipay.fund.aicash.annualized-yield-rate:0.01045}}") BigDecimal aicashAnnualizedYieldRate) {
        this.fundAccountRepository = fundAccountRepository;
        this.fundTradeRepository = fundTradeRepository;
        this.tradeRepository = tradeRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.riskPolicyDomainService = riskPolicyDomainService;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aicashAnnualizedYieldRate = normalizeYieldRate(aicashAnnualizedYieldRate);
    }

    /**
     * 创建基金信息。
     */
    @Override
    @Transactional
    public Long createFundAccount(CreateFundAccountCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String fundCode = normalizeFundCode(command.fundCode());
        String currencyCode = normalizeCurrency(command.currencyCode());
        if (fundAccountRepository.findByUserIdAndFundCode(command.userId(), fundCode).isPresent()) {
            return command.userId();
        }
        LocalDateTime now = LocalDateTime.now();
        ensureProductExists(fundCode, currencyCode, now);
        FundAccount fundAccount = FundAccount.open(command.userId(), fundCode, currencyCode, now);
        fundAccountRepository.save(fundAccount);
        return command.userId();
    }

    /**
     * 获取基金信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public FundAccountDTO getFundAccount(Long userId) {
        return getFundAccount(userId, DEFAULT_FUND_CODE);
    }

    /**
     * 获取基金信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public FundAccountDTO getFundAccount(Long userId, String fundCode) {
        FundAccount fundAccount = fundAccountRepository.findByUserIdAndFundCode(userId, normalizeFundCode(fundCode))
                .orElseThrow(() -> new NoSuchElementException("fund account not found: " + userId + "/" + normalizeFundCode(fundCode)));
        return toFundAccountDTO(fundAccount);
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO subscribe(FundSubscribeCommand command) {
        validateOrderNo(command.orderNo());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String fundCode = normalizeFundCode(command.fundCode());
        FundAmount amount = normalizePositive(command.amount(), "amount");
        ensureRiskAllowed(
                RiskSceneCode.FUND_SUBSCRIBE,
                command.userId(),
                buildFundAccountNo(command.userId(), fundCode),
                amount.toBigDecimal(),
                Map.of("fundCode", fundCode)
        );
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existing = fundTradeRepository.findTransactionForUpdate(command.orderNo()).orElse(null);
        if (existing != null) {
            if (existing.getTransactionType() != FundTransactionType.SUBSCRIBE) {
                throw new IllegalArgumentException("orderNo already used by another transaction type");
            }
            return toTransactionDTO(existing, "subscribe duplicated, idempotent return");
        }

        FundProduct product = mustGetProduct(fundCode);
        product.validateSubscribe(amount);
        ensureWalletBalanceEnoughForSubscribe(command.userId(), product.getCurrencyCode(), amount, command.businessNo());
        FundAccount fundAccount = mustGetFundAccountForUpdate(command.userId(), fundCode);
        fundAccount.placeSubscribe(amount, now);

        FundTransaction transaction = FundTransaction.pendingSubscribe(
                command.orderNo(),
                command.userId(),
                fundCode,
                amount,
                command.businessNo(),
                now
        );

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncAcceptedFundTrade(transaction, toMoney(amount, fundAccount.getCurrencyCode()), "FUND_SUBSCRIBE", TradeType.PAY, now);
        return toTransactionDTO(transaction, "subscribe accepted");
    }

    /**
     * 确认业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO confirmSubscribe(FundSubscribeConfirmCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("subscribe transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.SUBSCRIBE) {
            throw new IllegalArgumentException("orderNo is not a subscribe transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "subscribe confirm duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "subscribe transaction already canceled, confirm ignored");
        }

        FundAccount fundAccount = mustGetFundAccountForUpdate(transaction.getUserId(), transaction.getFundCode());
        FundAmount confirmedShare = resolveSubscribeShare(transaction, command.confirmedShare(), command.nav(), fundAccount.getLatestNav());
        LocalDateTime now = LocalDateTime.now();
        fundAccount.confirmSubscribe(transaction.getRequestAmount(), confirmedShare, command.nav(), now);
        transaction.markConfirmed(transaction.getRequestAmount(), confirmedShare, now);

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncSucceededFundTrade(transaction, toMoney(transaction.getRequestAmount(), fundAccount.getCurrencyCode()), "FUND_SUBSCRIBE", TradeType.PAY, now);
        return toTransactionDTO(transaction, "subscribe confirmed");
    }

    /**
     * 取消业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO cancelSubscribe(FundSubscribeCancelCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("subscribe transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.SUBSCRIBE) {
            throw new IllegalArgumentException("orderNo is not a subscribe transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "subscribe cancel duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "subscribe transaction already confirmed, cancel ignored");
        }

        FundAccount fundAccount = mustGetFundAccountForUpdate(transaction.getUserId(), transaction.getFundCode());
        LocalDateTime now = LocalDateTime.now();
        fundAccount.cancelSubscribe(transaction.getRequestAmount(), now);
        transaction.markCanceled(now);

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncFailedFundTrade(transaction, toMoney(transaction.getRequestAmount(), fundAccount.getCurrencyCode()), "FUND_SUBSCRIBE", TradeType.PAY, "subscribe canceled", now);
        return toTransactionDTO(transaction, "subscribe canceled");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO redeem(FundRedeemCommand command) {
        String redeemMode = normalizeRedeemMode(command.redeemMode());
        if ("FAST".equals(redeemMode)) {
            return fastRedeem(new FundFastRedeemCommand(
                    command.orderNo(),
                    command.userId(),
                    command.fundCode(),
                    command.share(),
                    command.businessNo(),
                    command.redeemDestination(),
                    command.bankName()
            ));
        }

        validateOrderNo(command.orderNo());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String fundCode = normalizeFundCode(command.fundCode());
        FundAmount share = normalizePositive(command.share(), "share");
        ensureRiskAllowed(
                RiskSceneCode.FUND_REDEEM,
                command.userId(),
                buildFundAccountNo(command.userId(), fundCode),
                share.toBigDecimal(),
                Map.of("fundCode", fundCode, "redeemMode", redeemMode)
        );
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existing = fundTradeRepository.findTransactionForUpdate(command.orderNo()).orElse(null);
        if (existing != null) {
            if (existing.getTransactionType() != FundTransactionType.REDEEM) {
                throw new IllegalArgumentException("orderNo already used by another transaction type");
            }
            return toTransactionDTO(existing, "redeem duplicated, idempotent return");
        }

        FundProduct product = mustGetProduct(fundCode);
        product.validateRedeem(share);
        FundAccount fundAccount = mustGetFundAccountForUpdate(command.userId(), fundCode);
        fundAccount.placeRedeem(share, now);

        FundTransaction transaction = FundTransaction.pendingRedeem(
                command.orderNo(),
                command.userId(),
                fundCode,
                share,
                command.businessNo(),
                buildRedeemExtInfo(redeemMode, command.redeemDestination(), command.bankName()),
                now
        );

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncAcceptedFundTrade(
                transaction,
                toMoney(share.multiply(fundAccount.getLatestNav()).setScale(4, RoundingMode.HALF_UP), fundAccount.getCurrencyCode()),
                "FUND_REDEEM",
                TradeType.WITHDRAW,
                now
        );
        return toTransactionDTO(transaction, "redeem accepted");
    }

    /**
     * 确认业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO confirmRedeem(FundRedeemConfirmCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("redeem transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.REDEEM
                && transaction.getTransactionType() != FundTransactionType.FAST_REDEEM) {
            throw new IllegalArgumentException("orderNo is not a redeem transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "redeem confirm duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "redeem transaction already canceled, confirm ignored");
        }

        FundAccount fundAccount = mustGetFundAccountForUpdate(transaction.getUserId(), transaction.getFundCode());
        LocalDateTime now = LocalDateTime.now();
        fundAccount.confirmRedeem(transaction.getRequestShare(), now);
        FundAmount confirmedAmount = transaction.getRequestShare().multiply(fundAccount.getLatestNav());
        transaction.markConfirmed(confirmedAmount, transaction.getRequestShare(), now);

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncSucceededFundTrade(
                transaction,
                toMoney(confirmedAmount, fundAccount.getCurrencyCode()),
                transaction.getTransactionType() == FundTransactionType.FAST_REDEEM ? "FUND_FAST_REDEEM" : "FUND_REDEEM",
                TradeType.WITHDRAW,
                now
        );
        return toTransactionDTO(transaction, "redeem confirmed");
    }

    /**
     * 取消业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO cancelRedeem(FundRedeemCancelCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("redeem transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.REDEEM
                && transaction.getTransactionType() != FundTransactionType.FAST_REDEEM) {
            throw new IllegalArgumentException("orderNo is not a redeem transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "redeem cancel duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "redeem transaction already confirmed, cancel ignored");
        }

        FundAccount fundAccount = mustGetFundAccountForUpdate(transaction.getUserId(), transaction.getFundCode());
        LocalDateTime now = LocalDateTime.now();
        fundAccount.cancelRedeem(transaction.getRequestShare(), now);
        transaction.markCanceled(now);

        if (transaction.getTransactionType() == FundTransactionType.FAST_REDEEM) {
            releaseFastRedeemQuota(
                    transaction.getFundCode(),
                    transaction.getUserId(),
                    transaction.getRequestAmount(),
                    LocalDate.now(),
                    now
            );
        }

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncFailedFundTrade(
                transaction,
                toMoney(firstPositiveFundAmount(transaction.getConfirmedAmount(), transaction.getRequestAmount()), fundAccount.getCurrencyCode()),
                transaction.getTransactionType() == FundTransactionType.FAST_REDEEM ? "FUND_FAST_REDEEM" : "FUND_REDEEM",
                TradeType.WITHDRAW,
                "redeem canceled",
                now
        );
        return toTransactionDTO(transaction, "redeem canceled");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO fastRedeem(FundFastRedeemCommand command) {
        validateOrderNo(command.orderNo());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String fundCode = normalizeFundCode(command.fundCode());
        FundAmount share = normalizePositive(command.share(), "share");
        ensureRiskAllowed(
                RiskSceneCode.FUND_FAST_REDEEM,
                command.userId(),
                buildFundAccountNo(command.userId(), fundCode),
                share.toBigDecimal(),
                Map.of("fundCode", fundCode)
        );
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existing = fundTradeRepository.findTransactionForUpdate(command.orderNo()).orElse(null);
        if (existing != null) {
            if (existing.getTransactionType() != FundTransactionType.FAST_REDEEM) {
                throw new IllegalArgumentException("orderNo already used by another transaction type");
            }
            return toTransactionDTO(existing, "fast redeem duplicated, idempotent return");
        }

        FundProduct product = mustGetProduct(fundCode);
        product.validateRedeem(share);
        FundAccount fundAccount = mustGetFundAccountForUpdate(command.userId(), fundCode);
        FundAmount estimateAmount = share.multiply(fundAccount.getLatestNav()).setScale(4, RoundingMode.HALF_UP);
        occupyFastRedeemQuota(product, command.userId(), estimateAmount, LocalDate.now(), now);
        fundAccount.placeRedeem(share, now);

        FundTransaction transaction = new FundTransaction(
                command.orderNo(),
                command.userId(),
                fundCode,
                FundTransactionType.FAST_REDEEM,
                FundTransactionStatus.PENDING,
                estimateAmount,
                share,
                FundAmount.ZERO,
                FundAmount.ZERO,
                command.businessNo(),
                buildRedeemExtInfo("FAST", command.redeemDestination(), command.bankName()),
                now,
                now
        );

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncAcceptedFundTrade(transaction, toMoney(estimateAmount, fundAccount.getCurrencyCode()), "FUND_FAST_REDEEM", TradeType.WITHDRAW, now);
        return toTransactionDTO(transaction, "fast redeem accepted");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO switchProduct(FundSwitchCommand command) {
        validateOrderNo(command.orderNo());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String sourceFundCode = normalizeFundCode(command.sourceFundCode());
        String targetFundCode = normalizeFundCode(command.targetFundCode());
        if (sourceFundCode.equals(targetFundCode)) {
            throw new IllegalArgumentException("sourceFundCode and targetFundCode must be different");
        }
        FundAmount sourceShare = normalizePositive(command.sourceShare(), "sourceShare");
        ensureRiskAllowed(
                RiskSceneCode.FUND_SWITCH,
                command.userId(),
                buildFundAccountNo(command.userId(), sourceFundCode),
                sourceShare.toBigDecimal(),
                Map.of("sourceFundCode", sourceFundCode, "targetFundCode", targetFundCode)
        );
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existing = fundTradeRepository.findTransactionForUpdate(command.orderNo()).orElse(null);
        if (existing != null) {
            if (existing.getTransactionType() != FundTransactionType.PRODUCT_SWITCH) {
                throw new IllegalArgumentException("orderNo already used by another transaction type");
            }
            return toTransactionDTO(existing, "switch duplicated, idempotent return");
        }

        FundProduct sourceProduct = mustGetProduct(sourceFundCode);
        FundProduct targetProduct = mustGetProduct(targetFundCode);
        sourceProduct.ensureSwitchEnabled();
        targetProduct.ensureSwitchEnabled();
        sourceProduct.validateRedeem(sourceShare);

        FundAccount sourceAccount = mustGetFundAccountForUpdate(command.userId(), sourceFundCode);
        FundAccount targetAccount = findOrCreateFundAccountForUpdate(command.userId(), targetFundCode, targetProduct.getCurrencyCode(), now);
        sourceAccount.placeRedeem(sourceShare, now);

        FundTransaction transaction = FundTransaction.pendingProductSwitch(
                command.orderNo(),
                command.userId(),
                sourceFundCode,
                sourceShare,
                targetFundCode,
                command.businessNo(),
                now
        );

        fundAccountRepository.save(sourceAccount);
        fundAccountRepository.save(targetAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncAcceptedFundTrade(
                transaction,
                toMoney(sourceShare.multiply(sourceAccount.getLatestNav()).setScale(4, RoundingMode.HALF_UP), sourceAccount.getCurrencyCode()),
                "FUND_SWITCH",
                TradeType.WITHDRAW,
                now
        );
        return toTransactionDTO(transaction, "switch accepted");
    }

    /**
     * 确认业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO confirmSwitch(FundSwitchConfirmCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("switch transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.PRODUCT_SWITCH) {
            throw new IllegalArgumentException("orderNo is not a switch transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "switch confirm duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "switch transaction already canceled, confirm ignored");
        }

        String sourceFundCode = transaction.getFundCode();
        String targetFundCode = transaction.getExtInfo();
        LocalDateTime now = LocalDateTime.now();
        FundAccount sourceAccount = mustGetFundAccountForUpdate(transaction.getUserId(), sourceFundCode);
        FundProduct targetProduct = mustGetProduct(targetFundCode);
        FundAccount targetAccount = findOrCreateFundAccountForUpdate(
                transaction.getUserId(),
                targetFundCode,
                targetProduct.getCurrencyCode(),
                now
        );

        FundAmount sourceNav = pickPositive(command.sourceNav(), sourceAccount.getLatestNav(), "sourceNav");
        FundAmount targetNav = pickPositive(command.targetNav(), targetAccount.getLatestNav(), "targetNav");
        FundAmount sourceAmount = transaction.getRequestShare().multiply(sourceNav).setScale(4, RoundingMode.HALF_UP);
        FundAmount targetShare = sourceAmount.divide(targetNav, 4, RoundingMode.HALF_UP);

        sourceAccount.confirmRedeem(transaction.getRequestShare(), now);
        targetAccount.placeSubscribe(sourceAmount, now);
        targetAccount.confirmSubscribe(sourceAmount, targetShare, targetNav, now);
        transaction.markConfirmed(sourceAmount, targetShare, now);

        fundAccountRepository.save(sourceAccount);
        fundAccountRepository.save(targetAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncSucceededFundTrade(
                transaction,
                toMoney(sourceAmount, sourceAccount.getCurrencyCode()),
                "FUND_SWITCH",
                TradeType.WITHDRAW,
                now
        );
        return toTransactionDTO(transaction, "switch confirmed");
    }

    /**
     * 取消业务数据。
     */
    @Override
    @Transactional
    public FundTransactionDTO cancelSwitch(FundSwitchCancelCommand command) {
        validateOrderNo(command.orderNo());
        FundTransaction transaction = fundTradeRepository.findTransactionForUpdate(command.orderNo())
                .orElseThrow(() -> new NoSuchElementException("switch transaction not found: " + command.orderNo()));
        if (transaction.getTransactionType() != FundTransactionType.PRODUCT_SWITCH) {
            throw new IllegalArgumentException("orderNo is not a switch transaction");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CANCELED) {
            return toTransactionDTO(transaction, "switch cancel duplicated, idempotent return");
        }
        if (transaction.getTransactionStatus() == FundTransactionStatus.CONFIRMED) {
            return toTransactionDTO(transaction, "switch transaction already confirmed, cancel ignored");
        }

        FundAccount sourceAccount = mustGetFundAccountForUpdate(transaction.getUserId(), transaction.getFundCode());
        LocalDateTime now = LocalDateTime.now();
        sourceAccount.cancelRedeem(transaction.getRequestShare(), now);
        transaction.markCanceled(now);

        fundAccountRepository.save(sourceAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncFailedFundTrade(
                transaction,
                toMoney(transaction.getRequestShare().multiply(sourceAccount.getLatestNav()).setScale(4, RoundingMode.HALF_UP), sourceAccount.getCurrencyCode()),
                "FUND_SWITCH",
                TradeType.WITHDRAW,
                "switch canceled",
                now
        );
        return toTransactionDTO(transaction, "switch canceled");
    }

    /**
     * 处理结算收益信息。
     */
    @Override
    @Transactional
    public FundTransactionDTO settleIncome(FundIncomeSettleCommand command) {
        validateOrderNo(command.orderNo());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String fundCode = normalizeFundCode(command.fundCode());
        FundAmount incomeAmount = normalizePositive(command.incomeAmount(), "incomeAmount");
        LocalDateTime now = LocalDateTime.now();

        FundTransaction existing = fundTradeRepository.findTransactionForUpdate(command.orderNo()).orElse(null);
        if (existing != null) {
            if (existing.getTransactionType() != FundTransactionType.INCOME_SETTLE) {
                throw new IllegalArgumentException("orderNo already used by another transaction type");
            }
            return toTransactionDTO(existing, "income settle duplicated, idempotent return");
        }

        FundAccount fundAccount = mustGetFundAccountForUpdate(command.userId(), fundCode);
        FundAmount nav = command.nav();
        if ((nav == null || nav.compareTo(FundAmount.ZERO) <= 0)) {
            FundIncomeCalendar calendar = fundAccountRepository.findIncomeCalendar(fundCode, LocalDate.now()).orElse(null);
            if (calendar != null) {
                nav = calendar.getNav();
            }
        }
        fundAccount.settleIncome(incomeAmount, nav, now);

        FundTransaction transaction = FundTransaction.confirmedIncomeSettle(
                command.orderNo(),
                command.userId(),
                fundCode,
                incomeAmount,
                command.businessNo(),
                now
        );

        fundAccountRepository.save(fundAccount);
        fundTradeRepository.saveTransaction(transaction);
        syncSucceededFundTrade(transaction, toMoney(incomeAmount, fundAccount.getCurrencyCode()), "FUND_INCOME_SETTLE", TradeType.DEPOSIT, now);
        return toTransactionDTO(transaction, "income settled");
    }

    /**
     * 保存或更新基金信息。
     */
    @Override
    @Transactional
    public FundProductDTO upsertFundProduct(UpsertFundProductCommand command) {
        String fundCode = normalizeFundCode(command.fundCode());
        LocalDateTime now = LocalDateTime.now();
        FundProduct product = fundAccountRepository.findProductForUpdate(fundCode)
                .orElse(FundProduct.defaultOf(
                        fundCode,
                        normalizeCurrency(command.currencyCode()),
                        now
                ));
        FundProductStatus status = parseProductStatus(command.productStatus());
        product.updatePolicy(
                command.productName(),
                normalizeNullableCurrency(command.currencyCode()),
                status,
                command.singleSubscribeMinAmount(),
                command.singleSubscribeMaxAmount(),
                command.dailySubscribeMaxAmount(),
                command.singleRedeemMinShare(),
                command.singleRedeemMaxShare(),
                command.dailyRedeemMaxShare(),
                command.fastRedeemDailyQuota(),
                command.fastRedeemPerUserDailyQuota(),
                command.switchEnabled(),
                now
        );
        FundProduct saved = fundAccountRepository.saveProduct(product);
        return toFundProductDTO(saved);
    }

    /**
     * 获取基金信息。
     */
    @Override
    @Transactional(readOnly = true)
    public FundProductDTO getFundProduct(String fundCode) {
        FundProduct product = mustGetProduct(normalizeFundCode(fundCode));
        return toFundProductDTO(product);
    }

    /**
     * 发布收益日历信息。
     */
    @Override
    @Transactional
    public FundIncomeCalendarDTO publishIncomeCalendar(PublishFundIncomeCalendarCommand command) {
        String fundCode = normalizeFundCode(command.fundCode());
        LocalDate bizDate = parseBizDate(command.bizDate());
        FundAmount nav = normalizePositive(command.nav(), "nav");
        FundAmount incomePer10k = normalizeNonNegative(command.incomePer10k(), "incomePer10k");
        LocalDateTime now = LocalDateTime.now();

        FundIncomeCalendar calendar = fundAccountRepository.findIncomeCalendarForUpdate(fundCode, bizDate)
                .orElse(FundIncomeCalendar.planned(fundCode, bizDate, now));
        calendar.publish(nav, incomePer10k, now);
        FundIncomeCalendar saved = fundAccountRepository.saveIncomeCalendar(calendar);
        return toFundIncomeCalendarDTO(saved);
    }

    /**
     * 处理结算收益日历信息。
     */
    @Override
    @Transactional
    public FundIncomeCalendarDTO settleIncomeCalendar(SettleFundIncomeCalendarCommand command) {
        String fundCode = normalizeFundCode(command.fundCode());
        LocalDate bizDate = parseBizDate(command.bizDate());
        FundIncomeCalendar calendar = fundAccountRepository.findIncomeCalendarForUpdate(fundCode, bizDate)
                .orElseThrow(() -> new NoSuchElementException("income calendar not found: " + fundCode + "/" + bizDate));
        calendar.markSettled(LocalDateTime.now());
        FundIncomeCalendar saved = fundAccountRepository.saveIncomeCalendar(calendar);
        return toFundIncomeCalendarDTO(saved);
    }

    private FundAccountDTO toFundAccountDTO(FundAccount fundAccount) {
        return new FundAccountDTO(
                fundAccount.getUserId(),
                fundAccount.getFundCode(),
                fundAccount.getCurrencyCode(),
                fundAccount.getAvailableShare(),
                fundAccount.getFrozenShare(),
                fundAccount.getPendingSubscribeAmount(),
                fundAccount.getPendingRedeemShare(),
                fundAccount.getHoldingAmount(),
                fundAccount.getAccumulatedIncome(),
                fundAccount.getYesterdayIncome(),
                fundAccount.getLatestNav(),
                fundAccount.getAccountStatus().name(),
                resolveAnnualizedYieldRate(fundAccount.getFundCode()),
                resolveIncomePer10k(fundAccount.getFundCode())
        );
    }

    private BigDecimal resolveAnnualizedYieldRate(String fundCode) {
        if (DEFAULT_FUND_CODE.equalsIgnoreCase(normalizeFundCode(fundCode))) {
            return aicashAnnualizedYieldRate;
        }
        return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
    }

    private FundAmount resolveIncomePer10k(String fundCode) {
        BigDecimal annualizedYieldRate = resolveAnnualizedYieldRate(fundCode);
        if (annualizedYieldRate.signum() <= 0) {
            return FundAmount.ZERO;
        }
        BigDecimal incomePer10k = annualizedYieldRate
                .multiply(BigDecimal.valueOf(10_000))
                .divide(BigDecimal.valueOf(365), FundAmount.SCALE, RoundingMode.HALF_UP);
        return FundAmount.of(incomePer10k);
    }

    private BigDecimal normalizeYieldRate(BigDecimal source) {
        if (source == null || source.signum() < 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }
        return source.setScale(6, RoundingMode.HALF_UP);
    }

    private FundTransactionDTO toTransactionDTO(FundTransaction transaction, String message) {
        return new FundTransactionDTO(
                transaction.getOrderNo(),
                transaction.getTransactionType().name(),
                transaction.getTransactionStatus().name(),
                message
        );
    }

    private FundProductDTO toFundProductDTO(FundProduct product) {
        return new FundProductDTO(
                product.getFundCode(),
                product.getProductName(),
                product.getCurrencyCode(),
                product.getProductStatus().name(),
                product.getSingleSubscribeMinAmount(),
                product.getSingleSubscribeMaxAmount(),
                product.getDailySubscribeMaxAmount(),
                product.getSingleRedeemMinShare(),
                product.getSingleRedeemMaxShare(),
                product.getDailyRedeemMaxShare(),
                product.getFastRedeemDailyQuota(),
                product.getFastRedeemPerUserDailyQuota(),
                product.isSwitchEnabled()
        );
    }

    private FundIncomeCalendarDTO toFundIncomeCalendarDTO(FundIncomeCalendar calendar) {
        return new FundIncomeCalendarDTO(
                calendar.getFundCode(),
                calendar.getBizDate().toString(),
                calendar.getNav(),
                calendar.getIncomePer10k(),
                calendar.getCalendarStatus().name()
        );
    }

    private FundProduct mustGetProduct(String fundCode) {
        return fundAccountRepository.findProduct(fundCode)
                .orElseThrow(() -> new NoSuchElementException("fund product not found: " + fundCode));
    }

    private FundAccount mustGetFundAccountForUpdate(Long userId, String fundCode) {
        return fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)
                .orElseThrow(() -> new NoSuchElementException("fund account not found: " + userId + "/" + fundCode));
    }

    private FundAccount findOrCreateFundAccountForUpdate(Long userId, String fundCode, String currencyCode, LocalDateTime now) {
        FundAccount account = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode).orElse(null);
        if (account != null) {
            return account;
        }
        FundAccount created = FundAccount.open(userId, fundCode, normalizeCurrency(currencyCode), now);
        return fundAccountRepository.save(created);
    }

    private void ensureProductExists(String fundCode, String currencyCode, LocalDateTime now) {
        if (fundAccountRepository.findProduct(fundCode).isEmpty()) {
            fundAccountRepository.saveProduct(FundProduct.defaultOf(fundCode, currencyCode, now));
        }
    }

    private void occupyFastRedeemQuota(FundProduct product,
                                       Long userId,
                                       FundAmount amount,
                                       LocalDate quotaDate,
                                       LocalDateTime now) {
        FundFastRedeemQuota quota = fundAccountRepository.findFastRedeemQuotaForUpdate(product.getFundCode(), quotaDate)
                .orElse(FundFastRedeemQuota.init(product.getFundCode(), quotaDate, product.getFastRedeemDailyQuota(), now));
        quota.resetLimit(product.getFastRedeemDailyQuota(), now);
        quota.occupy(amount, now);

        FundUserFastRedeemQuota userQuota = fundAccountRepository.findUserFastRedeemQuotaForUpdate(product.getFundCode(), userId, quotaDate)
                .orElse(FundUserFastRedeemQuota.init(product.getFundCode(), userId, quotaDate, product.getFastRedeemPerUserDailyQuota(), now));
        userQuota.resetLimit(product.getFastRedeemPerUserDailyQuota(), now);
        userQuota.occupy(amount, now);

        fundAccountRepository.saveFastRedeemQuota(quota);
        fundAccountRepository.saveUserFastRedeemQuota(userQuota);
    }

    private void releaseFastRedeemQuota(String fundCode,
                                        Long userId,
                                        FundAmount amount,
                                        LocalDate quotaDate,
                                        LocalDateTime now) {
        if (amount == null || amount.compareTo(FundAmount.ZERO) <= 0) {
            return;
        }
        FundFastRedeemQuota quota = fundAccountRepository.findFastRedeemQuotaForUpdate(fundCode, quotaDate).orElse(null);
        FundUserFastRedeemQuota userQuota = fundAccountRepository.findUserFastRedeemQuotaForUpdate(fundCode, userId, quotaDate).orElse(null);
        if (quota != null) {
            quota.release(amount, now);
            fundAccountRepository.saveFastRedeemQuota(quota);
        }
        if (userQuota != null) {
            userQuota.release(amount, now);
            fundAccountRepository.saveUserFastRedeemQuota(userQuota);
        }
    }

    private void syncAcceptedFundTrade(FundTransaction transaction,
                                       Money amount,
                                       String businessSceneCode,
                                       TradeType tradeType,
                                       LocalDateTime occurredAt) {
        tradeRepository.saveTradeOrder(buildFundTradeOrderSnapshot(
                transaction,
                amount,
                businessSceneCode,
                tradeType,
                TradeStatus.CREATED,
                null,
                occurredAt
        ));
        tradeRepository.saveTradeFundOrder(buildFundTradeOrderExtension(transaction, amount, occurredAt));
    }

    private void syncSucceededFundTrade(FundTransaction transaction,
                                        Money amount,
                                        String businessSceneCode,
                                        TradeType tradeType,
                                        LocalDateTime occurredAt) {
        tradeRepository.saveTradeOrder(buildFundTradeOrderSnapshot(
                transaction,
                amount,
                businessSceneCode,
                tradeType,
                TradeStatus.SUCCEEDED,
                null,
                occurredAt
        ));
        tradeRepository.saveTradeFundOrder(buildFundTradeOrderExtension(transaction, amount, occurredAt));
    }

    private void syncFailedFundTrade(FundTransaction transaction,
                                     Money amount,
                                     String businessSceneCode,
                                     TradeType tradeType,
                                     String failureReason,
                                     LocalDateTime occurredAt) {
        tradeRepository.saveTradeOrder(buildFundTradeOrderSnapshot(
                transaction,
                amount,
                businessSceneCode,
                tradeType,
                TradeStatus.FAILED,
                failureReason,
                occurredAt
        ));
        tradeRepository.saveTradeFundOrder(buildFundTradeOrderExtension(transaction, amount, occurredAt));
    }

    private TradeOrder buildFundTradeOrderSnapshot(FundTransaction transaction,
                                                   Money amount,
                                                   String businessSceneCode,
                                                   TradeType tradeType,
                                                   TradeStatus targetStatus,
                                                   String failureReason,
                                                   LocalDateTime occurredAt) {
        Money normalizedAmount = normalizeTradeMoney(amount);
        TradeOrder existingTradeOrder = tradeRepository.findTradeOrderByTradeOrderNo(transaction.getOrderNo()).orElse(null);
        String bizOrderNo = resolveFundBizOrderNo(transaction, existingTradeOrder);
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
        Money fundDebitAmount = tradeType == TradeType.WITHDRAW
                ? (targetStatus == TradeStatus.SUCCEEDED ? normalizedAmount : payableAmount)
                : zero;
        TradeSplitPlan splitPlan = existingTradeOrder == null
                ? TradeSplitPlan.of(normalizedAmount.getCurrencyUnit(), zero, fundDebitAmount, zero, zero)
                : existingTradeOrder.getSplitPlan();
        return new TradeOrder(
                existingTradeOrder == null ? null : existingTradeOrder.getId(),
                transaction.getOrderNo(),
                existingTradeOrder == null ? buildFundRequestNo(transaction.getOrderNo()) : existingTradeOrder.getRequestNo(),
                existingTradeOrder == null ? tradeType : existingTradeOrder.getTradeType(),
                existingTradeOrder == null ? businessSceneCode : existingTradeOrder.getBusinessSceneCode(),
                TradeBusinessDomainCode.AICASH.name(),
                bizOrderNo,
                existingTradeOrder == null ? null : existingTradeOrder.getOriginalTradeOrderNo(),
                transaction.getUserId(),
                transaction.getUserId(),
                resolveFundTradePaymentMethod(transaction, existingTradeOrder),
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
                buildFundTradeMetadata(transaction),
                existingTradeOrder == null ? occurredAt : existingTradeOrder.getCreatedAt(),
                occurredAt
        );
    }

    private String resolveFundTradePaymentMethod(FundTransaction transaction, TradeOrder existingTradeOrder) {
        if (existingTradeOrder != null) {
            String existingPaymentMethod = normalizeOptionalText(existingTradeOrder.getPaymentMethod());
            if (existingPaymentMethod != null) {
                return existingPaymentMethod;
            }
        }
        if (transaction == null) {
            return "FUND_ACCOUNT";
        }
        if (transaction.getTransactionType() != FundTransactionType.REDEEM
                && transaction.getTransactionType() != FundTransactionType.FAST_REDEEM) {
            return "FUND_ACCOUNT";
        }
        String destination = normalizeRedeemDestination(extractRedeemExtInfoValue(transaction.getExtInfo(), "destination"));
        if (REDEEM_DESTINATION_BANK_CARD.equals(destination)) {
            String bankName = normalizeRedeemBankName(extractRedeemExtInfoValue(transaction.getExtInfo(), "bankName"));
            if (bankName != null) {
                return bankName;
            }
            return REDEEM_DESTINATION_BANK_CARD;
        }
        return "WALLET";
    }

    private TradeFundOrder buildFundTradeOrderExtension(FundTransaction transaction,
                                                        Money amount,
                                                        LocalDateTime occurredAt) {
        String bizOrderNo = resolveFundBizOrderNo(transaction, null);
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
                resolveFundTradeType(transaction),
                resolveFundShareAmount(transaction),
                resolveFundConfirmAmount(transaction, amount),
                occurredAt == null ? LocalDate.now() : occurredAt.toLocalDate(),
                occurredAt,
                null,
                occurredAt
        );
    }

    private TradeFundTradeType resolveFundTradeType(FundTransaction transaction) {
        return switch (transaction.getTransactionType()) {
            case SUBSCRIBE -> TradeFundTradeType.PURCHASE;
            case FAST_REDEEM -> TradeFundTradeType.FAST_REDEEM;
            case REDEEM -> TradeFundTradeType.REDEEM;
            case INCOME_SETTLE -> TradeFundTradeType.YIELD_SETTLE;
            case PRODUCT_SWITCH -> TradeFundTradeType.TRANSFER_OUT;
            case FREEZE -> TradeFundTradeType.PAY_FREEZE;
            default -> TradeFundTradeType.PURCHASE;
        };
    }

    private BigDecimal resolveFundShareAmount(FundTransaction transaction) {
        if (transaction.getConfirmedShare().compareTo(FundAmount.ZERO) > 0) {
            return transaction.getConfirmedShare().toBigDecimal();
        }
        if (transaction.getRequestShare().compareTo(FundAmount.ZERO) > 0) {
            return transaction.getRequestShare().toBigDecimal();
        }
        return FundAmount.ZERO.toBigDecimal();
    }

    private Money resolveFundConfirmAmount(FundTransaction transaction, Money referenceAmount) {
        CurrencyUnit currencyUnit = referenceAmount == null ? CurrencyUnit.of(DEFAULT_CURRENCY) : referenceAmount.getCurrencyUnit();
        if (transaction.getTransactionStatus() != FundTransactionStatus.CONFIRMED) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        return toMoney(firstPositiveFundAmount(transaction.getConfirmedAmount(), transaction.getRequestAmount()), currencyUnit.getCode());
    }

    private FundAmount firstPositiveFundAmount(FundAmount preferred, FundAmount fallback) {
        if (preferred != null && preferred.compareTo(FundAmount.ZERO) > 0) {
            return preferred.setScale(4, RoundingMode.HALF_UP);
        }
        if (fallback != null && fallback.compareTo(FundAmount.ZERO) > 0) {
            return fallback.setScale(4, RoundingMode.HALF_UP);
        }
        return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private Money toMoney(FundAmount amount, String currencyCode) {
        FundAmount normalized = amount == null ? FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP) : amount.setScale(4, RoundingMode.HALF_UP);
        String resolvedCurrencyCode = normalizeCurrency(currencyCode);
        return Money.of(
                CurrencyUnit.of(resolvedCurrencyCode),
                normalized.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        );
    }

    private Money normalizeTradeMoney(Money amount) {
        if (amount == null || amount.isNegative() || amount.isZero()) {
            throw new IllegalArgumentException("fund trade amount must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private String resolveTradeCurrencyCode(Money amount) {
        if (amount == null) {
            return DEFAULT_CURRENCY;
        }
        return amount.getCurrencyUnit().getCode();
    }

    private String buildFundAccountNo(Long userId, String fundCode) {
        return "FUNDACC-" + userId + "-" + normalizeFundCode(fundCode);
    }

    private String resolveFundBillMonth(LocalDateTime occurredAt) {
        LocalDate baseDate = occurredAt == null ? LocalDate.now() : occurredAt.toLocalDate();
        return String.format(Locale.ROOT, "%04d-%02d", baseDate.getYear(), baseDate.getMonthValue());
    }

    private String buildFundBillNo(String fundAccountNo, String billMonth) {
        return "AICASHBILL-" + fundAccountNo + "-" + billMonth.replace("-", "");
    }

    private String buildFundRequestNo(String orderNo) {
        return "FUND:" + orderNo;
    }

    private String resolveFundBizOrderNo(FundTransaction transaction, TradeOrder existingTradeOrder) {
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

    private String buildFundTradeMetadata(FundTransaction transaction) {
        StringBuilder builder = new StringBuilder();
        appendMetadataSegment(builder, "fundCode", transaction.getFundCode());
        appendMetadataSegment(builder, "fundTransactionType", transaction.getTransactionType().name());
        appendMetadataSegment(builder, "fundTransactionStatus", transaction.getTransactionStatus().name());
        appendMetadataSegment(builder, "fundBusinessNo", transaction.getBusinessNo());
        appendMetadataSegment(builder, "fundExtInfo", transaction.getExtInfo());
        return builder.length() == 0 ? null : builder.toString();
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

    private String normalizeOptionalText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("orderNo must not be blank");
        }
        if (!aiPayIdGenerator.validate(orderNo.trim())) {
            throw new IllegalArgumentException("orderNo must match AiPay 32-digit ID rule");
        }
    }

    private void ensureWalletBalanceEnoughForSubscribe(Long userId,
                                                       String currencyCode,
                                                       FundAmount subscribeAmount,
                                                       String businessNo) {
        if (isSystemSeedBusinessNo(businessNo)) {
            return;
        }
        Money requiredAmount = toMoney(subscribeAmount, currencyCode);
        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(userId, requiredAmount.getCurrencyUnit().getCode())
                .orElseThrow(() -> new IllegalArgumentException("账户余额不足"));
        Money availableBalance = walletAccount.getAvailableBalance();
        if (availableBalance == null || availableBalance.compareTo(requiredAmount) < 0) {
            throw new IllegalArgumentException("账户余额不足");
        }
    }

    private boolean isSystemSeedBusinessNo(String businessNo) {
        String normalizedBusinessNo = normalizeOptionalText(businessNo);
        if (normalizedBusinessNo == null) {
            return false;
        }
        return normalizedBusinessNo.toUpperCase(Locale.ROOT).startsWith(SYSTEM_SEED_BUSINESS_NO_PREFIX);
    }

    private String normalizeFundCode(String fundCode) {
        String normalized = FundProductCodes.normalizeOrDefault(fundCode);
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("fundCode length must be <= 32");
        }
        return normalized;
    }

    private String normalizeCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        String normalized = currencyCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 8) {
            throw new IllegalArgumentException("currencyCode length must be <= 8");
        }
        return normalized;
    }

    private String normalizeNullableCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return normalizeCurrency(currencyCode);
    }

    private String normalizeRedeemMode(String redeemMode) {
        if (redeemMode == null || redeemMode.isBlank()) {
            return DEFAULT_REDEEM_MODE;
        }
        String normalized = redeemMode.trim().toUpperCase(Locale.ROOT);
        if (!"NORMAL".equals(normalized) && !"FAST".equals(normalized)) {
            throw new IllegalArgumentException("redeemMode must be NORMAL or FAST");
        }
        return normalized;
    }

    private String buildRedeemExtInfo(String redeemMode, String redeemDestination, String bankName) {
        String normalizedMode = normalizeRedeemMode(redeemMode);
        String normalizedDestination = normalizeRedeemDestination(redeemDestination);
        String normalizedBankName = normalizeRedeemBankName(bankName);
        if (normalizedDestination == null && normalizedBankName != null) {
            normalizedDestination = REDEEM_DESTINATION_BANK_CARD;
        }
        if (normalizedDestination == null) {
            normalizedDestination = REDEEM_DESTINATION_BALANCE;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("mode=").append(normalizedMode);
        builder.append("|destination=").append(normalizedDestination);
        if (REDEEM_DESTINATION_BANK_CARD.equals(normalizedDestination) && normalizedBankName != null) {
            builder.append("|bankName=").append(normalizedBankName);
        }
        return builder.toString();
    }

    private String extractRedeemExtInfoValue(String extInfo, String key) {
        String normalizedKey = normalizeOptionalText(key);
        String normalizedExtInfo = normalizeOptionalText(extInfo);
        if (normalizedKey == null || normalizedExtInfo == null) {
            return null;
        }
        String[] segments = normalizedExtInfo.split("\\|");
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                continue;
            }
            String[] keyValue = segment.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            if (normalizedKey.equalsIgnoreCase(keyValue[0].trim())) {
                return normalizeOptionalText(keyValue[1]);
            }
        }
        return null;
    }

    private String normalizeRedeemDestination(String redeemDestination) {
        String normalized = normalizeOptionalText(redeemDestination);
        if (normalized == null) {
            return null;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (REDEEM_DESTINATION_BALANCE.equals(upper)) {
            return REDEEM_DESTINATION_BALANCE;
        }
        if (REDEEM_DESTINATION_BANK_CARD.equals(upper)) {
            return REDEEM_DESTINATION_BANK_CARD;
        }
        if (upper.contains("BALANCE") || normalized.contains("余额")) {
            return REDEEM_DESTINATION_BALANCE;
        }
        if (upper.contains("BANK") || normalized.contains("银行卡") || normalized.contains("银行")) {
            return REDEEM_DESTINATION_BANK_CARD;
        }
        return null;
    }

    private String normalizeRedeemBankName(String bankName) {
        String normalized = normalizeOptionalText(bankName);
        if (normalized == null) {
            return null;
        }
        if (normalized.contains("余额")) {
            return null;
        }
        Matcher matcher = BANK_NAME_PATTERN.matcher(normalized);
        String resolved = null;
        while (matcher.find()) {
            String current = normalizeOptionalText(matcher.group(1));
            if (current != null && !"银行".equals(current)) {
                resolved = current;
            }
        }
        if (resolved != null) {
            if (resolved.length() > 64) {
                return resolved.substring(0, 64);
            }
            return resolved;
        }
        return null;
    }

    private FundProductStatus parseProductStatus(String productStatus) {
        if (productStatus == null || productStatus.isBlank()) {
            return null;
        }
        try {
            return FundProductStatus.valueOf(productStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("productStatus must be ACTIVE, PAUSED or CLOSED");
        }
    }

    private FundAmount normalizePositive(FundAmount source, String fieldName) {
        if (source == null || source.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return source.setScale(4, RoundingMode.HALF_UP);
    }

    private FundAmount normalizeNonNegative(FundAmount source, String fieldName) {
        if (source == null || source.compareTo(FundAmount.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return source.setScale(4, RoundingMode.HALF_UP);
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

    private LocalDate parseBizDate(String rawBizDate) {
        if (rawBizDate == null || rawBizDate.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(rawBizDate);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("bizDate format must be yyyy-MM-dd");
        }
    }

    private FundAmount resolveSubscribeShare(FundTransaction transaction,
                                             FundAmount confirmedShare,
                                             FundAmount nav,
                                             FundAmount latestNav) {
        if (confirmedShare != null && confirmedShare.compareTo(FundAmount.ZERO) > 0) {
            return confirmedShare.setScale(4, RoundingMode.HALF_UP);
        }
        FundAmount navValue = nav;
        if (navValue == null || navValue.compareTo(FundAmount.ZERO) <= 0) {
            navValue = latestNav;
        }
        if (navValue == null || navValue.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException("nav or confirmedShare must be provided");
        }
        return transaction.getRequestAmount().divide(navValue, 4, RoundingMode.HALF_UP);
    }

    private FundAmount pickPositive(FundAmount preferred, FundAmount fallback, String fieldName) {
        if (preferred != null && preferred.compareTo(FundAmount.ZERO) > 0) {
            return preferred.setScale(4, RoundingMode.HALF_UP);
        }
        if (fallback != null && fallback.compareTo(FundAmount.ZERO) > 0) {
            return fallback.setScale(4, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException(fieldName + " must be greater than 0");
    }
}
