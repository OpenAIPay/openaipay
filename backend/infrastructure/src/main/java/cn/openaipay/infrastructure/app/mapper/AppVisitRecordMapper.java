package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppVisitRecordDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * App 访问记录持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AppVisitRecordMapper extends BaseMapper<AppVisitRecordDO> {

    /**
     * 按设备ID查询记录列表。
     */
    default List<AppVisitRecordDO> listRecentByDeviceId(String deviceId, int limit) {
        QueryWrapper<AppVisitRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .orderByDesc("called_at")
                .last("LIMIT " + Math.max(limit, 1));
        return selectList(wrapper);
    }
}
