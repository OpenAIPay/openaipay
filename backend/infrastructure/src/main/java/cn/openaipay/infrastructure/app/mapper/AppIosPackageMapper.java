package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppIosPackageDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * iOS 安装包持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AppIosPackageMapper extends BaseMapper<AppIosPackageDO> {

    /**
     * 按IOS编码查找记录。
     */
    default Optional<AppIosPackageDO> findByIosCode(String iosCode) {
        QueryWrapper<AppIosPackageDO> wrapper = new QueryWrapper<>();
        wrapper.eq("ios_code", iosCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按版本编码查找记录。
     */
    default Optional<AppIosPackageDO> findByVersionCode(String versionCode) {
        QueryWrapper<AppIosPackageDO> wrapper = new QueryWrapper<>();
        wrapper.eq("version_code", versionCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
