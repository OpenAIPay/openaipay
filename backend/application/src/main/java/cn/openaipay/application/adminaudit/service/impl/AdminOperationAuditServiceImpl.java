package cn.openaipay.application.adminaudit.service.impl;

import cn.openaipay.application.adminaudit.command.AdminOperationAuditRecordCommand;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import cn.openaipay.application.adminaudit.port.AdminOperationAuditPort;
import cn.openaipay.application.adminaudit.service.AdminOperationAuditService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台操作审计服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Service
public class AdminOperationAuditServiceImpl implements AdminOperationAuditService {

    private static final int DEFAULT_LIMIT = 20;

    private final AdminOperationAuditPort adminOperationAuditPort;

    public AdminOperationAuditServiceImpl(AdminOperationAuditPort adminOperationAuditPort) {
        this.adminOperationAuditPort = adminOperationAuditPort;
    }

    @Override
    @Transactional
    public void record(AdminOperationAuditRecordCommand command) {
        if (command == null) {
            return;
        }
        adminOperationAuditPort.record(command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOperationAuditRowDTO> list(Long adminId,
                                                String requestMethod,
                                                String requestPath,
                                                String resultStatus,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                Integer limit) {
        int normalizedLimit = limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, 200));
        return adminOperationAuditPort.list(adminId, requestMethod, requestPath, resultStatus, from, to, normalizedLimit);
    }
}
