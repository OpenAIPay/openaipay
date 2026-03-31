package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppBehaviorEventDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * App 行为埋点持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AppBehaviorEventMapper extends BaseMapper<AppBehaviorEventDO> {
}
