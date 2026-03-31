package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppDeviceDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * App 设备持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AppDeviceMapper extends BaseMapper<AppDeviceDO> {

    /**
     * 按设备ID查找记录。
     */
    default Optional<AppDeviceDO> findByDeviceId(String deviceId) {
        QueryWrapper<AppDeviceDO> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查询最近登录记录。
     */
    default Optional<AppDeviceDO> findLatestByUserId(Long userId) {
        QueryWrapper<AppDeviceDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .isNotNull("last_login_at")
                .orderByDesc("last_login_at")
                .orderByDesc("updated_at")
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按登录账号查询最近登录记录。
     */
    default Optional<AppDeviceDO> findLatestByLoginId(String loginId) {
        QueryWrapper<AppDeviceDO> wrapper = new QueryWrapper<>();
        wrapper.eq("login_id", loginId)
                .isNotNull("last_login_at")
                .orderByDesc("last_login_at")
                .orderByDesc("updated_at")
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按应用编码查询记录列表。
     */
    default List<AppDeviceDO> listByAppCode(String appCode) {
        QueryWrapper<AppDeviceDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .orderByDesc("last_opened_at")
                .orderByDesc("updated_at");
        return selectList(wrapper);
    }
}
