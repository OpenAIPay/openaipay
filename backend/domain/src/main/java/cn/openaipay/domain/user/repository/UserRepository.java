package cn.openaipay.domain.user.repository;

import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserFeatureCode;
import cn.openaipay.domain.user.model.UserRecentContact;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface UserRepository {

    /**
     * 按用户ID查询用户聚合。
     */
    Optional<UserAggregate> findByUserId(Long userId);

    /**
     * 按用户ID批量查询用于资料展示的用户聚合。
     */
    List<UserAggregate> findProfilesByUserIds(List<Long> userIds);

    /**
     * 保存用户聚合。
     */
    UserAggregate save(UserAggregate aggregate);

    /**
     * 查询用户最近联系人列表。
     */
    List<UserRecentContact> listRecentContacts(Long ownerUserId, int limit);

    /**
     * 按UID处理记录。
     */
    boolean existsByAipayUid(String aipayUid);

    /**
     * 标记用户能力已开通。
     */
    void markFeatureEnabled(Long userId, UserFeatureCode featureCode, LocalDateTime openedAt);

    /**
     * 查询用户能力是否已开通。
     */
    boolean isFeatureEnabled(Long userId, UserFeatureCode featureCode);
}
