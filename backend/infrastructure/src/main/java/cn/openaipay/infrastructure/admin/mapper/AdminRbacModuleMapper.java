package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacModuleDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC模块持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminRbacModuleMapper extends BaseMapper<AdminRbacModuleDO> {

    /**
     * 按订单单号ASCIDASC查找全部信息。
     */
    default List<AdminRbacModuleDO> findAllByOrderBySortNoAscIdAsc() {
        QueryWrapper<AdminRbacModuleDO> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_no");
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }
}
