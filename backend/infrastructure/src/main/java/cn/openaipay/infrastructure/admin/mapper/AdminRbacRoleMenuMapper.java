package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRoleMenuDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC角色菜单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminRbacRoleMenuMapper extends BaseMapper<AdminRbacRoleMenuDO> {

    /**
     * 按角色编码订单菜单编码ASC查找记录。
     */
    default List<AdminRbacRoleMenuDO> findByRoleCodeOrderByMenuCodeAsc(String roleCode) {
        QueryWrapper<AdminRbacRoleMenuDO> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode);
        wrapper.orderByAsc("menu_code");
        return selectList(wrapper);
    }

    /**
     * 按角色编码IN查找记录。
     */
    default List<AdminRbacRoleMenuDO> findByRoleCodeIn(List<String> roleCodes) {
        QueryWrapper<AdminRbacRoleMenuDO> wrapper = new QueryWrapper<>();
        wrapper.in("role_code", roleCodes);
        return selectList(wrapper);
    }

    /**
     * 按角色编码删除记录。
     */
    default void deleteByRoleCode(String roleCode) {
        QueryWrapper<AdminRbacRoleMenuDO> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
