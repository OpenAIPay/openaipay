package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppInfoDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用定义持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AppInfoMapper extends BaseMapper<AppInfoDO> {

    /**
     * 按应用编码查找记录。
     */
    default Optional<AppInfoDO> findByAppCode(String appCode) {
        QueryWrapper<AppInfoDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
