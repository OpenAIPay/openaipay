package cn.openaipay.infrastructure.loanaccount;

import cn.openaipay.domain.loanaccount.model.LoanAccountProfile;
import cn.openaipay.domain.loanaccount.repository.LoanAccountProfileRepository;
import cn.openaipay.infrastructure.loanaccount.dataobject.LoanAccountProfileDO;
import cn.openaipay.infrastructure.loanaccount.mapper.LoanAccountProfileMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 爱借账户档案仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Repository
public class LoanAccountProfileRepositoryImpl implements LoanAccountProfileRepository {

    /** 档案持久化接口。 */
    private final LoanAccountProfileMapper loanAccountProfileMapper;

    public LoanAccountProfileRepositoryImpl(LoanAccountProfileMapper loanAccountProfileMapper) {
        this.loanAccountProfileMapper = loanAccountProfileMapper;
    }

    /**
     * 按账户号查询档案。
     */
    @Override
    public Optional<LoanAccountProfile> findByAccountNo(String accountNo) {
        return loanAccountProfileMapper.findByAccountNo(accountNo).map(this::toDomain);
    }

    /**
     * 按账户号查询档案并加锁。
     */
    @Override
    public Optional<LoanAccountProfile> findByAccountNoForUpdate(String accountNo) {
        return loanAccountProfileMapper.findByAccountNoForUpdate(accountNo).map(this::toDomain);
    }

    /**
     * 保存档案。
     */
    @Override
    @Transactional
    public LoanAccountProfile save(LoanAccountProfile profile) {
        LoanAccountProfileDO entity = loanAccountProfileMapper.findByAccountNo(profile.getAccountNo())
                .orElse(new LoanAccountProfileDO());
        fillEntity(entity, profile);
        LoanAccountProfileDO saved = loanAccountProfileMapper.save(entity);
        return toDomain(saved);
    }

    private LoanAccountProfile toDomain(LoanAccountProfileDO entity) {
        return new LoanAccountProfile(
                entity.getId(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getAnnualRatePercent(),
                entity.getOriginalAnnualRatePercent(),
                entity.getTotalTermMonths(),
                entity.getDrawDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillEntity(LoanAccountProfileDO entity, LoanAccountProfile profile) {
        LocalDateTime now = LocalDateTime.now();
        entity.setAccountNo(profile.getAccountNo());
        entity.setUserId(profile.getUserId());
        entity.setAnnualRatePercent(profile.getAnnualRatePercent());
        entity.setOriginalAnnualRatePercent(profile.getOriginalAnnualRatePercent());
        entity.setTotalTermMonths(profile.getTotalTermMonths());
        entity.setDrawDate(profile.getDrawDate());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(profile.getCreatedAt() == null ? now : profile.getCreatedAt());
        }
        entity.setUpdatedAt(profile.getUpdatedAt() == null ? now : profile.getUpdatedAt());
    }
}
