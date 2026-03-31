package cn.openaipay.infrastructure.agreement.mapper;

import cn.openaipay.infrastructure.agreement.dataobject.AgreementSignItemDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 协议签约明细持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface AgreementSignItemMapper extends BaseMapper<AgreementSignItemDO> {

    /**
     * 按单号查找记录。
     */
    default List<AgreementSignItemDO> findBySignNo(String signNo) {
        QueryWrapper<AgreementSignItemDO> wrapper = new QueryWrapper<>();
        wrapper.eq("sign_no", signNo);
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 按单号删除记录。
     */
    default void deleteBySignNo(String signNo) {
        QueryWrapper<AgreementSignItemDO> wrapper = new QueryWrapper<>();
        wrapper.eq("sign_no", signNo);
      /**
       * 删除业务数据。
       */
        delete(wrapper);
    }
}
