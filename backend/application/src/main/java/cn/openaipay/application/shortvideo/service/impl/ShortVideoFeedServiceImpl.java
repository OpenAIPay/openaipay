package cn.openaipay.application.shortvideo.service.impl;

import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedItemDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoPlaybackDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoStatsSnapshotDTO;
import cn.openaipay.application.shortvideo.port.ShortVideoPlaybackPort;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;
import cn.openaipay.application.shortvideo.service.ShortVideoFeedService;
import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.media.repository.MediaRepository;
import cn.openaipay.domain.shortvideo.model.FeedCursor;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import cn.openaipay.domain.shortvideo.model.UserVideoEngagement;
import cn.openaipay.domain.shortvideo.model.VideoStats;
import cn.openaipay.domain.shortvideo.repository.ShortVideoEngagementRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoPostRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoStatsRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频信息流应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Service
public class ShortVideoFeedServiceImpl implements ShortVideoFeedService {

    /** 默认返回条数。 */
    private static final int DEFAULT_LIMIT = 3;
    /** 最大返回条数。 */
    private static final int MAX_LIMIT = 10;

    /** 内容仓储。 */
    private final ShortVideoPostRepository shortVideoPostRepository;
    /** 统计仓储。 */
    private final ShortVideoStatsRepository shortVideoStatsRepository;
    /** 互动仓储。 */
    private final ShortVideoEngagementRepository shortVideoEngagementRepository;
    /** 用户仓储。 */
    private final UserRepository userRepository;
    /** 媒资仓储。 */
    private final MediaRepository mediaRepository;
    /** 播放地址解析端口。 */
    private final ShortVideoPlaybackPort shortVideoPlaybackPort;

    public ShortVideoFeedServiceImpl(ShortVideoPostRepository shortVideoPostRepository,
                                     ShortVideoStatsRepository shortVideoStatsRepository,
                                     ShortVideoEngagementRepository shortVideoEngagementRepository,
                                     UserRepository userRepository,
                                     MediaRepository mediaRepository,
                                     ShortVideoPlaybackPort shortVideoPlaybackPort) {
        this.shortVideoPostRepository = shortVideoPostRepository;
        this.shortVideoStatsRepository = shortVideoStatsRepository;
        this.shortVideoEngagementRepository = shortVideoEngagementRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.shortVideoPlaybackPort = shortVideoPlaybackPort;
    }

    /**
     * 查询当前用户可见的短视频信息流。
     */
    @Override
    @Transactional(readOnly = true)
    public ShortVideoFeedPageDTO listFeed(Long userId, ListShortVideoFeedQuery query) {
        Long normalizedUserId = requirePositive(userId, "userId");
        int normalizedLimit = normalizeLimit(query == null ? null : query.limit());
        FeedCursor cursor = parseCursor(query == null ? null : query.cursor());

        List<ShortVideoPost> loadedPosts = shortVideoPostRepository.listPublicFeed(cursor, normalizedLimit + 1);
        boolean hasMore = loadedPosts.size() > normalizedLimit;
        List<ShortVideoPost> posts = hasMore ? loadedPosts.subList(0, normalizedLimit) : loadedPosts;

        Map<Long, UserAggregate> authorMap = buildAuthorMap(posts);
        Map<String, VideoStats> statsMap = shortVideoStatsRepository.findByVideoIds(posts.stream().map(ShortVideoPost::getVideoId).toList());
        Map<String, UserVideoEngagement> engagementMap =
                shortVideoEngagementRepository.findByUserIdAndVideoIds(normalizedUserId, posts.stream().map(ShortVideoPost::getVideoId).toList());

        List<ShortVideoFeedItemDTO> items = new ArrayList<>();
        for (ShortVideoPost post : posts) {
            Optional<ShortVideoFeedItemDTO> item = buildFeedItem(post, normalizedUserId, authorMap, statsMap, engagementMap);
            item.ifPresent(items::add);
        }

        String nextCursor = hasMore && !posts.isEmpty() ? buildCursor(posts.get(posts.size() - 1)) : null;
        return new ShortVideoFeedPageDTO(items, nextCursor, hasMore);
    }

