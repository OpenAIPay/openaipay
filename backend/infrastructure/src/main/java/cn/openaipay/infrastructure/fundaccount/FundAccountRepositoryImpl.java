package cn.openaipay.infrastructure.fundaccount;

import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundAccountStatus;
import cn.openaipay.domain.fundaccount.model.FundFastRedeemQuota;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendar;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendarStatus;
import cn.openaipay.domain.fundaccount.model.FundProduct;
import cn.openaipay.domain.fundaccount.model.FundProductStatus;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.model.FundUserFastRedeemQuota;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.shared.number.FundAmount;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundAccountDO;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundFastRedeemQuotaDO;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundIncomeCalendarDO;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundProductDO;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundTransactionDO;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundUserFastRedeemQuotaDO;
import cn.openaipay.infrastructure.fundaccount.mapper.FundAccountMapper;
import cn.openaipay.infrastructure.fundaccount.mapper.FundFastRedeemQuotaMapper;
import cn.openaipay.infrastructure.fundaccount.mapper.FundIncomeCalendarMapper;
import cn.openaipay.infrastructure.fundaccount.mapper.FundProductMapper;
import cn.openaipay.infrastructure.fundaccount.mapper.FundTransactionMapper;
import cn.openaipay.infrastructure.fundaccount.mapper.FundUserFastRedeemQuotaMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 基金账户仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class FundAccountRepositoryImpl implements FundAccountRepository {

    /** FundAccountMapper组件 */
    private final FundAccountMapper fundAccountMapper;
    /** Fund交易Persistence组件 */
    private final FundTransactionMapper fundTransactionMapper;
    /** FundProductMapper组件 */
    private final FundProductMapper fundProductMapper;
    /** FundIncome日历Persistence组件 */
    private final FundIncomeCalendarMapper fundIncomeCalendarMapper;
    /** FundFast赎回QuotaMapper组件 */
    private final FundFastRedeemQuotaMapper fundFastRedeemQuotaMapper;
    /** FundUserFast赎回QuotaMapper组件 */
    private final FundUserFastRedeemQuotaMapper fundUserFastRedeemQuotaMapper;

    public FundAccountRepositoryImpl(FundAccountMapper fundAccountMapper,
                                     FundTransactionMapper fundTransactionMapper,
                                     FundProductMapper fundProductMapper,
                                     FundIncomeCalendarMapper fundIncomeCalendarMapper,
                                     FundFastRedeemQuotaMapper fundFastRedeemQuotaMapper,
                                     FundUserFastRedeemQuotaMapper fundUserFastRedeemQuotaMapper) {
        this.fundAccountMapper = fundAccountMapper;
        this.fundTransactionMapper = fundTransactionMapper;
        this.fundProductMapper = fundProductMapper;
        this.fundIncomeCalendarMapper = fundIncomeCalendarMapper;
        this.fundFastRedeemQuotaMapper = fundFastRedeemQuotaMapper;
        this.fundUserFastRedeemQuotaMapper = fundUserFastRedeemQuotaMapper;
    }

    /**
     * 按用户ID与基金编码查找记录。
     */
    @Override
    public Optional<FundAccount> findByUserIdAndFundCode(Long userId, String fundCode) {
        return fundAccountMapper.findByUserIdAndFundCode(userId, fundCode).map(this::toDomainAccount);
    }

    /**
     * 按用户ID与基金编码查找记录并加锁。
     */
    @Override
    public Optional<FundAccount> findByUserIdAndFundCodeForUpdate(Long userId, String fundCode) {
        return fundAccountMapper.findByUserIdAndFundCodeForUpdate(userId, fundCode).map(this::toDomainAccount);
    }

    /**
     * 按用户ID查找全部信息。
     */
    @Override
    public List<FundAccount> findAllByUserId(Long userId) {
        return fundAccountMapper.findByUserId(userId).stream().map(this::toDomainAccount).toList();
    }

    /**
     * 按基金编码查找全部信息。
     */
    @Override
    public List<FundAccount> findAllByFundCode(String fundCode) {
        return fundAccountMapper.findByFundCode(fundCode).stream().map(this::toDomainAccount).toList();
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public FundAccount save(FundAccount fundAccount) {
        validateAccountShares(fundAccount);
        FundAccountDO entity = fundAccountMapper.findByUserIdAndFundCode(
                        fundAccount.getUserId(),
                        fundAccount.getFundCode()
                )
                .orElse(new FundAccountDO());
        fillAccountDO(entity, fundAccount);
        FundAccountDO saved = fundAccountMapper.save(entity);
        return toDomainAccount(saved);
    }

    /**
     * 查找业务数据。
     */
    @Override
    public Optional<FundTransaction> findTransaction(String orderNo) {
        return fundTransactionMapper.findByOrderNo(orderNo).map(this::toDomainTransaction);
    }

    /**
     * 查找用于更新信息。
     */
    @Override
    public Optional<FundTransaction> findTransactionForUpdate(String orderNo) {
        return fundTransactionMapper.findByOrderNoForUpdate(orderNo).map(this::toDomainTransaction);
    }

    /**
     * 按单号查找记录。
     */
    @Override
    public Optional<FundTransaction> findTransactionByBusinessNoAndType(Long userId,
                                                                        String fundCode,
                                                                        FundTransactionType transactionType,
                                                                        String businessNo) {
        return fundTransactionMapper.findByBusinessNoAndType(
                userId,
                fundCode,
                transactionType.name(),
                businessNo
        ).map(this::toDomainTransaction);
    }

    /**
     * 按条件查找记录。
     */
    @Override
    public List<FundTransaction> findRecentTransactionsByTypes(String fundCode,
                                                               List<FundTransactionType> transactionTypes,
                                                               int limit) {
        List<String> typeCodes = transactionTypes == null
                ? List.of()
                : transactionTypes.stream()
                .filter(type -> type != null)
                .map(FundTransactionType::name)
                .toList();
        String normalizedFundCode = fundCode == null || fundCode.isBlank()
                ? null
                : fundCode.trim().toUpperCase(Locale.ROOT);
        return fundTransactionMapper.findRecentByTypes(normalizedFundCode, typeCodes, limit)
                .stream()
                .map(this::toDomainTransaction)
                .toList();
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public FundTransaction saveTransaction(FundTransaction transaction) {
        FundTransactionDO entity = fundTransactionMapper.findByOrderNo(transaction.getOrderNo())
                .orElse(new FundTransactionDO());
        fillTransactionDO(entity, transaction);
        FundTransactionDO saved = fundTransactionMapper.save(entity);
        return toDomainTransaction(saved);
    }

    /**
     * 查找业务数据。
     */
    @Override
    public Optional<FundProduct> findProduct(String fundCode) {
        return fundProductMapper.findByFundCode(fundCode).map(this::toDomainProduct);
    }

    /**
     * 查找用于更新信息。
     */
    @Override
    public Optional<FundProduct> findProductForUpdate(String fundCode) {
        return fundProductMapper.findByFundCodeForUpdate(fundCode).map(this::toDomainProduct);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public FundProduct saveProduct(FundProduct product) {
        FundProductDO entity = fundProductMapper.findByFundCode(product.getFundCode())
                .orElse(new FundProductDO());
        fillProductDO(entity, product);
        FundProductDO saved = fundProductMapper.save(entity);
        return toDomainProduct(saved);
    }

    /**
     * 查找收益日历信息。
     */
    @Override
    public Optional<FundIncomeCalendar> findIncomeCalendar(String fundCode, LocalDate bizDate) {
        return fundIncomeCalendarMapper.findByFundCodeAndBizDate(fundCode, bizDate).map(this::toDomainIncomeCalendar);
    }

    /**
     * 查找收益日历用于更新信息。
     */
    @Override
    public Optional<FundIncomeCalendar> findIncomeCalendarForUpdate(String fundCode, LocalDate bizDate) {
        return fundIncomeCalendarMapper.findByFundCodeAndBizDateForUpdate(fundCode, bizDate)
                .map(this::toDomainIncomeCalendar);
    }

    /**
     * 保存收益日历信息。
     */
    @Override
    @Transactional
    public FundIncomeCalendar saveIncomeCalendar(FundIncomeCalendar calendar) {
        FundIncomeCalendarDO entity = fundIncomeCalendarMapper.findByFundCodeAndBizDate(
                        calendar.getFundCode(),
                        calendar.getBizDate()
                )
                .orElse(new FundIncomeCalendarDO());
        fillIncomeCalendarDO(entity, calendar);
        FundIncomeCalendarDO saved = fundIncomeCalendarMapper.save(entity);
        return toDomainIncomeCalendar(saved);
    }

    /**
     * 查找额度用于更新信息。
     */
    @Override
    public Optional<FundFastRedeemQuota> findFastRedeemQuotaForUpdate(String fundCode, LocalDate quotaDate) {
        return fundFastRedeemQuotaMapper.findByFundCodeAndQuotaDateForUpdate(fundCode, quotaDate)
                .map(this::toDomainFastRedeemQuota);
    }

    /**
     * 保存额度信息。
     */
    @Override
    @Transactional
    public FundFastRedeemQuota saveFastRedeemQuota(FundFastRedeemQuota quota) {
        FundFastRedeemQuotaDO entity = fundFastRedeemQuotaMapper.findByFundCodeAndQuotaDate(
                        quota.getFundCode(),
                        quota.getQuotaDate()
                )
                .orElse(new FundFastRedeemQuotaDO());
        fillFastRedeemQuotaDO(entity, quota);
        FundFastRedeemQuotaDO saved = fundFastRedeemQuotaMapper.save(entity);
        return toDomainFastRedeemQuota(saved);
    }

    /**
     * 查找用户额度用于更新信息。
     */
    @Override
    public Optional<FundUserFastRedeemQuota> findUserFastRedeemQuotaForUpdate(String fundCode, Long userId, LocalDate quotaDate) {
        return fundUserFastRedeemQuotaMapper.findByFundCodeAndUserIdAndQuotaDateForUpdate(fundCode, userId, quotaDate)
                .map(this::toDomainUserFastRedeemQuota);
    }

    /**
     * 保存用户额度信息。
     */
    @Override
    @Transactional
    public FundUserFastRedeemQuota saveUserFastRedeemQuota(FundUserFastRedeemQuota quota) {
        FundUserFastRedeemQuotaDO entity = fundUserFastRedeemQuotaMapper.findByFundCodeAndUserIdAndQuotaDate(
                        quota.getFundCode(),
                        quota.getUserId(),
                        quota.getQuotaDate()
                )
                .orElse(new FundUserFastRedeemQuotaDO());
        fillUserFastRedeemQuotaDO(entity, quota);
        FundUserFastRedeemQuotaDO saved = fundUserFastRedeemQuotaMapper.save(entity);
        return toDomainUserFastRedeemQuota(saved);
    }

    private FundAccount toDomainAccount(FundAccountDO entity) {
        return new FundAccount(
                entity.getUserId(),
                entity.getFundCode(),
                entity.getCurrencyCode(),
                entity.getAvailableShare(),
                entity.getFrozenShare(),
                entity.getPendingSubscribeAmount(),
                entity.getPendingRedeemShare(),
                entity.getAccumulatedIncome(),
                entity.getYesterdayIncome(),
                entity.getLatestNav(),
                FundAccountStatus.valueOf(entity.getAccountStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FundTransaction toDomainTransaction(FundTransactionDO entity) {
        return new FundTransaction(
                entity.getOrderNo(),
                entity.getUserId(),
                entity.getFundCode(),
                FundTransactionType.valueOf(entity.getTransactionType()),
                FundTransactionStatus.valueOf(entity.getTransactionStatus()),
                entity.getRequestAmount(),
                entity.getRequestShare(),
                entity.getConfirmedAmount(),
                entity.getConfirmedShare(),
                entity.getBusinessNo(),
                entity.getExtInfo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FundProduct toDomainProduct(FundProductDO entity) {
        return new FundProduct(
                entity.getFundCode(),
                entity.getProductName(),
                entity.getCurrencyCode(),
                FundProductStatus.valueOf(entity.getProductStatus()),
                entity.getSingleSubscribeMinAmount(),
                entity.getSingleSubscribeMaxAmount(),
                entity.getDailySubscribeMaxAmount(),
                entity.getSingleRedeemMinShare(),
                entity.getSingleRedeemMaxShare(),
                entity.getDailyRedeemMaxShare(),
                entity.getFastRedeemDailyQuota(),
                entity.getFastRedeemPerUserDailyQuota(),
                Boolean.TRUE.equals(entity.getSwitchEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FundIncomeCalendar toDomainIncomeCalendar(FundIncomeCalendarDO entity) {
        return new FundIncomeCalendar(
                entity.getFundCode(),
                entity.getBizDate(),
                entity.getNav(),
                entity.getIncomePer10k(),
                FundIncomeCalendarStatus.valueOf(entity.getCalendarStatus()),
                entity.getPublishedAt(),
                entity.getSettledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FundFastRedeemQuota toDomainFastRedeemQuota(FundFastRedeemQuotaDO entity) {
        return new FundFastRedeemQuota(
                entity.getFundCode(),
                entity.getQuotaDate(),
                entity.getQuotaLimit(),
                entity.getQuotaUsed(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FundUserFastRedeemQuota toDomainUserFastRedeemQuota(FundUserFastRedeemQuotaDO entity) {
        return new FundUserFastRedeemQuota(
                entity.getFundCode(),
                entity.getUserId(),
                entity.getQuotaDate(),
                entity.getQuotaLimit(),
                entity.getQuotaUsed(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillAccountDO(FundAccountDO entity, FundAccount account) {
        LocalDateTime now = LocalDateTime.now();
        entity.setUserId(account.getUserId());
        entity.setFundCode(account.getFundCode());
        entity.setCurrencyCode(account.getCurrencyCode());
        entity.setAvailableShare(account.getAvailableShare());
        entity.setFrozenShare(account.getFrozenShare());
        entity.setPendingSubscribeAmount(account.getPendingSubscribeAmount());
        entity.setPendingRedeemShare(account.getPendingRedeemShare());
        entity.setAccumulatedIncome(account.getAccumulatedIncome());
        entity.setYesterdayIncome(account.getYesterdayIncome());
        entity.setLatestNav(account.getLatestNav());
        entity.setAccountStatus(account.getAccountStatus().name());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() == null ? now : account.getCreatedAt());
        }
        entity.setUpdatedAt(account.getUpdatedAt() == null ? now : account.getUpdatedAt());
    }

    private void fillTransactionDO(FundTransactionDO entity, FundTransaction transaction) {
        LocalDateTime now = LocalDateTime.now();
        entity.setOrderNo(transaction.getOrderNo());
        entity.setUserId(transaction.getUserId());
        entity.setFundCode(transaction.getFundCode());
        entity.setTransactionType(transaction.getTransactionType().name());
        entity.setTransactionStatus(transaction.getTransactionStatus().name());
        entity.setRequestAmount(transaction.getRequestAmount());
        entity.setRequestShare(transaction.getRequestShare());
        entity.setConfirmedAmount(transaction.getConfirmedAmount());
        entity.setConfirmedShare(transaction.getConfirmedShare());
        entity.setBusinessNo(transaction.getBusinessNo());
        entity.setExtInfo(transaction.getExtInfo());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(transaction.getCreatedAt() == null ? now : transaction.getCreatedAt());
        }
        entity.setUpdatedAt(transaction.getUpdatedAt() == null ? now : transaction.getUpdatedAt());
    }

    private void fillProductDO(FundProductDO entity, FundProduct product) {
        LocalDateTime now = LocalDateTime.now();
        entity.setFundCode(product.getFundCode());
        entity.setProductName(product.getProductName());
        entity.setCurrencyCode(product.getCurrencyCode());
        entity.setProductStatus(product.getProductStatus().name());
        entity.setSingleSubscribeMinAmount(product.getSingleSubscribeMinAmount());
        entity.setSingleSubscribeMaxAmount(product.getSingleSubscribeMaxAmount());
        entity.setDailySubscribeMaxAmount(product.getDailySubscribeMaxAmount());
        entity.setSingleRedeemMinShare(product.getSingleRedeemMinShare());
        entity.setSingleRedeemMaxShare(product.getSingleRedeemMaxShare());
        entity.setDailyRedeemMaxShare(product.getDailyRedeemMaxShare());
        entity.setFastRedeemDailyQuota(product.getFastRedeemDailyQuota());
        entity.setFastRedeemPerUserDailyQuota(product.getFastRedeemPerUserDailyQuota());
        entity.setSwitchEnabled(product.isSwitchEnabled());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(product.getCreatedAt() == null ? now : product.getCreatedAt());
        }
        entity.setUpdatedAt(product.getUpdatedAt() == null ? now : product.getUpdatedAt());
    }

    private void fillIncomeCalendarDO(FundIncomeCalendarDO entity, FundIncomeCalendar calendar) {
        LocalDateTime now = LocalDateTime.now();
        entity.setFundCode(calendar.getFundCode());
        entity.setBizDate(calendar.getBizDate());
        entity.setNav(calendar.getNav());
        entity.setIncomePer10k(calendar.getIncomePer10k());
        entity.setCalendarStatus(calendar.getCalendarStatus().name());
        entity.setPublishedAt(calendar.getPublishedAt());
        entity.setSettledAt(calendar.getSettledAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(calendar.getCreatedAt() == null ? now : calendar.getCreatedAt());
        }
        entity.setUpdatedAt(calendar.getUpdatedAt() == null ? now : calendar.getUpdatedAt());
    }

    private void fillFastRedeemQuotaDO(FundFastRedeemQuotaDO entity, FundFastRedeemQuota quota) {
        LocalDateTime now = LocalDateTime.now();
        entity.setFundCode(quota.getFundCode());
        entity.setQuotaDate(quota.getQuotaDate());
        entity.setQuotaLimit(quota.getQuotaLimit());
        entity.setQuotaUsed(quota.getQuotaUsed());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(quota.getCreatedAt() == null ? now : quota.getCreatedAt());
        }
        entity.setUpdatedAt(quota.getUpdatedAt() == null ? now : quota.getUpdatedAt());
    }

    private void fillUserFastRedeemQuotaDO(FundUserFastRedeemQuotaDO entity, FundUserFastRedeemQuota quota) {
        LocalDateTime now = LocalDateTime.now();
        entity.setFundCode(quota.getFundCode());
        entity.setUserId(quota.getUserId());
        entity.setQuotaDate(quota.getQuotaDate());
        entity.setQuotaLimit(quota.getQuotaLimit());
        entity.setQuotaUsed(quota.getQuotaUsed());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(quota.getCreatedAt() == null ? now : quota.getCreatedAt());
        }
        entity.setUpdatedAt(quota.getUpdatedAt() == null ? now : quota.getUpdatedAt());
    }

    private void validateAccountShares(FundAccount account) {
        requireNonNegative(account.getAvailableShare(), "availableShare");
        requireNonNegative(account.getFrozenShare(), "frozenShare");
        requireNonNegative(account.getPendingSubscribeAmount(), "pendingSubscribeAmount");
        requireNonNegative(account.getPendingRedeemShare(), "pendingRedeemShare");
        requireNonNegative(account.getAccumulatedIncome(), "accumulatedIncome");
        requireNonNegative(account.getYesterdayIncome(), "yesterdayIncome");
        requireNonNegative(account.getLatestNav(), "latestNav");
    }

    private void requireNonNegative(FundAmount amount, String fieldName) {
        if (amount == null) {
            return;
        }
        if (amount.compareTo(FundAmount.ZERO) < 0) {
            throw new IllegalStateException(fieldName + " must not be less than 0");
        }
    }
}
