package cn.openaipay.infrastructure.contact.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.contact.dataobject.ContactBlacklistDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface ContactBlacklistMapper extends BaseMapper<ContactBlacklistDO> {

    /**
     * 按条件查找记录。
     */
    default Optional<ContactBlacklistDO> findByOwnerAndBlocked(Long ownerUserId, Long blockedUserId) {
        QueryWrapper<ContactBlacklistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.eq("blocked_user_id", blockedUserId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按与用户ID查找记录。
     */
    default List<ContactBlacklistDO> findByOwnerAndBlockedUserIds(Long ownerUserId, Collection<Long> blockedUserIds) {
        if (ownerUserId == null || blockedUserIds == null || blockedUserIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ContactBlacklistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.in("blocked_user_id", blockedUserIds);
        return selectList(wrapper);
    }

    /**
     * 按与删除记录。
     */
    default void deleteByOwnerAndBlocked(Long ownerUserId, Long blockedUserId) {
        QueryWrapper<ContactBlacklistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.eq("blocked_user_id", blockedUserId);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
