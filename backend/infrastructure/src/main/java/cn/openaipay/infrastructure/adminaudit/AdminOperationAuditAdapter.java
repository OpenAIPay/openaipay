package cn.openaipay.infrastructure.adminaudit;

import cn.openaipay.application.adminaudit.command.AdminOperationAuditRecordCommand;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import cn.openaipay.application.adminaudit.port.AdminOperationAuditPort;
import cn.openaipay.infrastructure.adminaudit.dataobject.AdminOperationAuditDO;
import cn.openaipay.infrastructure.adminaudit.mapper.AdminOperationAuditMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 后台操作审计端口适配器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Component
public class AdminOperationAuditAdapter implements AdminOperationAuditPort {

    private final AdminOperationAuditMapper adminOperationAuditMapper;

    public AdminOperationAuditAdapter(AdminOperationAuditMapper adminOperationAuditMapper) {
        this.adminOperationAuditMapper = adminOperationAuditMapper;
    }

    @Override
    public void record(AdminOperationAuditRecordCommand command) {
        if (command == null) {
            return;
        }
        AdminOperationAuditDO entity = new AdminOperationAuditDO();
        entity.setTraceId(command.traceId());
        entity.setAdminId(command.adminId());
        entity.setAdminUsername(command.adminUsername());
        entity.setRequestMethod(command.requestMethod());
        entity.setRequestPath(command.requestPath());
        entity.setRequestQuery(command.requestQuery());
        entity.setRequestBody(command.requestBody());
        entity.setResultStatus(command.resultStatus());
        entity.setErrorMessage(command.errorMessage());
        entity.setCostMs(command.costMs());
        entity.setClientIp(command.clientIp());
        entity.setUserAgent(command.userAgent());
        entity.setCreatedAt(command.createdAt() == null ? LocalDateTime.now() : command.createdAt());
        adminOperationAuditMapper.insert(entity);
    }

    @Override
    public List<AdminOperationAuditRowDTO> list(Long adminId,
                                                String requestMethod,
                                                String requestPath,
                                                String resultStatus,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                int limit) {
        return adminOperationAuditMapper.query(adminId, requestMethod, requestPath, resultStatus, from, to, limit)
                .stream()
                .map(this::toRow)
                .toList();
    }

    private AdminOperationAuditRowDTO toRow(AdminOperationAuditDO entity) {
        return new AdminOperationAuditRowDTO(
                entity.getId(),
                entity.getTraceId(),
                entity.getAdminId(),
                entity.getAdminUsername(),
                entity.getRequestMethod(),
                entity.getRequestPath(),
                entity.getRequestQuery(),
                entity.getRequestBody(),
                entity.getResultStatus(),
                entity.getErrorMessage(),
                entity.getCostMs(),
                entity.getClientIp(),
                entity.getUserAgent(),
                entity.getCreatedAt()
        );
    }
}
