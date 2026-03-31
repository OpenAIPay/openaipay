package cn.openaipay.infrastructure.bankcard;

import cn.openaipay.domain.bankcard.model.BankCard;
import cn.openaipay.domain.bankcard.model.BankCardStatus;
import cn.openaipay.domain.bankcard.model.BankCardType;
import cn.openaipay.domain.bankcard.repository.BankCardRepository;
import cn.openaipay.infrastructure.bankcard.dataobject.BankCardDO;
import cn.openaipay.infrastructure.bankcard.mapper.BankCardMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 银行卡仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class BankCardRepositoryImpl implements BankCardRepository {

    /** BankCardMapper组件 */
    private final BankCardMapper bankCardMapper;

    public BankCardRepositoryImpl(BankCardMapper bankCardMapper) {
        this.bankCardMapper = bankCardMapper;
    }

    /**
     * 按单号查找记录。
     */
    @Override
    public Optional<BankCard> findByCardNo(String cardNo) {
        return bankCardMapper.findByCardNo(cardNo).map(this::toDomain);
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public List<BankCard> findByUserId(Long userId) {
        return bankCardMapper.findByUserIdOrderByIsDefaultDescIdAsc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public List<BankCard> findActiveByUserId(Long userId) {
        return bankCardMapper.findByUserIdAndCardStatusOrderByIsDefaultDescIdAsc(userId, BankCardStatus.ACTIVE.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public BankCard save(BankCard bankCard) {
        BankCardDO entity = bankCardMapper.findByCardNo(bankCard.getCardNo()).orElse(new BankCardDO());
        fillDO(entity, bankCard);
        return toDomain(bankCardMapper.save(entity));
    }

    /**
     * 保存ALL信息。
     */
    @Override
    @Transactional
    public List<BankCard> saveAll(List<BankCard> bankCards) {
        List<BankCardDO> entities = new ArrayList<>();
        for (BankCard bankCard : bankCards) {
            BankCardDO entity = bankCardMapper.findByCardNo(bankCard.getCardNo()).orElse(new BankCardDO());
            fillDO(entity, bankCard);
            entities.add(entity);
        }
        return bankCardMapper.saveAll(entities).stream().map(this::toDomain).toList();
    }

    private BankCard toDomain(BankCardDO entity) {
        return new BankCard(
                entity.getId(),
                entity.getCardNo(),
                entity.getUserId(),
                entity.getBankCode(),
                entity.getBankName(),
                BankCardType.from(entity.getCardType()),
                entity.getCardHolderName(),
                entity.getReservedMobile(),
                entity.getPhoneTailNo(),
                BankCardStatus.from(entity.getCardStatus()),
                Boolean.TRUE.equals(entity.getIsDefault()),
                entity.getSingleLimit(),
                entity.getDailyLimit(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(BankCardDO entity, BankCard bankCard) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCardNo(bankCard.getCardNo());
        entity.setUserId(bankCard.getUserId());
        entity.setBankCode(bankCard.getBankCode());
        entity.setBankName(bankCard.getBankName());
        entity.setCardType(bankCard.getCardType().name());
        entity.setCardHolderName(bankCard.getCardHolderName());
        entity.setReservedMobile(bankCard.getReservedMobile());
        entity.setPhoneTailNo(bankCard.getPhoneTailNo());
        entity.setCardStatus(bankCard.getCardStatus().name());
        entity.setIsDefault(bankCard.isDefaultCard());
        entity.setSingleLimit(bankCard.getSingleLimit());
        entity.setDailyLimit(bankCard.getDailyLimit());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(bankCard.getCreatedAt() == null ? now : bankCard.getCreatedAt());
        }
        entity.setUpdatedAt(bankCard.getUpdatedAt() == null ? now : bankCard.getUpdatedAt());
    }
}
