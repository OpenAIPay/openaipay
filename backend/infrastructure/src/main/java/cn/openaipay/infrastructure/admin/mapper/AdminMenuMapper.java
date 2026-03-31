package cn.openaipay.infrastructure.admin.mapper;

import cn.openaipay.infrastructure.admin.dataobject.AdminMenuDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理菜单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface AdminMenuMapper extends BaseMapper<AdminMenuDO> {

    /**
     * 按订单单号ASCIDASC查找记录。
     */
    default List<AdminMenuDO> findByVisibleTrueOrderBySortNoAscIdAsc() {
        QueryWrapper<AdminMenuDO> wrapper = new QueryWrapper<>();
        wrapper.eq("visible", true);
        wrapper.orderByAsc("sort_no");
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 按菜单编码IN与订单单号ASCIDASC查找记录。
     */
    default List<AdminMenuDO> findByMenuCodeInAndVisibleTrueOrderBySortNoAscIdAsc(Set<String> menuCodes) {
        QueryWrapper<AdminMenuDO> wrapper = new QueryWrapper<>();
        wrapper.in("menu_code", menuCodes);
        wrapper.eq("visible", true);
        wrapper.orderByAsc("sort_no");
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }
}