    private Map<Long, UserAggregate> buildAuthorMap(List<ShortVideoPost> posts) {
        Map<Long, UserAggregate> authorMap = new LinkedHashMap<>();
        userRepository.findProfilesByUserIds(posts.stream().map(ShortVideoPost::getCreatorUserId).distinct().toList())
                .forEach(aggregate -> authorMap.put(aggregate.getAccount().getUserId(), aggregate));
        return authorMap;
    }

    private Optional<ShortVideoFeedItemDTO> buildFeedItem(ShortVideoPost post,
                                                          Long currentUserId,
                                                          Map<Long, UserAggregate> authorMap,
                                                          Map<String, VideoStats> statsMap,
                                                          Map<String, UserVideoEngagement> engagementMap) {
        Optional<MediaAsset> coverMediaOpt = mediaRepository.findByMediaId(post.getCoverMediaId());
        Optional<MediaAsset> playbackMediaOpt = mediaRepository.findByMediaId(post.getPlaybackMediaId());
        if (coverMediaOpt.isEmpty() || playbackMediaOpt.isEmpty()) {
            return Optional.empty();
        }

        UserAggregate authorAggregate = authorMap.get(post.getCreatorUserId());
        ShortVideoAuthorDTO author = new ShortVideoAuthorDTO(
                post.getCreatorUserId(),
                resolveAuthorNickname(post.getCreatorUserId(), authorAggregate),
                authorAggregate == null || authorAggregate.getProfile() == null ? null : authorAggregate.getProfile().getAvatarUrl()
        );

        VideoStats stats = statsMap.getOrDefault(
                post.getVideoId(),
                new VideoStats(null, post.getVideoId(), 0, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now())
        );
        UserVideoEngagement engagement = engagementMap.getOrDefault(
                post.getVideoId(),
                UserVideoEngagement.emptyOf(currentUserId, post.getVideoId())
        );
        ShortVideoStatsSnapshotDTO snapshot = new ShortVideoStatsSnapshotDTO(
                engagement.isLiked(),
                engagement.isFavorited(),
                stats.getLikeCount(),
                stats.getFavoriteCount(),
                stats.getCommentCount()
        );

        ShortVideoPlaybackDTO playback = shortVideoPlaybackPort.resolvePlayback(post, playbackMediaOpt.get());
        String coverUrl = shortVideoPlaybackPort.resolveResourceUrl(coverMediaOpt.get());
        return Optional.of(new ShortVideoFeedItemDTO(
                post.getVideoId(),
                post.getCaption(),
                author,
                coverUrl,
                playback,
                snapshot
        ));
    }

    private String resolveAuthorNickname(Long creatorUserId, UserAggregate authorAggregate) {
        if (authorAggregate != null
                && authorAggregate.getProfile() != null
                && authorAggregate.getProfile().getNickname() != null
                && !authorAggregate.getProfile().getNickname().isBlank()) {
            return authorAggregate.getProfile().getNickname().trim();
        }
        return "用户" + creatorUserId;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String buildCursor(ShortVideoPost post) {
        String raw = post.getFeedPriority() + ":" + post.getId() + ":" + post.getVideoId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private FeedCursor parseCursor(String cursorToken) {
        if (cursorToken == null || cursorToken.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursorToken.trim()), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 3);
            if (parts.length < 3) {
                throw new IllegalArgumentException("cursor is invalid");
            }
            return new FeedCursor(
                    cursorToken.trim(),
                    Integer.parseInt(parts[0]),
                    Long.parseLong(parts[1]),
                    parts[2],
                    LocalDateTime.now()
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("cursor is invalid");
        }
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
