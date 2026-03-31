package cn.openaipay.application.adminaudit.facade.impl;

import cn.openaipay.application.adminaudit.command.AdminOperationAuditRecordCommand;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import cn.openaipay.application.adminaudit.facade.AdminOperationAuditFacade;
import cn.openaipay.application.adminaudit.service.AdminOperationAuditService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 后台操作审计门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Service
public class AdminOperationAuditFacadeImpl implements AdminOperationAuditFacade {

    private final AdminOperationAuditService adminOperationAuditService;

    public AdminOperationAuditFacadeImpl(AdminOperationAuditService adminOperationAuditService) {
        this.adminOperationAuditService = adminOperationAuditService;
    }

    @Override
    public void record(AdminOperationAuditRecordCommand command) {
        adminOperationAuditService.record(command);
    }

    @Override
    public List<AdminOperationAuditRowDTO> list(Long adminId,
                                                String requestMethod,
                                                String requestPath,
                                                String resultStatus,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                Integer limit) {
        return adminOperationAuditService.list(adminId, requestMethod, requestPath, resultStatus, from, to, limit);
    }
}
