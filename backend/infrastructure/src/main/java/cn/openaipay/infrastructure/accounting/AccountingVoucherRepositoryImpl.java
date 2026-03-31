package cn.openaipay.infrastructure.accounting;

import cn.openaipay.domain.accounting.model.AccountingVoucher;
import cn.openaipay.domain.accounting.model.AccountingVoucherQuery;
import cn.openaipay.domain.accounting.model.AccountingVoucherStatus;
import cn.openaipay.domain.accounting.model.VoucherType;
import cn.openaipay.domain.accounting.repository.AccountingVoucherRepository;
import cn.openaipay.infrastructure.accounting.dataobject.AccountingVoucherDO;
import cn.openaipay.infrastructure.accounting.mapper.AccountingVoucherMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会计凭证仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Repository
public class AccountingVoucherRepositoryImpl implements AccountingVoucherRepository {

    /** 核算凭证映射器信息 */
    private final AccountingVoucherMapper accountingVoucherMapper;

    public AccountingVoucherRepositoryImpl(AccountingVoucherMapper accountingVoucherMapper) {
        this.accountingVoucherMapper = accountingVoucherMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public AccountingVoucher save(AccountingVoucher voucher) {
        AccountingVoucherDO entity = voucher.getId() == null
                ? accountingVoucherMapper.findByVoucherNo(voucher.getVoucherNo()).orElse(new AccountingVoucherDO())
                : accountingVoucherMapper.findById(voucher.getId()).orElse(new AccountingVoucherDO());
        fillDO(entity, voucher);
        return toDomain(accountingVoucherMapper.save(entity));
    }

    /**
     * 按凭证单号查找记录。
     */
    @Override
    public Optional<AccountingVoucher> findByVoucherNo(String voucherNo) {
        return accountingVoucherMapper.findByVoucherNo(voucherNo).map(this::toDomain);
    }

    /**
     * 按事件ID查找记录。
     */
    @Override
    public Optional<AccountingVoucher> findByEventId(String eventId) {
        return accountingVoucherMapper.findByEventId(eventId).map(this::toDomain);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public List<AccountingVoucher> list(AccountingVoucherQuery query) {
        QueryWrapper<AccountingVoucherDO> wrapper = new QueryWrapper<>();
        if (query != null) {
            eqIfPresent(wrapper, "voucher_no", query.voucherNo());
            eqIfPresent(wrapper, "source_biz_type", query.sourceBizType());
            eqIfPresent(wrapper, "source_biz_no", query.sourceBizNo());
            eqIfPresent(wrapper, "biz_order_no", query.bizOrderNo());
            eqIfPresent(wrapper, "trade_order_no", query.tradeOrderNo());
            eqIfPresent(wrapper, "pay_order_no", query.payOrderNo());
            if (query.status() != null) {
                wrapper.eq("status", query.status().name());
            }
            wrapper.orderByDesc("posting_date", "updated_at", "id");
            wrapper.last("LIMIT " + normalizeLimit(query.limit()));
        }
        return accountingVoucherMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private AccountingVoucher toDomain(AccountingVoucherDO entity) {
        return new AccountingVoucher(
                entity.getId(),
                entity.getVoucherNo(),
                entity.getBookId(),
                parseVoucherType(entity.getVoucherType()),
                entity.getEventId(),
                entity.getSourceBizType(),
                entity.getSourceBizNo(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getBusinessSceneCode(),
                entity.getBusinessDomainCode(),
                parseStatus(entity.getStatus()),
                resolveVoucherCurrency(entity),
                entity.getTotalDebitAmount(),
                entity.getTotalCreditAmount(),
                List.of(),
                entity.getOccurredAt(),
                entity.getPostingDate(),
                entity.getReversedVoucherNo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(AccountingVoucherDO entity, AccountingVoucher voucher) {
        LocalDateTime now = LocalDateTime.now();
        entity.setVoucherNo(voucher.getVoucherNo());
        entity.setBookId(voucher.getBookId());
        entity.setVoucherType(voucher.getVoucherType().name());
        entity.setEventId(voucher.getEventId());
        entity.setSourceBizType(voucher.getSourceBizType());
        entity.setSourceBizNo(voucher.getSourceBizNo());
        entity.setBizOrderNo(voucher.getBizOrderNo());
        entity.setTradeOrderNo(voucher.getTradeOrderNo());
        entity.setPayOrderNo(voucher.getPayOrderNo());
        entity.setBusinessSceneCode(voucher.getBusinessSceneCode());
        entity.setBusinessDomainCode(voucher.getBusinessDomainCode());
        entity.setStatus(voucher.getStatus().name());
        entity.setTotalDebitAmount(voucher.getTotalDebitAmount());
        entity.setTotalCreditAmount(voucher.getTotalCreditAmount());
        entity.setOccurredAt(voucher.getOccurredAt());
        entity.setPostingDate(voucher.getPostingDate());
        entity.setReversedVoucherNo(voucher.getReversedVoucherNo());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(voucher.getCreatedAt() == null ? now : voucher.getCreatedAt());
        }
        entity.setUpdatedAt(voucher.getUpdatedAt() == null ? now : voucher.getUpdatedAt());
    }

    private AccountingVoucherStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return AccountingVoucherStatus.CREATED;
        }
        try {
            return AccountingVoucherStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return AccountingVoucherStatus.CREATED;
        }
    }

    private VoucherType parseVoucherType(String raw) {
        if (raw == null || raw.isBlank()) {
            return VoucherType.NORMAL;
        }
        try {
            return VoucherType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return VoucherType.NORMAL;
        }
    }

    private void eqIfPresent(QueryWrapper<AccountingVoucherDO> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.eq(column, value.trim());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private CurrencyUnit resolveVoucherCurrency(AccountingVoucherDO entity) {
        Money totalDebitAmount = entity.getTotalDebitAmount();
        if (totalDebitAmount != null) {
            return totalDebitAmount.getCurrencyUnit();
        }
        Money totalCreditAmount = entity.getTotalCreditAmount();
        if (totalCreditAmount != null) {
            return totalCreditAmount.getCurrencyUnit();
        }
        return CurrencyUnit.of("CNY");
    }
}
