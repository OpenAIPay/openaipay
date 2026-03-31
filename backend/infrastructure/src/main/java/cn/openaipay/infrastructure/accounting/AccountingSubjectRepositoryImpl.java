package cn.openaipay.infrastructure.accounting;

import cn.openaipay.domain.accounting.model.AccountingSubject;
import cn.openaipay.domain.accounting.model.AccountingSubjectQuery;
import cn.openaipay.domain.accounting.model.DebitCreditFlag;
import cn.openaipay.domain.accounting.model.SubjectType;
import cn.openaipay.domain.accounting.repository.AccountingSubjectRepository;
import cn.openaipay.infrastructure.accounting.dataobject.AccountingSubjectDO;
import cn.openaipay.infrastructure.accounting.mapper.AccountingSubjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会计科目仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Repository
public class AccountingSubjectRepositoryImpl implements AccountingSubjectRepository {

    /** 科目信息 */
    private final AccountingSubjectMapper accountingSubjectMapper;

    public AccountingSubjectRepositoryImpl(AccountingSubjectMapper accountingSubjectMapper) {
        this.accountingSubjectMapper = accountingSubjectMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public AccountingSubject save(AccountingSubject subject) {
        AccountingSubjectDO entity = subject.getId() == null
                ? accountingSubjectMapper.findBySubjectCode(subject.getSubjectCode()).orElse(new AccountingSubjectDO())
                : accountingSubjectMapper.findById(subject.getId()).orElse(new AccountingSubjectDO());
        fillDO(entity, subject);
        return toDomain(accountingSubjectMapper.save(entity));
    }

    /**
     * 按科目编码查找记录。
     */
    @Override
    public Optional<AccountingSubject> findBySubjectCode(String subjectCode) {
        return accountingSubjectMapper.findBySubjectCode(subjectCode).map(this::toDomain);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public List<AccountingSubject> list(AccountingSubjectQuery query) {
        QueryWrapper<AccountingSubjectDO> wrapper = new QueryWrapper<>();
        if (query != null) {
            if (query.enabled() != null) {
                wrapper.eq("enabled", query.enabled());
            }
            eqIfPresent(wrapper, "subject_type", query.subjectType());
            wrapper.orderByAsc("subject_code", "id");
            wrapper.last("LIMIT " + normalizeLimit(query.limit()));
        }
        return accountingSubjectMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private AccountingSubject toDomain(AccountingSubjectDO entity) {
        return new AccountingSubject(
                entity.getId(),
                entity.getSubjectCode(),
                entity.getSubjectName(),
                parseSubjectType(entity.getSubjectType()),
                parseDirection(entity.getBalanceDirection()),
                entity.getParentSubjectCode(),
                entity.getLevelNo(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getRemark(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(AccountingSubjectDO entity, AccountingSubject subject) {
        LocalDateTime now = LocalDateTime.now();
        entity.setSubjectCode(subject.getSubjectCode());
        entity.setSubjectName(subject.getSubjectName());
        entity.setSubjectType(subject.getSubjectType().name());
        entity.setBalanceDirection(subject.getBalanceDirection().name());
        entity.setParentSubjectCode(subject.getParentSubjectCode());
        entity.setLevelNo(subject.getLevelNo());
        entity.setEnabled(subject.isEnabled());
        entity.setRemark(subject.getRemark());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(subject.getCreatedAt() == null ? now : subject.getCreatedAt());
        }
        entity.setUpdatedAt(subject.getUpdatedAt() == null ? now : subject.getUpdatedAt());
    }

    private SubjectType parseSubjectType(String raw) {
        if (raw == null || raw.isBlank()) {
            return SubjectType.ASSET;
        }
        try {
            return SubjectType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return SubjectType.ASSET;
        }
    }

    private DebitCreditFlag parseDirection(String raw) {
        if (raw == null || raw.isBlank()) {
            return DebitCreditFlag.DEBIT;
        }
        try {
            return DebitCreditFlag.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return DebitCreditFlag.DEBIT;
        }
    }

    private void eqIfPresent(QueryWrapper<AccountingSubjectDO> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.eq(column, value.trim());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 100;
        }
        return Math.min(limit, 500);
    }
}
