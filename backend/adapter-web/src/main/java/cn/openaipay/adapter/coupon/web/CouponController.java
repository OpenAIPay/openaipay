package cn.openaipay.adapter.coupon.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.coupon.command.IssueCouponCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.coupon.model.CouponIssue;
import cn.openaipay.domain.coupon.model.CouponIssueStatus;
import cn.openaipay.domain.coupon.model.CouponTemplate;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;
import cn.openaipay.domain.coupon.repository.CouponRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C端优惠券能力控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    /** 手机充值奖励模板编码。 */
    private static final String MOBILE_TOP_UP_REWARD_TEMPLATE_CODE = "MOBILE_TOPUP_REWARD_2_5";
    /** 首页横幅话费红包每日领取上限。 */
    private static final int MOBILE_TOP_UP_REWARD_DAILY_CLAIM_LIMIT = 5;
    /** 手机充值奖励领取渠道。 */
    private static final String MOBILE_TOP_UP_REWARD_CLAIM_CHANNEL = "HOME_BANNER_TOPUP_RED_PACKET";
    /** 手机充值奖励发放操作人。 */
    private static final String MOBILE_TOP_UP_REWARD_OPERATOR = "home-banner";
    /** 演示账号初始化发券渠道。 */
    private static final String DEMO_AUTO_LOGIN_CLAIM_CHANNEL = "DEMO_AUTO_LOGIN";
    /** 演示红包默认发放数量。 */
    private static final int DEMO_AUTO_LOGIN_DEFAULT_COUNT = 10;
    /** 演示红包最大发放数量。 */
    private static final int DEMO_AUTO_LOGIN_MAX_COUNT = 20;
    /** 演示红包金额最小值。 */
    private static final BigDecimal DEMO_REWARD_MIN_AMOUNT = new BigDecimal("2.00");
    /** 演示红包金额最大值。 */
    private static final BigDecimal DEMO_REWARD_MAX_AMOUNT = new BigDecimal("5.00");
    /** 演示红包有效期（年）。 */
    private static final int DEMO_REWARD_VALID_YEARS = 1;
    /** 演示红包币种。 */
    private static final CurrencyUnit DEMO_REWARD_CURRENCY = CurrencyUnit.of("CNY");

    /** 优惠券门面。 */
    private final CouponFacade couponFacade;
    /** 优惠券仓储。 */
    private final CouponRepository couponRepository;
    /** 爱付号生成器。 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** 业务类型注册表。 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;

    public CouponController(CouponFacade couponFacade,
                            CouponRepository couponRepository,
                            AiPayIdGenerator aiPayIdGenerator,
                            AiPayBizTypeRegistry aiPayBizTypeRegistry) {
        this.couponFacade = couponFacade;
        this.couponRepository = couponRepository;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
    }

    /**
     * 领取手机充值红包（首页第三横幅）。
     */
    @PostMapping("/mobile-topup-reward/claim")
    public ApiResponse<ClaimMobileTopUpRewardResponse> claimMobileTopUpReward(
            @Valid @RequestBody ClaimMobileTopUpRewardRequest request) {
        CouponTemplate template = couponRepository.findTemplateByCode(MOBILE_TOP_UP_REWARD_TEMPLATE_CODE)
                .orElseThrow(() -> new IllegalStateException("手机充值红包模板未配置"));
        if (template.getStatus() != CouponTemplateStatus.ACTIVE || template.getTemplateId() == null) {
            throw new IllegalStateException("手机充值红包活动未开启");
        }

        String businessNo = resolveBusinessNo(request.businessNo(), request.userId());
        CouponIssueDTO existingIssue = couponRepository.findIssueByTemplateUserAndBusinessNo(
                template.getTemplateId(),
                request.userId(),
                businessNo
        ).map(this::toCouponIssueDTO).orElse(null);

        if (existingIssue != null) {
            int todayClaimedCount = countTodayBannerClaims(template.getTemplateId(), request.userId(), MOBILE_TOP_UP_REWARD_CLAIM_CHANNEL);
            return ApiResponse.success(toClaimResponse(existingIssue, todayClaimedCount));
        }

        int todayClaimedCount = countTodayBannerClaims(template.getTemplateId(), request.userId(), MOBILE_TOP_UP_REWARD_CLAIM_CHANNEL);
        if (todayClaimedCount >= MOBILE_TOP_UP_REWARD_DAILY_CLAIM_LIMIT) {
            throw new IllegalArgumentException(
                    "话费充值红包一天最多领取五次，您已领取" + todayClaimedCount + "次"
            );
        }

        CouponIssueDTO issued = couponFacade.issueCoupon(new IssueCouponCommand(
                template.getTemplateId(),
                request.userId(),
                MOBILE_TOP_UP_REWARD_CLAIM_CHANNEL,
                businessNo,
                MOBILE_TOP_UP_REWARD_OPERATOR
        ));
        int todayClaimedCountAfterIssue = todayClaimedCount + 1;

        return ApiResponse.success(toClaimResponse(issued, todayClaimedCountAfterIssue));
    }

    /**
     * 查询当前用户可用的话费红包列表（单选时默认金额最大的券）。
     */
    @GetMapping("/mobile-topup-reward/available")
    public ApiResponse<List<MobileTopUpRewardIssueResponse>> listAvailableMobileTopUpRewards(
            @RequestParam("userId") @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId) {
        CouponTemplate template = couponRepository.findTemplateByCode(MOBILE_TOP_UP_REWARD_TEMPLATE_CODE)
                .orElse(null);
        if (template == null || template.getTemplateId() == null) {
            return ApiResponse.success(List.of());
        }

        LocalDateTime now = LocalDateTime.now();
        List<MobileTopUpRewardIssueResponse> rewards = couponFacade
                .listIssues(template.getTemplateId(), userId, CouponIssueStatus.UNUSED.name(), 200)
                .stream()
                .filter(issue -> issue.expireAt() == null || !issue.expireAt().isBefore(now))
                .sorted(Comparator
                        .comparing(CouponController::couponAmountValue, Comparator.reverseOrder())
                        .thenComparing(issue -> issue.expireAt() == null ? LocalDateTime.MAX : issue.expireAt())
                        .thenComparing(CouponIssueDTO::claimedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(issue -> new MobileTopUpRewardIssueResponse(
                        issue.couponNo(),
                        issue.couponAmount().getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        issue.couponAmount().getCurrencyUnit().getCode(),
                        issue.expireAt(),
                        issue.expireAt() == null ? null : issue.expireAt().toLocalDate(),
                        issue.status()
                ))
                .toList();
        return ApiResponse.success(rewards);
    }

    /**
     * 演示账号初始化批量发放手机充值红包。
     */
    @PostMapping("/mobile-topup-reward/demo-auto-login/grant")
    public ApiResponse<DemoAutoLoginGrantResponse> grantDemoAutoLoginRewards(
            @Valid @RequestBody DemoAutoLoginGrantRequest request) {
        CouponTemplate template = couponRepository.findTemplateByCode(MOBILE_TOP_UP_REWARD_TEMPLATE_CODE)
                .orElseThrow(() -> new IllegalStateException("手机充值红包模板未配置"));
        if (template.getTemplateId() == null) {
            throw new IllegalStateException("手机充值红包模板数据异常");
        }

        int issueCount = resolveDemoIssueCount(request.count());
        List<CouponIssue> existingDemoIssues = listExistingDemoAutoLoginIssues(template.getTemplateId(), request.userId());
        if (!existingDemoIssues.isEmpty()) {
            List<MobileTopUpRewardIssueResponse> existingRewards = existingDemoIssues.stream()
                    .sorted(Comparator.comparing(CouponIssue::getClaimedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(issueCount)
                    .map(this::toMobileTopUpRewardIssueResponse)
                    .toList();
            return ApiResponse.success(new DemoAutoLoginGrantResponse(
                    request.userId(),
                    0,
                    MOBILE_TOP_UP_REWARD_TEMPLATE_CODE,
                    existingRewards
            ));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusYears(DEMO_REWARD_VALID_YEARS);
        List<MobileTopUpRewardIssueResponse> issuedRewards = new ArrayList<>(issueCount);
        for (int index = 0; index < issueCount; index++) {
            Money randomAmount = randomDemoRewardAmount();
            String couponNo = buildCouponNo(request.userId());
            String businessNo = buildDemoBusinessNo(request.userId(), now, index);
            CouponIssue issue = CouponIssue.issue(
                    couponNo,
                    template.getTemplateId(),
                    request.userId(),
                    randomAmount,
                    DEMO_AUTO_LOGIN_CLAIM_CHANNEL,
                    businessNo,
                    now,
                    expireAt
            );
            CouponIssue persisted = couponRepository.saveIssue(issue);
            CouponIssue resolvedIssue = persisted == null ? issue : persisted;
            issuedRewards.add(toMobileTopUpRewardIssueResponse(resolvedIssue));
        }

        return ApiResponse.success(new DemoAutoLoginGrantResponse(
                request.userId(),
                issueCount,
                MOBILE_TOP_UP_REWARD_TEMPLATE_CODE,
                issuedRewards
        ));
    }

    private List<CouponIssue> listExistingDemoAutoLoginIssues(Long templateId, Long userId) {
        return couponRepository.findIssues(templateId, userId, null, 500)
                .stream()
                .filter(issue -> DEMO_AUTO_LOGIN_CLAIM_CHANNEL.equals(normalizeOptional(issue.getClaimChannel())))
                .toList();
    }

    private MobileTopUpRewardIssueResponse toMobileTopUpRewardIssueResponse(CouponIssue issue) {
        return new MobileTopUpRewardIssueResponse(
                issue.getCouponNo(),
                issue.getCouponAmount().getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                issue.getCouponAmount().getCurrencyUnit().getCode(),
                issue.getExpireAt(),
                issue.getExpireAt() == null ? null : issue.getExpireAt().toLocalDate(),
                issue.getStatus().name()
        );
    }

    private static BigDecimal couponAmountValue(CouponIssueDTO issue) {
        if (issue == null || issue.couponAmount() == null || issue.couponAmount().getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return issue.couponAmount().getAmount();
    }

    /**
     * 统计用户在指定渠道当日已领取次数。
     *
     * 说明：该逻辑作为横幅红包等“按天限领”场景的通用限制能力，
     * 后续新增活动可复用同一统计口径（模板ID + 用户ID + 领取渠道 + 当天日期）。
     */
    private int countTodayBannerClaims(Long templateId, Long userId, String claimChannel) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long claimedCount = couponRepository.findIssues(templateId, userId, null, 500)
                .stream()
                .filter(issue -> claimChannel.equals(normalizeOptional(issue.getClaimChannel())))
                .filter(issue -> issue.getClaimedAt() != null && issue.getClaimedAt().toLocalDate().equals(today))
                .count();
        return Math.toIntExact(claimedCount);
    }

    private ClaimMobileTopUpRewardResponse toClaimResponse(CouponIssueDTO issue, int todayClaimedCount) {
        int normalizedTodayClaimedCount = Math.max(todayClaimedCount, 0);
        int dailyLimit = MOBILE_TOP_UP_REWARD_DAILY_CLAIM_LIMIT;
        int remainingCount = Math.max(dailyLimit - normalizedTodayClaimedCount, 0);
        return new ClaimMobileTopUpRewardResponse(
                issue.couponNo(),
                MOBILE_TOP_UP_REWARD_TEMPLATE_CODE,
                issue.couponAmount().getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                issue.couponAmount().getCurrencyUnit().getCode(),
                issue.claimedAt(),
                dailyLimit,
                normalizedTodayClaimedCount,
                remainingCount
        );
    }

    private CouponIssueDTO toCouponIssueDTO(CouponIssue issue) {
        return new CouponIssueDTO(
                issue.getIssueId(),
                issue.getCouponNo(),
                issue.getTemplateId(),
                issue.getUserId(),
                issue.getCouponAmount(),
                issue.getStatus().name(),
                issue.getClaimChannel(),
                issue.getBusinessNo(),
                issue.getOrderNo(),
                issue.getBizOrderNo(),
                issue.getTradeOrderNo(),
                issue.getPayOrderNo(),
                issue.getClaimedAt(),
                issue.getExpireAt(),
                issue.getUsedAt(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    private String resolveBusinessNo(String businessNo, Long userId) {
        String normalized = normalizeOptional(businessNo);
        if (normalized != null) {
            return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
        }
        String autoGenerated = "TOPUP_BANNER_" + userId + "_"
                + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        return autoGenerated.length() > 64 ? autoGenerated.substring(0, 64) : autoGenerated;
    }

    private int resolveDemoIssueCount(Integer requestedCount) {
        if (requestedCount == null) {
            return DEMO_AUTO_LOGIN_DEFAULT_COUNT;
        }
        return Math.max(1, Math.min(requestedCount, DEMO_AUTO_LOGIN_MAX_COUNT));
    }

    private Money randomDemoRewardAmount() {
        double randomValue = ThreadLocalRandom.current().nextDouble(
                DEMO_REWARD_MIN_AMOUNT.doubleValue(),
                DEMO_REWARD_MAX_AMOUNT.doubleValue() + 0.0001D
        );
        BigDecimal amount = BigDecimal.valueOf(randomValue).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(DEMO_REWARD_MIN_AMOUNT) < 0) {
            amount = DEMO_REWARD_MIN_AMOUNT;
        }
        if (amount.compareTo(DEMO_REWARD_MAX_AMOUNT) > 0) {
            amount = DEMO_REWARD_MAX_AMOUNT;
        }
        return Money.of(DEMO_REWARD_CURRENCY, amount);
    }

    private String buildCouponNo(Long userId) {
        return aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_COUPON,
                aiPayBizTypeRegistry.couponIssueBizType(),
                String.valueOf(userId)
        );
    }

    private String buildDemoBusinessNo(Long userId, LocalDateTime now, int index) {
        String timePart = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).format(now);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10_000);
        String businessNo = String.format(Locale.ROOT,
                "DEMO_AUTO_LOGIN_%s_%s_%02d_%04d",
                userId,
                timePart,
                index,
                randomPart
        );
        return businessNo.length() > 64 ? businessNo.substring(0, 64) : businessNo;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 领取手机充值红包请求。
     */
    public record ClaimMobileTopUpRewardRequest(
            /** 用户ID */
            @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
            /** 业务幂等号 */
            String businessNo
    ) {
    }

    /**
     * 领取手机充值红包响应。
     */
    public record ClaimMobileTopUpRewardResponse(
            /** 红包券号 */
            String couponNo,
            /** 模板编码 */
            String templateCode,
            /** 红包金额 */
            String couponAmount,
            /** 币种 */
            String currencyCode,
            /** 领取时间 */
            LocalDateTime claimedAt,
            /** 每日领取上限 */
            int dailyClaimLimit,
            /** 今日已领取次数（含本次） */
            int todayClaimedCount,
            /** 今日剩余可领取次数 */
            int remainingClaimCount
    ) {
    }

    /**
     * 可用话费红包列表项。
     */
    public record MobileTopUpRewardIssueResponse(
            /** 红包券号 */
            String couponNo,
            /** 红包金额 */
            String couponAmount,
            /** 币种 */
            String currencyCode,
            /** 过期时间 */
            LocalDateTime expireAt,
            /** 过期日期 */
            LocalDate expireDate,
            /** 状态编码 */
            String status
    ) {
    }

    /**
     * 演示账号初始化发放红包请求。
     */
    public record DemoAutoLoginGrantRequest(
            /** 用户ID */
            @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
            /** 发放数量 */
            @Min(value = 1, message = "必须大于0") @Max(value = DEMO_AUTO_LOGIN_MAX_COUNT, message = "不能超过20") Integer count
    ) {
    }

    /**
     * 演示账号初始化发放红包响应。
     */
    public record DemoAutoLoginGrantResponse(
            /** 用户ID */
            Long userId,
            /** 发放数量 */
            int issuedCount,
            /** 模板编码 */
            String templateCode,
            /** 发放明细 */
            List<MobileTopUpRewardIssueResponse> issuedRewards
    ) {
    }
}
