package cn.openaipay.application.adminaudit.service;

import cn.openaipay.application.adminaudit.command.AdminOperationAuditRecordCommand;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台操作审计服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public interface AdminOperationAuditService {

    /**
     * 记录后台操作日志。
     */
    void record(AdminOperationAuditRecordCommand command);

    /**
     * 查询后台操作日志。
     */
    List<AdminOperationAuditRowDTO> list(Long adminId,
                                         String requestMethod,
                                         String requestPath,
                                         String resultStatus,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         Integer limit);
}
