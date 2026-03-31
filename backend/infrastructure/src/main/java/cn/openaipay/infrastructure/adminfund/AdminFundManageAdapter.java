package cn.openaipay.infrastructure.adminfund;

import cn.openaipay.application.adminfund.dto.AdminBankCardRowDTO;
import cn.openaipay.application.adminfund.dto.AdminCreditAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import cn.openaipay.application.adminfund.port.AdminFundManagePort;
import cn.openaipay.infrastructure.bankcard.dataobject.BankCardDO;
import cn.openaipay.infrastructure.bankcard.mapper.BankCardMapper;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditAccountDO;
import cn.openaipay.infrastructure.creditaccount.mapper.CreditAccountMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundAccountDO;
import cn.openaipay.infrastructure.fundaccount.mapper.FundAccountMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletAccountDO;
import cn.openaipay.infrastructure.walletaccount.mapper.WalletAccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * 资金中心查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class AdminFundManageAdapter implements AdminFundManagePort {

    private static final String CREDIT_PREFIX = "CA";
    private static final String LOAN_PREFIX = "LA";

    private final WalletAccountMapper walletAccountMapper;
    private final FundAccountMapper fundAccountMapper;
    private final CreditAccountMapper creditAccountMapper;
    private final BankCardMapper bankCardMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;

    public AdminFundManageAdapter(WalletAccountMapper walletAccountMapper,
                                  FundAccountMapper fundAccountMapper,
                                  CreditAccountMapper creditAccountMapper,
                                  BankCardMapper bankCardMapper,
                                  UserAccountMapper userAccountMapper,
                                  UserProfileMapper userProfileMapper) {
        this.walletAccountMapper = walletAccountMapper;
        this.fundAccountMapper = fundAccountMapper;
        this.creditAccountMapper = creditAccountMapper;
        this.bankCardMapper = bankCardMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public AdminFundOverviewDTO overview() {
        long walletAccountCount = safeCount(walletAccountMapper.selectCount(new QueryWrapper<>()));
        long fundAccountCount = safeCount(fundAccountMapper.selectCount(new QueryWrapper<>()));
        long creditAccountCount = safeCount(creditAccountMapper.selectCount(
                new QueryWrapper<CreditAccountDO>().likeRight("account_no", CREDIT_PREFIX)
        ));
        long loanAccountCount = safeCount(creditAccountMapper.selectCount(
                new QueryWrapper<CreditAccountDO>().likeRight("account_no", LOAN_PREFIX)
        ));
        long bankCardCount = safeCount(bankCardMapper.selectCount(new QueryWrapper<>()));
        return new AdminFundOverviewDTO(
                walletAccountCount,
                fundAccountCount,
                creditAccountCount,
                loanAccountCount,
                bankCardCount
        );
    }

    @Override
    public List<AdminWalletAccountRowDTO> listWalletAccounts(Long userId, String accountStatus, int pageNo, int pageSize) {
        QueryWrapper<WalletAccountDO> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (accountStatus != null) {
            wrapper.eq("account_status", accountStatus);
        }
        wrapper.orderByDesc("updated_at", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<WalletAccountDO> rows = safeList(walletAccountMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(rows.stream()
                .map(WalletAccountDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return rows.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    return new AdminWalletAccountRowDTO(
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            userDigest == null ? null : userDigest.aipayUid(),
                            item.getCurrencyCode(),
                            toMoney(item.getAvailableBalance(), item.getCurrencyCode()),
                            toMoney(item.getReservedBalance(), item.getCurrencyCode()),
                            item.getAccountStatus(),
                            item.getCreatedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<AdminFundAccountRowDTO> listFundAccounts(Long userId, String fundCode, String accountStatus, int pageNo, int pageSize) {
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (fundCode != null) {
            wrapper.eq("fund_code", fundCode);
        }
        if (accountStatus != null) {
            wrapper.eq("account_status", accountStatus);
        }
        wrapper.orderByDesc("updated_at", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<FundAccountDO> rows = safeList(fundAccountMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(rows.stream()
                .map(FundAccountDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return rows.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    return new AdminFundAccountRowDTO(
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            userDigest == null ? null : userDigest.aipayUid(),
                            item.getFundCode(),
                            item.getCurrencyCode(),
                            item.getAvailableShare(),
                            item.getFrozenShare(),
                            item.getPendingSubscribeAmount(),
                            item.getPendingRedeemShare(),
                            item.getAccumulatedIncome(),
                            item.getYesterdayIncome(),
                            item.getLatestNav(),
                            item.getAccountStatus(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<AdminCreditAccountRowDTO> listCreditAccounts(Long userId, String accountStatus, String payStatus, int pageNo, int pageSize) {
        return queryCreditAccounts(CREDIT_PREFIX, "AICREDIT", userId, accountStatus, payStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminCreditAccountRowDTO> listLoanAccounts(Long userId, String accountStatus, String payStatus, int pageNo, int pageSize) {
        return queryCreditAccounts(LOAN_PREFIX, "LOAN_ACCOUNT", userId, accountStatus, payStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminBankCardRowDTO> listBankCards(Long userId, String cardStatus, String bankCode, int pageNo, int pageSize) {
        QueryWrapper<BankCardDO> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (cardStatus != null) {
            wrapper.eq("card_status", cardStatus);
        }
        if (bankCode != null) {
            wrapper.eq("bank_code", bankCode);
        }
        wrapper.orderByDesc("is_default", "updated_at", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<BankCardDO> rows = safeList(bankCardMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(rows.stream()
                .map(BankCardDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return rows.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    return new AdminBankCardRowDTO(
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            userDigest == null ? null : userDigest.aipayUid(),
                            item.getCardNo(),
                            item.getBankCode(),
                            item.getBankName(),
                            item.getCardType(),
                            item.getCardHolderName(),
                            item.getReservedMobile(),
                            item.getPhoneTailNo(),
                            item.getCardStatus(),
                            item.getIsDefault(),
                            item.getSingleLimit(),
                            item.getDailyLimit(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    private List<AdminCreditAccountRowDTO> queryCreditAccounts(String prefix,
                                                               String productCode,
                                                               Long userId,
                                                               String accountStatus,
                                                               String payStatus,
                                                               int pageNo,
                                                               int pageSize) {
        QueryWrapper<CreditAccountDO> wrapper = new QueryWrapper<>();
        wrapper.likeRight("account_no", prefix);
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (accountStatus != null) {
            wrapper.eq("account_status", accountStatus);
        }
        if (payStatus != null) {
            wrapper.eq("pay_status", payStatus);
        }
        wrapper.orderByDesc("updated_at", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<CreditAccountDO> rows = safeList(creditAccountMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(rows.stream()
                .map(CreditAccountDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return rows.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    Money availableLimit = item.getTotalLimit() == null || item.getPrincipalBalance() == null
                            ? null
                            : item.getTotalLimit().minus(item.getPrincipalBalance());
                    return new AdminCreditAccountRowDTO(
                            productCode,
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            userDigest == null ? null : userDigest.aipayUid(),
                            item.getAccountNo(),
                            item.getTotalLimit(),
                            availableLimit,
                            item.getPrincipalBalance(),
                            item.getPrincipalUnreachAmount(),
                            item.getOverduePrincipalBalance(),
                            item.getOverduePrincipalUnreachAmount(),
                            item.getInterestBalance(),
                            item.getFineBalance(),
                            item.getAccountStatus(),
                            item.getPayStatus(),
                            item.getRepayDayOfMonth(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    private Map<Long, UserDigest> loadUserDigestMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserAccountDO> accountMap = safeList(userAccountMapper.findByUserIds(userIds)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (left, right) -> left));
        Map<Long, UserProfileDO> profileMap = safeList(userProfileMapper.findByUserIds(userIds)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (left, right) -> left));
        Map<Long, UserDigest> result = new LinkedHashMap<>();
        userIds.forEach(userId -> {
            UserAccountDO account = accountMap.get(userId);
            UserProfileDO profile = profileMap.get(userId);
            result.put(userId, new UserDigest(
                    userId,
                    profile != null && hasText(profile.getNickname()) ? profile.getNickname() : account == null ? null : account.getAipayUid(),
                    account == null ? null : account.getAipayUid()
            ));
        });
        return result;
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }
        CurrencyUnit unit = CurrencyUnit.of(currencyCode == null || currencyCode.isBlank() ? "CNY" : currencyCode);
        int decimalPlaces = unit.getDecimalPlaces();
        BigDecimal normalized = decimalPlaces < 0 || amount.scale() <= decimalPlaces
                ? amount
                : amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return Money.of(unit, normalized);
    }

    private String buildPageClause(int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(1, pageNo);
        int normalizedPageSize = Math.max(1, pageSize);
        long offset = (long) (normalizedPageNo - 1) * normalizedPageSize;
        return "LIMIT " + normalizedPageSize + " OFFSET " + offset;
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private record UserDigest(
            Long userId,
            String displayName,
            String aipayUid
    ) {
    }
}
