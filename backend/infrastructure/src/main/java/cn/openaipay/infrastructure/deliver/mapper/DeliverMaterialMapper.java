package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverMaterialDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * DeliverMaterialMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface DeliverMaterialMapper extends BaseMapper<DeliverMaterialDO> {

    /**
     * 按编码查找记录。
     */
    default List<DeliverMaterialDO> findByCodes(Collection<String> materialCodes) {
        if (materialCodes == null || materialCodes.isEmpty()) {
            return List.of();
        }
        QueryWrapper<DeliverMaterialDO> wrapper = new QueryWrapper<>();
        wrapper.in("material_code", materialCodes);
        return selectList(wrapper);
    }
}
