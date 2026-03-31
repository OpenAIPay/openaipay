package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRolePermissionDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC角色权限持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminRbacRolePermissionMapper extends BaseMapper<AdminRbacRolePermissionDO> {

    /**
     * 按角色编码订单权限编码ASC查找记录。
     */
    default List<AdminRbacRolePermissionDO> findByRoleCodeOrderByPermissionCodeAsc(String roleCode) {
        QueryWrapper<AdminRbacRolePermissionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode);
        wrapper.orderByAsc("permission_code");
        return selectList(wrapper);
    }

    /**
     * 按角色编码IN查找记录。
     */
    default List<AdminRbacRolePermissionDO> findByRoleCodeIn(List<String> roleCodes) {
        QueryWrapper<AdminRbacRolePermissionDO> wrapper = new QueryWrapper<>();
        wrapper.in("role_code", roleCodes);
        return selectList(wrapper);
    }

    /**
     * 按角色编码删除记录。
     */
    default void deleteByRoleCode(String roleCode) {
        QueryWrapper<AdminRbacRolePermissionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
