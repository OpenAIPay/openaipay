package cn.openaipay.infrastructure.user.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.user.dataobject.UserPrivacySettingDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户隐私设置持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface UserPrivacySettingMapper extends BaseMapper<UserPrivacySettingDO> {

    /**
     * 按用户ID查找记录。
     */
    default Optional<UserPrivacySettingDO> findByUserId(Long userId) {
        QueryWrapper<UserPrivacySettingDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
