package cn.openaipay.infrastructure.user.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.user.dataobject.UserRecentContactDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户最近联系人持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface UserRecentContactMapper extends BaseMapper<UserRecentContactDO> {

    /**
     * 按用户ID查询最近联系人，按最近互动时间倒序。
     *
     * @param ownerUserId 联系人所有者用户ID
     * @param limit 最大返回条数
     * @return 最近联系人实体列表
     */
    default List<UserRecentContactDO> findByOwnerUserIdOrderByLastInteractionDesc(Long ownerUserId, int limit) {
        if (ownerUserId == null || limit <= 0) {
            return List.of();
        }
        QueryWrapper<UserRecentContactDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.orderByDesc("last_interaction_at");
        wrapper.orderByDesc("interaction_count");
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + limit);
        return selectList(wrapper);
    }
}
