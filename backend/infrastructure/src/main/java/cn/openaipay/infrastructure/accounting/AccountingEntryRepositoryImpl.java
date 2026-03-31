package cn.openaipay.infrastructure.accounting;

import cn.openaipay.domain.accounting.model.AccountingEntry;
import cn.openaipay.domain.accounting.model.AccountingEntryQuery;
import cn.openaipay.domain.accounting.model.DebitCreditFlag;
import cn.openaipay.domain.accounting.repository.AccountingEntryRepository;
import cn.openaipay.infrastructure.accounting.dataobject.AccountingEntryDO;
import cn.openaipay.infrastructure.accounting.mapper.AccountingEntryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会计分录仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Repository
public class AccountingEntryRepositoryImpl implements AccountingEntryRepository {

    /** 分录信息 */
    private final AccountingEntryMapper accountingEntryMapper;

    public AccountingEntryRepositoryImpl(AccountingEntryMapper accountingEntryMapper) {
        this.accountingEntryMapper = accountingEntryMapper;
    }

    /**
     * 保存ALL信息。
     */
    @Override
    @Transactional
    public List<AccountingEntry> saveAll(List<AccountingEntry> entries) {
        List<AccountingEntry> saved = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return saved;
        }
        for (AccountingEntry entry : entries) {
            AccountingEntryDO entity = new AccountingEntryDO();
            fillDO(entity, entry);
            saved.add(toDomain(accountingEntryMapper.save(entity)));
        }
        return saved;
    }

    /**
     * 查询业务数据。
     */
    @Override
    public List<AccountingEntry> list(AccountingEntryQuery query) {
        QueryWrapper<AccountingEntryDO> wrapper = new QueryWrapper<>();
        if (query != null) {
            eqIfPresent(wrapper, "voucher_no", query.voucherNo());
            eqIfPresent(wrapper, "subject_code", query.subjectCode());
            eqIfPresent(wrapper, "owner_type", query.ownerType());
            if (query.ownerId() != null) {
                wrapper.eq("owner_id", query.ownerId());
            }
            eqIfPresent(wrapper, "biz_order_no", query.bizOrderNo());
            eqIfPresent(wrapper, "trade_order_no", query.tradeOrderNo());
            eqIfPresent(wrapper, "pay_order_no", query.payOrderNo());
            eqIfPresent(wrapper, "source_biz_type", query.sourceBizType());
            eqIfPresent(wrapper, "source_biz_no", query.sourceBizNo());
            wrapper.orderByAsc("voucher_no", "line_no", "id");
            wrapper.last("LIMIT " + normalizeLimit(query.limit()));
        }
        return accountingEntryMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private AccountingEntry toDomain(AccountingEntryDO entity) {
        return new AccountingEntry(
                entity.getId(),
                entity.getVoucherNo(),
                entity.getLineNo(),
                entity.getSubjectCode(),
                parseFlag(entity.getDcFlag()),
                entity.getAmount(),
                entity.getOwnerType(),
                entity.getOwnerId(),
                entity.getAccountDomain(),
                entity.getAccountType(),
                entity.getAccountNo(),
                entity.getBizRole(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getSourceBizType(),
                entity.getSourceBizNo(),
                entity.getReferenceNo(),
                entity.getEntryMemo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(AccountingEntryDO entity, AccountingEntry entry) {
        LocalDateTime now = LocalDateTime.now();
        entity.setVoucherNo(entry.getVoucherNo());
        entity.setLineNo(entry.getLineNo());
        entity.setSubjectCode(entry.getSubjectCode());
        entity.setDcFlag(entry.getDcFlag().name());
        entity.setAmount(entry.getAmount());
        entity.setOwnerType(entry.getOwnerType());
        entity.setOwnerId(entry.getOwnerId());
        entity.setAccountDomain(entry.getAccountDomain());
        entity.setAccountType(entry.getAccountType());
        entity.setAccountNo(entry.getAccountNo());
        entity.setBizRole(entry.getBizRole());
        entity.setBizOrderNo(entry.getBizOrderNo());
        entity.setTradeOrderNo(entry.getTradeOrderNo());
        entity.setPayOrderNo(entry.getPayOrderNo());
        entity.setSourceBizType(entry.getSourceBizType());
        entity.setSourceBizNo(entry.getSourceBizNo());
        entity.setReferenceNo(entry.getReferenceNo());
        entity.setEntryMemo(entry.getEntryMemo());
        entity.setCreatedAt(entry.getCreatedAt() == null ? now : entry.getCreatedAt());
        entity.setUpdatedAt(entry.getUpdatedAt() == null ? now : entry.getUpdatedAt());
    }

    private DebitCreditFlag parseFlag(String raw) {
        if (raw == null || raw.isBlank()) {
            return DebitCreditFlag.DEBIT;
        }
        try {
            return DebitCreditFlag.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return DebitCreditFlag.DEBIT;
        }
    }

    private void eqIfPresent(QueryWrapper<AccountingEntryDO> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.eq(column, value.trim());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 200;
        }
        return Math.min(limit, 500);
    }
}
