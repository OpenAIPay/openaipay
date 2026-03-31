package cn.openaipay.infrastructure.fundtrade;

import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundTransactionDO;
import cn.openaipay.infrastructure.fundaccount.mapper.FundTransactionMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基金交易仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Repository
public class FundTradeRepositoryImpl implements FundTradeRepository {

    /** 基金交易持久化接口。 */
    private final FundTransactionMapper fundTransactionMapper;

    public FundTradeRepositoryImpl(FundTransactionMapper fundTransactionMapper) {
        this.fundTransactionMapper = fundTransactionMapper;
    }

    /**
     * 按订单号查询基金交易。
     */
    @Override
    public Optional<FundTransaction> findTransaction(String orderNo) {
        return fundTransactionMapper.findByOrderNo(orderNo).map(this::toDomainTransaction);
    }

    /**
     * 按订单号加锁查询基金交易。
     */
    @Override
    public Optional<FundTransaction> findTransactionForUpdate(String orderNo) {
        return fundTransactionMapper.findByOrderNoForUpdate(orderNo).map(this::toDomainTransaction);
    }

    /**
     * 按业务编号查询基金交易。
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
     * 按基金编码与交易类型批量查询最近基金交易。
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
     * 按用户、基金、类型查询指定更新时间范围内已确认交易。
     */
    @Override
    public List<FundTransaction> findConfirmedTransactionsUpdatedInRange(Long userId,
                                                                         String fundCode,
                                                                         LocalDateTime updatedFromInclusive,
                                                                         LocalDateTime updatedToInclusive,
                                                                         List<FundTransactionType> transactionTypes) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        String normalizedFundCode = fundCode == null || fundCode.isBlank()
                ? null
                : fundCode.trim().toUpperCase(Locale.ROOT);
        if (normalizedFundCode == null) {
            return List.of();
        }
        List<String> typeCodes = transactionTypes == null
                ? List.of()
                : transactionTypes.stream()
                .filter(type -> type != null)
                .map(FundTransactionType::name)
                .toList();
        return fundTransactionMapper.findByStatusAndUpdatedRange(
                        userId,
                        normalizedFundCode,
                        FundTransactionStatus.CONFIRMED.name(),
                        updatedFromInclusive,
                        updatedToInclusive,
                        typeCodes
                ).stream()
                .map(this::toDomainTransaction)
                .toList();
    }

    /**
     * 保存基金交易。
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
}
