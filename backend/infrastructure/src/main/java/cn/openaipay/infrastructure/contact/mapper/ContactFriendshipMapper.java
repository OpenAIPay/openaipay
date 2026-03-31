package cn.openaipay.infrastructure.contact.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.contact.dataobject.ContactFriendshipDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface ContactFriendshipMapper extends BaseMapper<ContactFriendshipDO> {

    /**
     * 按条件查找记录。
     */
    default Optional<ContactFriendshipDO> findByOwnerAndFriend(Long ownerUserId, Long friendUserId) {
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.eq("friend_user_id", friendUserId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default List<ContactFriendshipDO> findByOwnerUserId(Long ownerUserId, int limit) {
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 按用户ID查找全部信息。
     */
    default List<ContactFriendshipDO> findAllByOwnerUserId(Long ownerUserId) {
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.orderByDesc("id");
        return selectList(wrapper);
    }

    /**
     * 按与用户ID查找记录。
     */
    default List<ContactFriendshipDO> findByOwnerAndFriendUserIds(Long ownerUserId, Collection<Long> friendUserIds) {
        if (ownerUserId == null || friendUserIds == null || friendUserIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.in("friend_user_id", friendUserIds);
        return selectList(wrapper);
    }

    /**
     * 按与删除记录。
     */
    default void deleteByOwnerAndFriend(Long ownerUserId, Long friendUserId) {
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.eq("friend_user_id", friendUserId);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
