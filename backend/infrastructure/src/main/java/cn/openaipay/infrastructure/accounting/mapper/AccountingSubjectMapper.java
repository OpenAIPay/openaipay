package cn.openaipay.infrastructure.accounting.mapper;

import cn.openaipay.infrastructure.accounting.dataobject.AccountingSubjectDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 会计科目Mapper。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Mapper
public interface AccountingSubjectMapper extends BaseMapper<AccountingSubjectDO> {

    /**
     * 按科目编码查找记录。
     */
    default Optional<AccountingSubjectDO> findBySubjectCode(String subjectCode) {
        QueryWrapper<AccountingSubjectDO> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_code", subjectCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
