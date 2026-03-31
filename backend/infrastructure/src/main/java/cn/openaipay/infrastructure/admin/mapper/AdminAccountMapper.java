package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminAccountDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理账户持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface AdminAccountMapper extends BaseMapper<AdminAccountDO> {

    /**
     * 按用户名查找记录。
     */
    default Optional<AdminAccountDO> findByUsername(String username) {
        QueryWrapper<AdminAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按后台ID查找记录。
     */
    default Optional<AdminAccountDO> findByAdminId(Long adminId) {
        QueryWrapper<AdminAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("admin_id", adminId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按订单后台IDASC查找全部信息。
     */
    default List<AdminAccountDO> findAllByOrderByAdminIdAsc() {
        QueryWrapper<AdminAccountDO> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("admin_id");
        return selectList(wrapper);
    }
}
