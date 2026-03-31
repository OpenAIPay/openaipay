package cn.openaipay.infrastructure.user.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.user.dataobject.UserFeatureStatusDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 用户能力开通状态持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface UserFeatureStatusMapper extends BaseMapper<UserFeatureStatusDO> {

    /**
     * 按用户ID与功能编码查找记录。
     */
    default Optional<UserFeatureStatusDO> findByUserIdAndFeatureCode(Long userId, String featureCode) {
        QueryWrapper<UserFeatureStatusDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("feature_code", featureCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

}
