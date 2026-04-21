package cn.openaipay.application.shortvideo.service.impl;

import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import cn.openaipay.application.shortvideo.port.ShortVideoPlaybackPort;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;
import cn.openaipay.application.shortvideo.service.ShortVideoCommentService;
import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.media.repository.MediaRepository;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import cn.openaipay.domain.shortvideo.model.VideoComment;
import cn.openaipay.domain.shortvideo.repository.ShortVideoCommentLikeRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoCommentRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoPostRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoStatsRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频评论应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Service
public class ShortVideoCommentServiceImpl implements ShortVideoCommentService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int PREVIEW_REPLY_LIMIT = 2;

    /** 视频仓储。 */
    private final ShortVideoPostRepository shortVideoPostRepository;
    /** 评论仓储。 */
    private final ShortVideoCommentRepository shortVideoCommentRepository;
    /** 评论点赞仓储。 */
    private final ShortVideoCommentLikeRepository shortVideoCommentLikeRepository;
    /** 统计仓储。 */
    private final ShortVideoStatsRepository shortVideoStatsRepository;
    /** 用户仓储。 */
    private final UserRepository userRepository;
    /** 媒资仓储。 */
    private final MediaRepository mediaRepository;
    /** 短视频资源解析端口。 */
    private final ShortVideoPlaybackPort shortVideoPlaybackPort;

    public ShortVideoCommentServiceImpl(ShortVideoPostRepository shortVideoPostRepository,
                                        ShortVideoCommentRepository shortVideoCommentRepository,
                                        ShortVideoCommentLikeRepository shortVideoCommentLikeRepository,
                                        ShortVideoStatsRepository shortVideoStatsRepository,
                                        UserRepository userRepository,
                                        MediaRepository mediaRepository,
                                        ShortVideoPlaybackPort shortVideoPlaybackPort) {
        this.shortVideoPostRepository = shortVideoPostRepository;
        this.shortVideoCommentRepository = shortVideoCommentRepository;
        this.shortVideoCommentLikeRepository = shortVideoCommentLikeRepository;
        this.shortVideoStatsRepository = shortVideoStatsRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.shortVideoPlaybackPort = shortVideoPlaybackPort;
    }

    /**
     * 查询评论列表。
     */
    @Override
    @Transactional(readOnly = true)
    public ShortVideoCommentPageDTO listComments(Long userId, ListShortVideoCommentsQuery query) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String videoId = normalizeVideoId(query == null ? null : query.videoId());
        requirePublicVideo(videoId);
        int limit = normalizeLimit(query == null ? null : query.limit());
        CommentCursor cursor = parseCursor(query == null ? null : query.cursor());

        List<VideoComment> loadedComments = shortVideoCommentRepository.listActiveTopLevelByVideoId(
                videoId,
                cursor == null ? null : cursor.lastCreatedAt(),
                cursor == null ? null : cursor.lastId(),
                limit + 1
        );
        boolean hasMore = loadedComments.size() > limit;
        List<VideoComment> comments = hasMore ? loadedComments.subList(0, limit) : loadedComments;

        Map<String, List<VideoComment>> previewReplyMap = buildPreviewReplyMap(comments);
        Map<Long, UserAggregate> userMap = buildUserMap(mergeComments(comments, previewReplyMap.values()));
        Set<String> likedCommentIds = buildLikedCommentIds(normalizedUserId, comments, previewReplyMap.values());

        List<ShortVideoCommentDTO> items = comments.stream()
                .map(comment -> toDTO(comment, userMap, likedCommentIds, previewReplyMap.get(comment.getCommentId())))
                .toList();
        String nextCursor = hasMore && !comments.isEmpty() ? buildCursor(comments.get(comments.size() - 1)) : null;
        return new ShortVideoCommentPageDTO(items, nextCursor, hasMore);
    }

    /**
     * 查询回复列表。
     */
    @Override
    @Transactional(readOnly = true)
    public ShortVideoCommentPageDTO listReplies(Long userId, ListShortVideoCommentRepliesQuery query) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String commentId = normalizeCommentId(query == null ? null : query.commentId());
        VideoComment anchorComment = requireActiveComment(commentId);
        requirePublicVideo(anchorComment.getVideoId());

        String rootCommentId = anchorComment.isTopLevel()
                ? anchorComment.getCommentId()
                : anchorComment.getRootThreadCommentId();
        int limit = normalizeLimit(query == null ? null : query.limit());
        CommentCursor cursor = parseCursor(query == null ? null : query.cursor());

        List<VideoComment> loadedReplies = shortVideoCommentRepository.listActiveRepliesByRootCommentId(
                rootCommentId,
                cursor == null ? null : cursor.lastCreatedAt(),
                cursor == null ? null : cursor.lastId(),
                limit + 1
        );
        boolean hasMore = loadedReplies.size() > limit;
        List<VideoComment> replies = hasMore ? loadedReplies.subList(0, limit) : loadedReplies;

        Map<Long, UserAggregate> userMap = buildUserMap(replies);
        Set<String> likedCommentIds = shortVideoCommentLikeRepository.findLikedCommentIds(
                normalizedUserId,
                replies.stream().map(VideoComment::getCommentId).toList()
        );

        List<ShortVideoCommentDTO> items = replies.stream()
                .map(reply -> toDTO(reply, userMap, likedCommentIds, List.of()))
                .toList();
        String nextCursor = hasMore && !replies.isEmpty() ? buildCursor(replies.get(replies.size() - 1)) : null;
        return new ShortVideoCommentPageDTO(items, nextCursor, hasMore);
    }

    /**
     * 发布评论。
     */
    @Override
    @Transactional
    public ShortVideoCommentDTO createComment(Long userId, CreateShortVideoCommentCommand command) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String videoId = normalizeVideoId(command == null ? null : command.videoId());
        ShortVideoPost post = requirePublicVideo(videoId);
        String parentCommentId = normalizeOptional(command == null ? null : command.parentCommentId());
        String content = normalizeContent(command == null ? null : command.content());
        String imageMediaId = normalizeOptional(command == null ? null : command.imageMediaId());
        if (content == null && imageMediaId == null) {
            throw new IllegalArgumentException("content or imageMediaId must not both be blank");
        }

        VideoComment parentComment = null;
        String rootCommentId = null;
        if (parentCommentId != null) {
            parentComment = requireActiveComment(parentCommentId);
            if (!post.getVideoId().equals(parentComment.getVideoId())) {
                throw new IllegalArgumentException("parentCommentId does not belong to current video");
            }
            rootCommentId = parentComment.isTopLevel()
                    ? parentComment.getCommentId()
                    : parentComment.getRootThreadCommentId();
        }

        if (imageMediaId != null) {
            MediaAsset imageMedia = mediaRepository.findByMediaId(imageMediaId)
                    .orElseThrow(() -> new NoSuchElementException("imageMediaId not found: " + imageMediaId));
            if (!normalizedUserId.equals(imageMedia.getOwnerUserId())) {
                throw new IllegalArgumentException("imageMediaId does not belong to current user");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        VideoComment saved = shortVideoCommentRepository.save(new VideoComment(
                null,
                buildCommentId(normalizedUserId, post.getVideoId(), now),
                post.getVideoId(),
                parentComment == null ? null : parentComment.getCommentId(),
                rootCommentId,
                normalizedUserId,
                content,
                imageMediaId,
                0L,
                0L,
                "ACTIVE",
                now,
                now
        ));

        if (rootCommentId != null) {
            shortVideoCommentRepository.adjustReplyCount(rootCommentId, 1L);
        }
        shortVideoStatsRepository.adjustCommentCount(post.getVideoId(), 1L);

        UserAggregate author = userRepository.findByUserId(normalizedUserId).orElse(null);
        return toDTO(
                saved,
                author == null ? Map.of() : Map.of(normalizedUserId, author),
                Set.of(),
                List.of()
        );
    }

    /**
     * 点赞评论。
     */
    @Override
    @Transactional
    public ShortVideoCommentLikeDTO likeComment(Long userId, LikeShortVideoCommentCommand command) {
        return mutateCommentLike(userId, command, true);
    }

    /**
     * 取消点赞评论。
     */
    @Override
    @Transactional
    public ShortVideoCommentLikeDTO unlikeComment(Long userId, LikeShortVideoCommentCommand command) {
        return mutateCommentLike(userId, command, false);
    }

    private ShortVideoCommentLikeDTO mutateCommentLike(Long userId,
                                                       LikeShortVideoCommentCommand command,
                                                       boolean targetState) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String commentId = normalizeCommentId(command == null ? null : command.commentId());
        VideoComment comment = requireActiveComment(commentId);
        requirePublicVideo(comment.getVideoId());

        boolean currentLiked = shortVideoCommentLikeRepository.existsByUserIdAndCommentId(normalizedUserId, commentId);
        if (currentLiked == targetState) {
            return new ShortVideoCommentLikeDTO(commentId, targetState, comment.getLikeCount());
        }

        if (targetState) {
            shortVideoCommentLikeRepository.saveLike(normalizedUserId, commentId);
            shortVideoCommentRepository.adjustLikeCount(commentId, 1L);
        } else {
            shortVideoCommentLikeRepository.deleteLike(normalizedUserId, commentId);
            shortVideoCommentRepository.adjustLikeCount(commentId, -1L);
        }

        long likeCount = shortVideoCommentRepository.findByCommentId(commentId)
                .map(VideoComment::getLikeCount)
                .orElse(Math.max(0L, comment.getLikeCount() + (targetState ? 1L : -1L)));
        return new ShortVideoCommentLikeDTO(commentId, targetState, likeCount);
    }

    private Map<String, List<VideoComment>> buildPreviewReplyMap(List<VideoComment> comments) {
        Map<String, List<VideoComment>> result = new LinkedHashMap<>();
        for (VideoComment comment : comments) {
            if (comment.getReplyCount() <= 0L) {
                result.put(comment.getCommentId(), List.of());
                continue;
            }
            result.put(
                    comment.getCommentId(),
                    shortVideoCommentRepository.listActiveRepliesByRootCommentId(
                            comment.getCommentId(),
                            null,
                            null,
                            PREVIEW_REPLY_LIMIT
                    )
            );
        }
        return result;
    }

    private Map<Long, UserAggregate> buildUserMap(Collection<VideoComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserAggregate> result = new LinkedHashMap<>();
        userRepository.findProfilesByUserIds(comments.stream().map(VideoComment::getUserId).distinct().toList())
                .forEach(aggregate -> result.put(aggregate.getAccount().getUserId(), aggregate));
        return result;
    }

    private Set<String> buildLikedCommentIds(Long userId,
                                             List<VideoComment> comments,
                                             Collection<List<VideoComment>> replyGroups) {
        Set<String> commentIds = new LinkedHashSet<>();
        comments.forEach(comment -> commentIds.add(comment.getCommentId()));
        replyGroups.forEach(group -> group.forEach(reply -> commentIds.add(reply.getCommentId())));
        return shortVideoCommentLikeRepository.findLikedCommentIds(userId, List.copyOf(commentIds));
    }

    private List<VideoComment> mergeComments(List<VideoComment> comments, Collection<List<VideoComment>> replyGroups) {
        List<VideoComment> merged = new ArrayList<>(comments);
        replyGroups.forEach(merged::addAll);
        return merged;
    }

    private ShortVideoCommentDTO toDTO(VideoComment comment,
                                       Map<Long, UserAggregate> userMap,
                                       Set<String> likedCommentIds,
                                       List<VideoComment> previewReplies) {
        UserAggregate userAggregate = userMap.get(comment.getUserId());
        return new ShortVideoCommentDTO(
                comment.getCommentId(),
                comment.getVideoId(),
                comment.getParentCommentId(),
                comment.getRootCommentId(),
                new ShortVideoAuthorDTO(
                        comment.getUserId(),
                        resolveNickname(comment.getUserId(), userAggregate),
                        userAggregate == null || userAggregate.getProfile() == null
                                ? null
                                : userAggregate.getProfile().getAvatarUrl()
                ),
                comment.getContent(),
                resolveImageUrl(comment.getImageMediaId()),
                likedCommentIds.contains(comment.getCommentId()),
                comment.getLikeCount(),
                comment.getReplyCount(),
                previewReplies == null
                        ? List.of()
                        : previewReplies.stream()
                                .map(reply -> toDTO(reply, userMap, likedCommentIds, List.of()))
                                .toList(),
                comment.getCreatedAt()
        );
    }

    private String resolveImageUrl(String imageMediaId) {
        if (imageMediaId == null || imageMediaId.isBlank()) {
            return null;
        }
        return mediaRepository.findByMediaId(imageMediaId.trim())
                .map(shortVideoPlaybackPort::resolveResourceUrl)
                .orElse(null);
    }

    private String resolveNickname(Long userId, UserAggregate userAggregate) {
        if (userAggregate != null
                && userAggregate.getProfile() != null
                && userAggregate.getProfile().getNickname() != null
                && !userAggregate.getProfile().getNickname().isBlank()) {
            return userAggregate.getProfile().getNickname().trim();
        }
        return "用户" + userId;
    }

    private ShortVideoPost requirePublicVideo(String videoId) {
        return shortVideoPostRepository.findByVideoId(videoId)
                .filter(ShortVideoPost::isPublicPublished)
                .orElseThrow(() -> new NoSuchElementException("video not found: " + videoId));
    }

    private VideoComment requireActiveComment(String commentId) {
        return shortVideoCommentRepository.findByCommentId(commentId)
                .filter(comment -> "ACTIVE".equalsIgnoreCase(comment.getStatus()))
                .orElseThrow(() -> new NoSuchElementException("comment not found: " + commentId));
    }

    private int normalizeLimit(Integer rawLimit) {
        if (rawLimit == null || rawLimit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(rawLimit, MAX_LIMIT);
    }

    private String normalizeVideoId(String rawVideoId) {
        if (rawVideoId == null || rawVideoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        return rawVideoId.trim();
    }

    private String normalizeCommentId(String rawCommentId) {
        if (rawCommentId == null || rawCommentId.isBlank()) {
            throw new IllegalArgumentException("commentId must not be blank");
        }
        return rawCommentId.trim();
    }

    private String normalizeContent(String rawContent) {
        String normalized = normalizeOptional(rawContent);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("content length must be <= 500");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String buildCommentId(Long userId, String videoId, LocalDateTime now) {
        return "SVC-" + userId + "-" + videoId + "-" + now.toEpochSecond(ZoneOffset.UTC) + "-" + (now.getNano() / 1_000_000);
    }

    private String buildCursor(VideoComment comment) {
        List<String> parts = new ArrayList<>();
        parts.add(String.valueOf(comment.getCreatedAt().toEpochSecond(ZoneOffset.UTC)));
        parts.add(String.valueOf(comment.getId()));
        parts.add(comment.getCommentId());
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(String.join(":", parts).getBytes(StandardCharsets.UTF_8));
    }

    private CommentCursor parseCursor(String cursorToken) {
        if (cursorToken == null || cursorToken.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursorToken.trim()), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 3);
            if (parts.length < 3) {
                throw new IllegalArgumentException("cursor is invalid");
            }
            return new CommentCursor(
                    LocalDateTime.ofEpochSecond(Long.parseLong(parts[0]), 0, ZoneOffset.UTC),
                    Long.parseLong(parts[1])
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("cursor is invalid");
        }
    }

    /**
     * 评论分页游标。
     */
    private record CommentCursor(LocalDateTime lastCreatedAt, Long lastId) {
    }
}
