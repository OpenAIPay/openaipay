package cn.openaipay.application.feedback.service.impl;

import cn.openaipay.application.feedback.command.HandleFeedbackCommand;
import cn.openaipay.application.feedback.command.SubmitFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import cn.openaipay.application.feedback.service.FeedbackService;
import cn.openaipay.domain.feedback.model.FeedbackStatus;
import cn.openaipay.domain.feedback.model.FeedbackTicket;
import cn.openaipay.domain.feedback.model.FeedbackType;
import cn.openaipay.domain.feedback.repository.FeedbackRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 反馈单应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    /** 单号时间 */
    private static final DateTimeFormatter FEEDBACK_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT);
    /** 默认信息 */
    private static final int DEFAULT_LIMIT = 20;
    /** 最大信息 */
    private static final int MAX_LIMIT = 100;

    /** 反馈信息 */
    private final FeedbackRepository feedbackRepository;
    /** 用户信息 */
    private final UserRepository userRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                                          UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    /**
     * 提交业务数据。
     */
    @Override
    @Transactional
    public FeedbackTicketDTO submit(SubmitFeedbackCommand command) {
        LocalDateTime now = LocalDateTime.now();
        FeedbackTicket ticket = FeedbackTicket.create(
                buildFeedbackNo(now),
                normalizeFeedbackUserId(command.userId()),
                FeedbackType.fromCode(command.feedbackType()),
                normalizeOptional(command.sourceChannel()) == null ? "IOS_APP" : command.sourceChannel().trim(),
                command.sourcePageCode(),
                command.title(),
                command.content(),
                command.contactMobile(),
                command.attachmentUrls(),
                now
        );
        return toDTO(feedbackRepository.save(ticket));
    }

    /**
     * 获取工单信息。
     */
    @Override
    @Transactional(readOnly = true)
    public FeedbackTicketDTO getTicket(String feedbackNo) {
        return toDTO(feedbackRepository.findByFeedbackNo(normalizeRequired(feedbackNo, "feedbackNo"))
                .orElseThrow(() -> new NoSuchElementException("feedback ticket not found: " + feedbackNo)));
    }

    /**
     * 查询用户工单信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<FeedbackTicketDTO> listUserTickets(Long userId, Integer limit) {
        return feedbackRepository.listByUserId(requirePositive(userId, "userId"), normalizeLimit(limit))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 查询工单信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<FeedbackTicketDTO> listTickets(String feedbackNo, String status, String feedbackType, Long userId, Integer limit) {
        Long normalizedUserId = userId == null ? null : normalizeFeedbackUserId(userId);
        FeedbackStatus normalizedStatus = normalizeOptional(status) == null ? null : FeedbackStatus.fromCode(status);
        FeedbackType normalizedType = normalizeOptional(feedbackType) == null ? null : FeedbackType.fromCode(feedbackType);
        Map<Long, String> nicknameCache = new HashMap<>();
        return feedbackRepository.listByFilters(normalizeOptional(feedbackNo), normalizedStatus, normalizedType, normalizedUserId, normalizeLimit(limit))
                .stream()
                .map(ticket -> toDTO(ticket, nicknameCache))
                .toList();
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public FeedbackTicketDTO handle(HandleFeedbackCommand command) {
        String feedbackNo = normalizeRequired(command.feedbackNo(), "feedbackNo");
        FeedbackTicket ticket = feedbackRepository.findByFeedbackNo(feedbackNo)
                .orElseThrow(() -> new NoSuchElementException("feedback ticket not found: " + feedbackNo));
        ticket.changeStatus(
                FeedbackStatus.fromCode(command.status()),
                normalizeRequired(command.handledBy(), "handledBy"),
                command.handleNote(),
                LocalDateTime.now()
        );
        return toDTO(feedbackRepository.save(ticket));
    }

    private FeedbackTicketDTO toDTO(FeedbackTicket ticket) {
        return toDTO(ticket, new HashMap<>());
    }

    private FeedbackTicketDTO toDTO(FeedbackTicket ticket, Map<Long, String> nicknameCache) {
        return new FeedbackTicketDTO(
                ticket.getFeedbackNo(),
                String.valueOf(ticket.getUserId()),
                resolveNickname(ticket.getUserId(), nicknameCache),
                ticket.getFeedbackType().name(),
                ticket.getSourceChannel(),
                ticket.getSourcePageCode(),
                ticket.getTitle(),
                ticket.getContent(),
                ticket.getContactMobile(),
                ticket.getAttachmentUrls(),
                ticket.getStatus().name(),
                ticket.getHandledBy(),
                ticket.getHandleNote(),
                ticket.getHandledAt(),
                ticket.getClosedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private String resolveNickname(Long userId, Map<Long, String> nicknameCache) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (nicknameCache.containsKey(userId)) {
            return nicknameCache.get(userId);
        }
        String nickname = userRepository.findByUserId(userId)
                .map(UserAggregate::getProfile)
                .map(UserProfile::getNickname)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(null);
        nicknameCache.put(userId, nickname);
        return nickname;
    }

    private String buildFeedbackNo(LocalDateTime now) {
        return "FDBK" + FEEDBACK_NO_TIME_FORMATTER.format(now)
                + String.format(Locale.ROOT, "%03d", ThreadLocalRandom.current().nextInt(0, 1000));
    }

    private Long normalizeFeedbackUserId(Long userId) {
        return requirePositive(userId, "userId");
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
