package cn.openaipay.infrastructure.agreement.mapper;

import cn.openaipay.infrastructure.agreement.dataobject.AgreementTemplateDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 协议模板持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface AgreementTemplateMapper extends BaseMapper<AgreementTemplateDO> {

    /**
     * 按业务与状态查找记录。
     */
    default List<AgreementTemplateDO> findByBizTypeAndStatus(String bizType, String status) {
        QueryWrapper<AgreementTemplateDO> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_type", bizType);
        wrapper.eq("status", status);
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

}
