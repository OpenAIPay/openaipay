package cn.openaipay.infrastructure.security.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.security.dataobject.SecurityAttemptGuardDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全限流状态持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface SecurityAttemptGuardMapper extends BaseMapper<SecurityAttemptGuardDO> {

    /**
     * 按作用域+主体查询并加锁。
     */
    default Optional<SecurityAttemptGuardDO> findForUpdate(String scope, String principalKey) {
        QueryWrapper<SecurityAttemptGuardDO> wrapper = new QueryWrapper<>();
        wrapper.eq("guard_scope", scope)
                .eq("principal_key", principalKey)
                .last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}

