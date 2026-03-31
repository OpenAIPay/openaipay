package cn.openaipay.infrastructure.loantrade;

import cn.openaipay.domain.loantrade.model.LoanTradeOperationType;
import cn.openaipay.domain.loantrade.model.LoanTradeOrder;
import cn.openaipay.domain.loantrade.model.LoanTradeOrderStatus;
import cn.openaipay.domain.loantrade.repository.LoanTradeRepository;
import cn.openaipay.infrastructure.loantrade.dataobject.LoanTradeOrderDO;
import cn.openaipay.infrastructure.loantrade.mapper.LoanTradeOrderMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 爱借交易仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class LoanTradeRepositoryImpl implements LoanTradeRepository {

    /** 借款交易订单信息 */
    private final LoanTradeOrderMapper loanTradeOrderMapper;

    public LoanTradeRepositoryImpl(LoanTradeOrderMapper loanTradeOrderMapper) {
        this.loanTradeOrderMapper = loanTradeOrderMapper;
    }

    /**
     * 按XID与ID查找记录。
     */
    @Override
    public Optional<LoanTradeOrder> findByXidAndBranchId(String xid, String branchId) {
        return loanTradeOrderMapper.findByXidAndBranchId(xid, branchId).map(this::toDomain);
    }

    /**
     * 按XID与ID查找记录并加锁。
     */
    @Override
    public Optional<LoanTradeOrder> findByXidAndBranchIdForUpdate(String xid, String branchId) {
        return loanTradeOrderMapper.findByXidAndBranchIdForUpdate(xid, branchId).map(this::toDomain);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public LoanTradeOrder save(LoanTradeOrder loanTradeOrder) {
        LoanTradeOrderDO entity = loanTradeOrderMapper.findByXidAndBranchId(
                        loanTradeOrder.getXid(),
                        loanTradeOrder.getBranchId()
                )
                .orElse(new LoanTradeOrderDO());
        fillEntity(entity, loanTradeOrder);
        LoanTradeOrderDO saved = loanTradeOrderMapper.save(entity);
        return toDomain(saved);
    }

    private LoanTradeOrder toDomain(LoanTradeOrderDO entity) {
        return new LoanTradeOrder(
                entity.getId(),
                entity.getXid(),
                entity.getBranchId(),
                entity.getBusinessNo(),
                entity.getAccountNo(),
                LoanTradeOperationType.from(entity.getOperationType()),
                LoanTradeOrderStatus.from(entity.getStatus()),
                entity.getRequestAmount(),
                entity.getInterestAmount(),
                entity.getPrincipalAmount(),
                entity.getFineAmount(),
                entity.getInterestBranchId(),
                entity.getPrincipalBranchId(),
                entity.getFineBranchId(),
                entity.getAnnualRatePercent(),
                entity.getRemainingTermMonths(),
                entity.getMonthlyPayment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillEntity(LoanTradeOrderDO entity, LoanTradeOrder loanTradeOrder) {
        LocalDateTime now = LocalDateTime.now();
        entity.setXid(loanTradeOrder.getXid());
        entity.setBranchId(loanTradeOrder.getBranchId());
        entity.setBusinessNo(loanTradeOrder.getBusinessNo());
        entity.setAccountNo(loanTradeOrder.getAccountNo());
        entity.setOperationType(loanTradeOrder.getOperationType().name());
        entity.setStatus(loanTradeOrder.getStatus().name());
        entity.setRequestAmount(loanTradeOrder.getRequestAmount());
        entity.setInterestAmount(loanTradeOrder.getInterestAmount());
        entity.setPrincipalAmount(loanTradeOrder.getPrincipalAmount());
        entity.setFineAmount(loanTradeOrder.getFineAmount());
        entity.setInterestBranchId(loanTradeOrder.getInterestBranchId());
        entity.setPrincipalBranchId(loanTradeOrder.getPrincipalBranchId());
        entity.setFineBranchId(loanTradeOrder.getFineBranchId());
        entity.setAnnualRatePercent(loanTradeOrder.getAnnualRatePercent());
        entity.setRemainingTermMonths(loanTradeOrder.getRemainingTermMonths());
        entity.setMonthlyPayment(loanTradeOrder.getMonthlyPayment());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(loanTradeOrder.getCreatedAt() == null ? now : loanTradeOrder.getCreatedAt());
        }
        entity.setUpdatedAt(loanTradeOrder.getUpdatedAt() == null ? now : loanTradeOrder.getUpdatedAt());
    }
}
