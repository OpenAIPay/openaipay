package cn.openaipay.infrastructure.user.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户账户持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountDO> {

    /**
     * 按用户ID查找记录。
     */
    default Optional<UserAccountDO> findByUserId(Long userId) {
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按登录ID查找记录。
     */
    default Optional<UserAccountDO> findByLoginId(String loginId) {
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("login_id", loginId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按用户ID查询用户账户。
     *
     * @param userIds 用户ID集合
     * @return 用户账户实体列表
     */
    default List<UserAccountDO> findByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return selectList(wrapper);
    }

    /**
     * 按UID处理记录。
     */
    default boolean existsByAipayUid(String aipayUid) {
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("aipay_uid", aipayUid);
        Long total = selectCount(wrapper);
        return total != null && total > 0;
    }
}
