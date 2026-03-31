package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRoleDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理RBAC角色持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface AdminRbacRoleMapper extends BaseMapper<AdminRbacRoleDO> {

    /**
     * 按订单角色编码ASC查找全部信息。
     */
    default List<AdminRbacRoleDO> findAllByOrderByRoleCodeAsc() {
        QueryWrapper<AdminRbacRoleDO> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("role_code");
        return selectList(wrapper);
    }

    /**
     * 按角色状态订单角色编码ASC查找记录。
     */
    default List<AdminRbacRoleDO> findByRoleStatusIgnoreCaseOrderByRoleCodeAsc(String roleStatus) {
        QueryWrapper<AdminRbacRoleDO> wrapper = new QueryWrapper<>();
        String normalizedRoleStatus = roleStatus == null ? "" : roleStatus.trim().toUpperCase(Locale.ROOT);
        wrapper.apply("UPPER(role_status) = {0}", normalizedRoleStatus);
        wrapper.orderByAsc("role_code");
        return selectList(wrapper);
    }

    /**
     * 按角色编码查找记录。
     */
    default Optional<AdminRbacRoleDO> findByRoleCode(String roleCode) {
        QueryWrapper<AdminRbacRoleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
