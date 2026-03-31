package cn.openaipay.infrastructure.user.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户资料持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfileDO> {

    /**
     * 按用户ID查找记录。
     */
    default Optional<UserProfileDO> findByUserId(Long userId) {
        QueryWrapper<UserProfileDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按用户ID查询用户资料。
     *
     * @param userIds 用户ID集合
     * @return 用户资料实体列表
     */
    default List<UserProfileDO> findByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<UserProfileDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return selectList(wrapper);
    }

    /**
     * 按身份证号查询用户资料。
     */
    default List<UserProfileDO> findByIdCardNo(String idCardNo) {
        if (idCardNo == null || idCardNo.isBlank()) {
            return List.of();
        }
        QueryWrapper<UserProfileDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id_card_no", idCardNo.trim().toUpperCase());
        return selectList(wrapper);
    }
}
