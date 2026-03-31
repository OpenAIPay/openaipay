package cn.openaipay.infrastructure.adminuser;

import cn.openaipay.application.adminuser.dto.AdminFundAccountSnapshotDTO;
import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;
import cn.openaipay.application.adminuser.port.AdminUserManagePort;
import cn.openaipay.domain.shared.number.FundAmount;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditAccountDO;
import cn.openaipay.infrastructure.creditaccount.mapper.CreditAccountMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundAccountDO;
import cn.openaipay.infrastructure.fundaccount.mapper.FundAccountMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserPrivacySettingDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.dataobject.UserSecuritySettingDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserPrivacySettingMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.user.mapper.UserSecuritySettingMapper;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletAccountDO;
import cn.openaipay.infrastructure.walletaccount.mapper.WalletAccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户管理查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class AdminUserManageAdapter implements AdminUserManagePort {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserSecuritySettingMapper userSecuritySettingMapper;
    private final UserPrivacySettingMapper userPrivacySettingMapper;
    private final WalletAccountMapper walletAccountMapper;
    private final CreditAccountMapper creditAccountMapper;
    private final FundAccountMapper fundAccountMapper;

    public AdminUserManageAdapter(UserAccountMapper userAccountMapper,
                                  UserProfileMapper userProfileMapper,
                                  UserSecuritySettingMapper userSecuritySettingMapper,
                                  UserPrivacySettingMapper userPrivacySettingMapper,
                                  WalletAccountMapper walletAccountMapper,
                                  CreditAccountMapper creditAccountMapper,
                                  FundAccountMapper fundAccountMapper) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.userSecuritySettingMapper = userSecuritySettingMapper;
        this.userPrivacySettingMapper = userPrivacySettingMapper;
        this.walletAccountMapper = walletAccountMapper;
        this.creditAccountMapper = creditAccountMapper;
        this.fundAccountMapper = fundAccountMapper;
    }

    @Override
    public AdminUserCenterSummaryDTO summary() {
        long totalUserCount = safeCount(userAccountMapper.selectCount(new QueryWrapper<>()));
        long activeUserCount = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("account_status", "ACTIVE")));
        long frozenUserCount = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("account_status", "FROZEN")));
        long closedUserCount = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("account_status", "CLOSED")));
        long walletActiveUserCount = safeList(walletAccountMapper.selectList(
                new QueryWrapper<WalletAccountDO>()
                        .select("user_id")
                        .eq("account_status", "ACTIVE")
        )).stream()
                .map(WalletAccountDO::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .count();
        long creditNormalUserCount = safeList(creditAccountMapper.selectList(
                new QueryWrapper<CreditAccountDO>()
                        .select("user_id")
                        .eq("account_status", "NORMAL")
        )).stream()
                .map(CreditAccountDO::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .count();

        return new AdminUserCenterSummaryDTO(
                totalUserCount,
                activeUserCount,
                frozenUserCount,
                closedUserCount,
                walletActiveUserCount,
                creditNormalUserCount
        );
    }

    @Override
    public List<AdminUserSummaryDTO> listUsers(String keyword, String accountStatus, String kycLevel, int pageNo, int pageSize) {
        List<UserAccountDO> accounts = queryUserAccounts(keyword, accountStatus, kycLevel, pageNo, pageSize);
        return buildUserSummaryList(accounts);
    }

    @Override
    public AdminUserDetailDTO getUserDetail(Long userId) {
        UserAccountDO account = userAccountMapper.findByUserId(requirePositive(userId, "userId"))
                .orElseThrow(() -> new NoSuchElementException("user not found: " + userId));

        Long normalizedUserId = account.getUserId();
        UserProfileDO profile = userProfileMapper.findByUserId(normalizedUserId).orElse(null);
        UserSecuritySettingDO securitySetting = userSecuritySettingMapper.findByUserId(normalizedUserId).orElse(null);
        UserPrivacySettingDO privacySetting = userPrivacySettingMapper.findByUserId(normalizedUserId).orElse(null);
        WalletAccountDO walletAccount = walletAccountMapper.findByUserId(normalizedUserId).orElse(null);
        CreditAccountDO creditAccount = creditAccountMapper.findByUserId(normalizedUserId).orElse(null);
        List<FundAccountDO> fundAccounts = safeList(fundAccountMapper.findByUserId(normalizedUserId));

        AdminUserSummaryDTO summary = buildUserSummary(
                account,
                profile,
                walletAccount,
                creditAccount,
                fundAccounts.size()
        );

        List<AdminFundAccountSnapshotDTO> fundSnapshots = fundAccounts.stream()
                .map(this::toFundSnapshot)
                .toList();

        return new AdminUserDetailDTO(
                summary,
                profile == null ? null : profile.getAvatarUrl(),
                profile == null ? null : profile.getCountryCode(),
                profile == null ? null : profile.getMaskedRealName(),
                profile == null ? null : profile.getIdCardNo(),
                profile == null ? null : profile.getGender(),
                profile == null ? null : profile.getRegion(),
                profile == null ? null : profile.getBirthday(),
                securitySetting == null ? null : securitySetting.getBiometricEnabled(),
                securitySetting == null ? null : securitySetting.getTwoFactorMode(),
                securitySetting == null ? null : securitySetting.getRiskLevel(),
                securitySetting == null ? null : securitySetting.getDeviceLockEnabled(),
                securitySetting == null ? null : securitySetting.getPrivacyModeEnabled(),
                privacySetting == null ? null : privacySetting.getAllowSearchByMobile(),
                privacySetting == null ? null : privacySetting.getAllowSearchByAipayUid(),
                privacySetting == null ? null : privacySetting.getHideRealName(),
                privacySetting == null ? null : privacySetting.getPersonalizedRecommendationEnabled(),
                creditAccount == null ? null : creditAccount.getInterestBalance(),
                creditAccount == null ? null : creditAccount.getFineBalance(),
                sumFundAmount(fundAccounts, FundAccountDO::getAvailableShare),
                sumFundAmount(fundAccounts, FundAccountDO::getAccumulatedIncome),
                fundSnapshots,
                profile == null ? null : profile.getUpdatedAt()
        );
    }

    @Override
    public AdminUserSummaryDTO changeUserStatus(Long userId, String status) {
        Long normalizedUserId = requirePositive(userId, "userId");
        UserAccountDO account = userAccountMapper.findByUserId(normalizedUserId)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + normalizedUserId));

        UserAccountDO updated = new UserAccountDO();
        updated.setAccountStatus(status);
        int affected = userAccountMapper.update(updated, new QueryWrapper<UserAccountDO>().eq("user_id", normalizedUserId));
        if (affected <= 0) {
            throw new IllegalStateException("failed to update user status: " + normalizedUserId);
        }

        UserAccountDO refreshed = userAccountMapper.findByUserId(normalizedUserId).orElse(account);
        UserProfileDO profile = userProfileMapper.findByUserId(normalizedUserId).orElse(null);
        WalletAccountDO wallet = walletAccountMapper.findByUserId(normalizedUserId).orElse(null);
        CreditAccountDO credit = creditAccountMapper.findByUserId(normalizedUserId).orElse(null);
        int fundCount = safeList(fundAccountMapper.findByUserId(normalizedUserId)).size();
        return buildUserSummary(refreshed, profile, wallet, credit, fundCount);
    }

    private List<UserAccountDO> queryUserAccounts(String keyword, String accountStatus, String kycLevel, int pageNo, int pageSize) {
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();

        if (accountStatus != null) {
            wrapper.eq("account_status", accountStatus);
        }
        if (kycLevel != null) {
            wrapper.eq("kyc_level", kycLevel);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword != null) {
            Long keywordUserId = parsePositiveLongOrNull(normalizedKeyword);
            Set<Long> profileMatchedUserIds = queryProfileMatchedUserIds(normalizedKeyword);

            wrapper.and(query -> {
                if (keywordUserId != null) {
                    query.eq("user_id", keywordUserId).or();
                }
                query.like("aipay_uid", normalizedKeyword)
                        .or()
                        .like("login_id", normalizedKeyword);
                if (!profileMatchedUserIds.isEmpty()) {
                    query.or().in("user_id", profileMatchedUserIds);
                }
            });
        }

        wrapper.orderByDesc("updated_at");
        long offset = (long) Math.max(0, pageNo - 1) * Math.max(1, pageSize);
        wrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
        return safeList(userAccountMapper.selectList(wrapper));
    }

    private Set<Long> queryProfileMatchedUserIds(String keyword) {
        if (!hasText(keyword)) {
            return Set.of();
        }
        QueryWrapper<UserProfileDO> profileWrapper = new QueryWrapper<>();
        profileWrapper.select("user_id");
        profileWrapper.and(query -> query
                .like("nickname", keyword)
                .or()
                .like("mobile", keyword)
                .or()
                .like("masked_real_name", keyword));
        profileWrapper.last("LIMIT 200");
        return safeList(userProfileMapper.selectList(profileWrapper)).stream()
                .map(UserProfileDO::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
    }

    private List<AdminUserSummaryDTO> buildUserSummaryList(List<UserAccountDO> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = accounts.stream()
                .map(UserAccountDO::getUserId)
                .filter(id -> id != null && id > 0)
                .toList();

        Map<Long, UserProfileDO> profileMap = loadProfileMap(userIds);
        Map<Long, WalletAccountDO> walletMap = loadWalletMap(userIds);
        Map<Long, CreditAccountDO> creditMap = loadCreditMap(userIds);
        Map<Long, List<FundAccountDO>> fundMap = loadFundMap(userIds);

        return accounts.stream()
                .map(account -> {
                    Long userId = account.getUserId();
                    List<FundAccountDO> fundList = fundMap.getOrDefault(userId, List.of());
                    return buildUserSummary(
                            account,
                            profileMap.get(userId),
                            walletMap.get(userId),
                            creditMap.get(userId),
                            fundList.size()
                    );
                })
                .toList();
    }

    private AdminUserSummaryDTO buildUserSummary(UserAccountDO account,
                                                 UserProfileDO profile,
                                                 WalletAccountDO wallet,
                                                 CreditAccountDO credit,
                                                 int fundAccountCount) {
        return new AdminUserSummaryDTO(
                toUserIdText(account.getUserId()),
                profile == null ? null : profile.getMaskedRealName(),
                account.getAipayUid(),
                account.getLoginId(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getMobile(),
                account.getAccountStatus(),
                account.getKycLevel(),
                account.getLoginPasswordSet(),
                account.getPayPasswordSet(),
                wallet == null ? null : wallet.getAccountStatus(),
                wallet == null ? null : toMoney(wallet.getAvailableBalance(), wallet.getCurrencyCode()),
                wallet == null ? null : toMoney(wallet.getReservedBalance(), wallet.getCurrencyCode()),
                credit == null ? null : credit.getAccountStatus(),
                credit == null ? null : credit.getTotalLimit(),
                credit == null ? null : credit.getPrincipalBalance(),
                credit == null ? null : credit.getPrincipalBalance(),
                fundAccountCount,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private AdminFundAccountSnapshotDTO toFundSnapshot(FundAccountDO entity) {
        return new AdminFundAccountSnapshotDTO(
                entity.getFundCode(),
                entity.getCurrencyCode(),
                entity.getAvailableShare(),
                entity.getFrozenShare(),
                entity.getPendingSubscribeAmount(),
                entity.getPendingRedeemShare(),
                entity.getAccumulatedIncome(),
                entity.getYesterdayIncome(),
                entity.getLatestNav(),
                entity.getAccountStatus(),
                entity.getUpdatedAt()
        );
    }

    private Map<Long, UserProfileDO> loadProfileMap(Collection<Long> userIds) {
        return userProfileMapper.findByUserIds(userIds).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (first, ignored) -> first));
    }

    private Map<Long, WalletAccountDO> loadWalletMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<WalletAccountDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return safeList(walletAccountMapper.selectList(wrapper)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(WalletAccountDO::getUserId, Function.identity(), (first, ignored) -> first));
    }

    private Map<Long, CreditAccountDO> loadCreditMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<CreditAccountDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return safeList(creditAccountMapper.selectList(wrapper)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(CreditAccountDO::getUserId, Function.identity(), (first, ignored) -> first));
    }

    private Map<Long, List<FundAccountDO>> loadFundMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return safeList(fundAccountMapper.selectList(wrapper)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.groupingBy(FundAccountDO::getUserId));
    }

    private FundAmount sumFundAmount(List<FundAccountDO> fundAccounts, Function<FundAccountDO, FundAmount> extractor) {
        if (fundAccounts == null || fundAccounts.isEmpty()) {
            return FundAmount.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (FundAccountDO account : fundAccounts) {
            FundAmount amount = extractor.apply(account);
            if (amount != null) {
                total = total.add(amount.toBigDecimal());
            }
        }
        return new FundAmount(total);
    }

    private String normalizeKeyword(String rawKeyword) {
        if (!hasText(rawKeyword)) {
            return null;
        }
        return rawKeyword.trim();
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }
        CurrencyUnit unit = resolveCurrencyUnit(currencyCode);
        BigDecimal normalizedAmount = normalizeMoneyScale(amount, unit);
        return Money.of(unit, normalizedAmount);
    }

    private CurrencyUnit resolveCurrencyUnit(String currencyCode) {
        String normalizedCurrency = hasText(currencyCode) ? currencyCode.trim().toUpperCase(Locale.ROOT) : "CNY";
        try {
            return CurrencyUnit.of(normalizedCurrency);
        } catch (IllegalArgumentException ex) {
            return CurrencyUnit.of("CNY");
        }
    }

    private BigDecimal normalizeMoneyScale(BigDecimal amount, CurrencyUnit unit) {
        int decimalPlaces = unit.getDecimalPlaces();
        if (decimalPlaces < 0 || amount.scale() <= decimalPlaces) {
            return amount;
        }
        return amount.setScale(decimalPlaces, RoundingMode.DOWN);
    }

    private Long parsePositiveLongOrNull(String text) {
        if (!hasText(text) || !text.matches("^\\d+$")) {
            return null;
        }
        try {
            long value = Long.parseLong(text);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private String toUserIdText(Long userId) {
        return userId == null ? null : String.valueOf(userId);
    }

    private long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    private <T> List<T> safeList(List<T> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values;
    }
}
