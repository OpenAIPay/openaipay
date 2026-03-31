package cn.openaipay.application.kyc.service.impl;

import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.dto.KycStatusDTO;
import cn.openaipay.application.kyc.service.KycService;
import cn.openaipay.domain.user.model.KycLevel;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * 实名应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class KycServiceImpl implements KycService {

    /** 中国身份证18位号码正则。 */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[0-9X]$");
    /** 身份证前17位加权因子。 */
    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    /** 身份证校验位映射。 */
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    /** 身份证生日解析格式。 */
    private static final DateTimeFormatter ID_CARD_BIRTHDAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /** 用户仓储。 */
    private final UserRepository userRepository;

    public KycServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 查询实名状态。
     */
    @Override
    public KycStatusDTO getStatus(Long userId) {
        return toStatus(mustGetUser(userId));
    }

    /**
     * 提交实名认证。
     */
    @Override
    @Transactional
    public KycStatusDTO submit(SubmitKycCommand command) {
        UserAggregate aggregate = mustGetUser(command.userId());
        String normalizedRealName = normalizeRealName(command.realName());
        String normalizedIdCardNo = normalizeIdCardNo(command.idCardNo());
        LocalDate birthday = parseBirthdayFromIdCard(normalizedIdCardNo);
        aggregate.getProfile().updateIdentityProfile(
                maskRealName(normalizedRealName),
                normalizedIdCardNo,
                resolveGenderFromIdCard(normalizedIdCardNo),
                birthday
        );
        if (aggregate.getAccount().getKycLevel().compareTo(KycLevel.L2) < 0) {
            aggregate.getAccount().updateKycLevel(KycLevel.L2);
        }
        userRepository.save(aggregate);
        return toStatus(aggregate);
    }

    private UserAggregate mustGetUser(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + userId));
    }

    private KycStatusDTO toStatus(UserAggregate aggregate) {
        String kycLevel = aggregate.getAccount().getKycLevel().name();
        return new KycStatusDTO(
                aggregate.getAccount().getUserId(),
                kycLevel,
                "L2".equals(kycLevel) || "L3".equals(kycLevel),
                aggregate.getProfile().getMaskedRealName(),
                maskIdCardNo(aggregate.getProfile().getIdCardNo())
        );
    }

    private String normalizeRealName(String rawRealName) {
        if (rawRealName == null) {
            throw new IllegalArgumentException("realName不能为空");
        }
        String normalized = rawRealName.trim().replace(" ", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("realName不能为空");
        }
        if (normalized.contains("*")) {
            normalized = normalized.replace("*", "");
        }
        if (normalized.length() < 2) {
            throw new IllegalArgumentException("realName格式不正确");
        }
        return normalized;
    }

    private String normalizeIdCardNo(String rawIdCardNo) {
        if (rawIdCardNo == null) {
            throw new IllegalArgumentException("idCardNo不能为空");
        }
        String normalized = rawIdCardNo.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("idCardNo不能为空");
        }
        if (!ID_CARD_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("idCardNo格式不正确");
        }
        if (!isValidIdCardChecksum(normalized)) {
            throw new IllegalArgumentException("idCardNo校验失败");
        }
        LocalDate birthday = parseBirthdayFromIdCard(normalized);
        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("idCardNo出生日期不合法");
        }
        return normalized;
    }

    private boolean isValidIdCardChecksum(String idCardNo) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCardNo.charAt(i) - '0') * ID_CARD_WEIGHTS[i];
        }
        return ID_CARD_CHECK_CODES[sum % 11] == idCardNo.charAt(17);
    }

    private LocalDate parseBirthdayFromIdCard(String idCardNo) {
        try {
            return LocalDate.parse(idCardNo.substring(6, 14), ID_CARD_BIRTHDAY_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("idCardNo出生日期不合法", exception);
        }
    }

    private String resolveGenderFromIdCard(String idCardNo) {
        int genderCode = idCardNo.charAt(16) - '0';
        return genderCode % 2 == 0 ? "FEMALE" : "MALE";
    }

    private String maskRealName(String realName) {
        if (realName.length() <= 1) {
            return realName;
        }
        return realName.substring(0, 1) + "*".repeat(realName.length() - 1);
    }

    private String maskIdCardNo(String idCardNo) {
        if (idCardNo == null || idCardNo.isBlank()) {
            return null;
        }
        if (idCardNo.length() <= 8) {
            return idCardNo;
        }
        return idCardNo.substring(0, 4)
                + "*".repeat(Math.max(0, idCardNo.length() - 8))
                + idCardNo.substring(idCardNo.length() - 4);
    }
}
