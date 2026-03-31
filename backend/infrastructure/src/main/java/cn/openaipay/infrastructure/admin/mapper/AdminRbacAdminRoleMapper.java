package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacAdminRoleDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC后台管理角色持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminRbacAdminRoleMapper extends BaseMapper<AdminRbacAdminRoleDO> {

    /**
     * 按后台ID订单角色编码ASC查找记录。
     */
    default List<AdminRbacAdminRoleDO> findByAdminIdOrderByRoleCodeAsc(Long adminId) {
        QueryWrapper<AdminRbacAdminRoleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("admin_id", adminId);
        wrapper.orderByAsc("role_code");
        return selectList(wrapper);
    }

    /**
     * 按后台ID删除记录。
     */
    default void deleteByAdminId(Long adminId) {
        QueryWrapper<AdminRbacAdminRoleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("admin_id", adminId);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
