package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacPermissionDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC权限持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminRbacPermissionMapper extends BaseMapper<AdminRbacPermissionDO> {

    /**
     * 按订单模块编码ASC权限编码ASC查找全部信息。
     */
    default List<AdminRbacPermissionDO> findAllByOrderByModuleCodeAscPermissionCodeAsc() {
        QueryWrapper<AdminRbacPermissionDO> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("module_code");
        wrapper.orderByAsc("permission_code");
        return selectList(wrapper);
    }

    /**
     * 按模块编码订单权限编码ASC查找记录。
     */
    default List<AdminRbacPermissionDO> findByModuleCodeOrderByPermissionCodeAsc(String moduleCode) {
        QueryWrapper<AdminRbacPermissionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("module_code", moduleCode);
        wrapper.orderByAsc("permission_code");
        return selectList(wrapper);
    }

    /**
     * 按权限编码IN订单模块编码ASC权限编码ASC查找记录。
     */
    default List<AdminRbacPermissionDO> findByPermissionCodeInOrderByModuleCodeAscPermissionCodeAsc(List<String> permissionCodes) {
        QueryWrapper<AdminRbacPermissionDO> wrapper = new QueryWrapper<>();
        wrapper.in("permission_code", permissionCodes);
        wrapper.orderByAsc("module_code");
        wrapper.orderByAsc("permission_code");
        return selectList(wrapper);
    }
}
