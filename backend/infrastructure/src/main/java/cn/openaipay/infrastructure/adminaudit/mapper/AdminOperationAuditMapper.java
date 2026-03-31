package cn.openaipay.infrastructure.adminaudit.mapper;

import cn.openaipay.infrastructure.adminaudit.dataobject.AdminOperationAuditDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台操作审计 mapper。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AdminOperationAuditMapper extends BaseMapper<AdminOperationAuditDO> {

    /**
     * 条件查询审计日志。
     */
    default List<AdminOperationAuditDO> query(Long adminId,
                                              String requestMethod,
                                              String requestPath,
                                              String resultStatus,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        QueryWrapper<AdminOperationAuditDO> wrapper = new QueryWrapper<>();
        if (adminId != null && adminId > 0) {
            wrapper.eq("admin_id", adminId);
        }
        if (requestMethod != null && !requestMethod.isBlank()) {
            wrapper.eq("request_method", requestMethod.trim().toUpperCase());
        }
        if (requestPath != null && !requestPath.isBlank()) {
            wrapper.like("request_path", requestPath.trim());
        }
        if (resultStatus != null && !resultStatus.isBlank()) {
            wrapper.eq("result_status", resultStatus.trim().toUpperCase());
        }
        if (from != null) {
            wrapper.ge("created_at", from);
        }
        if (to != null) {
            wrapper.le("created_at", to);
        }
        wrapper.orderByDesc("id").last("LIMIT " + normalizedLimit);
        return selectList(wrapper);
    }
}
