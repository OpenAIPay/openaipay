package cn.openaipay.application.bankcard.service.impl;

import cn.openaipay.application.bankcard.command.BindBankCardCommand;
import cn.openaipay.application.bankcard.command.SetDefaultBankCardCommand;
import cn.openaipay.application.bankcard.dto.BankCardDTO;
import cn.openaipay.application.bankcard.service.BankCardService;
import cn.openaipay.domain.bankcard.model.BankCard;
import cn.openaipay.domain.bankcard.model.BankCardStatus;
import cn.openaipay.domain.bankcard.model.BankCardType;
import cn.openaipay.domain.bankcard.repository.BankCardRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 银行卡应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class BankCardServiceImpl implements BankCardService {

    /** BankCardRepository组件 */
    private final BankCardRepository bankCardRepository;
    /** UserRepository组件 */
    private final UserRepository userRepository;

    public BankCardServiceImpl(BankCardRepository bankCardRepository, UserRepository userRepository) {
        this.bankCardRepository = bankCardRepository;
        this.userRepository = userRepository;
    }

    /**
     * 处理银行信息。
     */
    @Override
    @Transactional
    public BankCardDTO bindBankCard(BindBankCardCommand command) {
        Long userId = requirePositive(command.userId(), "userId");
        String cardNo = normalizeCardNo(command.cardNo());
        if (bankCardRepository.findByCardNo(cardNo).isPresent()) {
            throw new IllegalArgumentException("bank card already exists: " + cardNo);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean defaultCard = Boolean.TRUE.equals(command.defaultCard());
        if (defaultCard) {
            clearDefaultCards(userId, now);
        }

        BankCard bankCard = BankCard.bind(
                cardNo,
                userId,
                command.bankCode(),
                command.bankName(),
                BankCardType.from(command.cardType()),
                resolveCardHolderName(userId, command.cardHolderName()),
                command.reservedMobile(),
                command.phoneTailNo(),
                defaultCard,
                command.singleLimit(),
                command.dailyLimit(),
                now
        );
        return toDTO(bankCardRepository.save(bankCard));
    }

    /**
     * 处理SET银行信息。
     */
    @Override
    @Transactional
    public BankCardDTO setDefaultBankCard(SetDefaultBankCardCommand command) {
        Long userId = requirePositive(command.userId(), "userId");
        String cardNo = normalizeCardNo(command.cardNo());

        List<BankCard> userCards = bankCardRepository.findByUserId(userId);
        if (userCards.isEmpty()) {
            throw new NoSuchElementException("bank card not found for userId: " + userId);
        }

        LocalDateTime now = LocalDateTime.now();
        BankCard target = null;
        for (BankCard card : userCards) {
            if (card.getCardNo().equals(cardNo)) {
                if (card.getCardStatus() != BankCardStatus.ACTIVE) {
                    throw new IllegalStateException("default card must be ACTIVE status");
                }
                card.markDefault(true, now);
                target = card;
            } else if (card.isDefaultCard()) {
                card.markDefault(false, now);
            }
        }

        if (target == null) {
            throw new NoSuchElementException("bank card not found: " + cardNo);
        }

        bankCardRepository.saveAll(userCards);
        return toDTO(target);
    }

    /**
     * 查询用户银行信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<BankCardDTO> listUserBankCards(Long userId) {
        Long normalizedUserId = requirePositive(userId, "userId");
        return bankCardRepository.findByUserId(normalizedUserId).stream().map(this::toDTO).toList();
    }

    /**
     * 查询用户银行信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<BankCardDTO> listUserActiveBankCards(Long userId) {
        Long normalizedUserId = requirePositive(userId, "userId");
        return bankCardRepository.findActiveByUserId(normalizedUserId).stream().map(this::toDTO).toList();
    }

    private void clearDefaultCards(Long userId, LocalDateTime now) {
        List<BankCard> userCards = bankCardRepository.findByUserId(userId);
        boolean changed = false;
        for (BankCard userCard : userCards) {
            if (userCard.isDefaultCard()) {
                userCard.markDefault(false, now);
                changed = true;
            }
        }
        if (changed) {
            bankCardRepository.saveAll(userCards);
        }
    }

    private BankCardDTO toDTO(BankCard bankCard) {
        String normalizedBankName = normalizeBankName(bankCard.getBankCode(), bankCard.getBankName());
        return new BankCardDTO(
                bankCard.getCardNo(),
                maskCardNo(bankCard.getCardNo()),
                bankCard.getUserId(),
                bankCard.getBankCode(),
                normalizedBankName,
                bankCard.getCardType().name(),
                bankCard.getCardHolderName(),
                bankCard.getReservedMobile(),
                bankCard.getPhoneTailNo(),
                bankCard.getCardStatus().name(),
                bankCard.isDefaultCard(),
                bankCard.getSingleLimit(),
                bankCard.getDailyLimit()
        );
    }

    private String maskCardNo(String cardNo) {
        if (cardNo == null || cardNo.isBlank()) {
            return cardNo;
        }
        if (cardNo.length() <= 4) {
            return cardNo;
        }
        String tail = cardNo.substring(cardNo.length() - 4);
        if (cardNo.length() <= 8) {
            return "****" + tail;
        }
        String head = cardNo.substring(0, 4);
        return head + " **** **** " + tail;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeCardNo(String cardNo) {
        if (cardNo == null || cardNo.isBlank()) {
            throw new IllegalArgumentException("cardNo must not be blank");
        }
        String normalized = cardNo.trim().replace(" ", "");
        if (normalized.length() < 12 || normalized.length() > 32) {
            throw new IllegalArgumentException("cardNo length must be between 12 and 32");
        }
        if (!normalized.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("cardNo must contain digits only");
        }
        return normalized;
    }

    private String resolveCardHolderName(Long userId, String cardHolderName) {
        String normalized = normalizeOptional(cardHolderName);
        if (normalized != null) {
            return normalized;
        }

        return userRepository.findByUserId(userId)
                .map(UserAggregate::getProfile)
                .map(this::resolveCardHolderNameFromProfile)
                .orElse("银行卡用户");
    }

    private String resolveCardHolderNameFromProfile(UserProfile profile) {
        if (profile == null) {
            return "银行卡用户";
        }

        String maskedRealName = normalizeOptional(profile.getMaskedRealName());
        if (maskedRealName != null) {
            return maskedRealName;
        }

        String nickname = normalizeOptional(profile.getNickname());
        if (nickname != null) {
            return nickname;
        }

        return "银行卡用户";
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeBankName(String bankCode, String bankName) {
        String normalizedCode = bankCode == null ? "" : bankCode.trim().toUpperCase();
        return switch (normalizedCode) {
            case "ICBC" -> "中国工商银行";
            case "ABC" -> "中国农业银行";
            case "CCB" -> "中国建设银行";
            case "BOC" -> "中国银行";
            case "PSBC" -> "中国邮政储蓄银行";
            case "SPDB" -> "上海浦东发展银行";
            case "CMBC" -> "中国民生银行";
            case "CEB" -> "中国光大银行";
            default -> bankName;
        };
    }
}
