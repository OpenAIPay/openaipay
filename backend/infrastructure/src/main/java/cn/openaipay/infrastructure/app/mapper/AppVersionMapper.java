package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppVersionDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用版本持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AppVersionMapper extends BaseMapper<AppVersionDO> {

    /**
     * 按版本编码查找记录。
     */
    default Optional<AppVersionDO> findByVersionCode(String versionCode) {
        QueryWrapper<AppVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("version_code", versionCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按应用编码查找记录。
     */
    default List<AppVersionDO> findByAppCode(String appCode, String clientType) {
        QueryWrapper<AppVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode);
        if (clientType != null && !clientType.isBlank()) {
            wrapper.eq("client_type", clientType.trim());
        }
        wrapper.orderByDesc("updated_at");
        return selectList(wrapper);
    }

    /**
     * 查找版本信息。
     */
    default Optional<AppVersionDO> findLatestPublishedVersion(String appCode, String clientType) {
        QueryWrapper<AppVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("latest_published_version", true);
        if (clientType != null && !clientType.isBlank()) {
            wrapper.eq("client_type", clientType.trim());
        }
        wrapper.orderByDesc("updated_at").last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
