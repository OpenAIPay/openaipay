package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import cn.openaipay.application.adminaudit.facade.AdminOperationAuditFacade;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台审计日志控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@ExtendWith(MockitoExtension.class)
class AdminAuditControllerTest {

    @Mock
    private AdminOperationAuditFacade adminOperationAuditFacade;

    private AdminAuditController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAuditController(adminOperationAuditFacade);
    }

    @Test
    void listShouldPassFiltersAndMapFields() {
        AdminOperationAuditRowDTO row = new AdminOperationAuditRowDTO(
                1L,
                "TRACE-001",
                9001L,
                "ops_admin",
                "POST",
                "/api/admin/outbox/dead-letters/requeue",
                null,
                null,
                "SUCCESS",
                null,
                123L,
                null,
                null,
                LocalDateTime.of(2026, 3, 23, 11, 0)
        );
        when(adminOperationAuditFacade.list(
                9001L,
                "POST",
                "/api/admin/outbox",
                "SUCCESS",
                null,
                null,
                20
        )).thenReturn(List.of(row));

        ApiResponse<List<AdminAuditController.AdminAuditRow>> response = controller.list(
                9001L,
                "POST",
                "/api/admin/outbox",
                "SUCCESS",
                null,
                null,
                20
        );

        verify(adminOperationAuditFacade).list(9001L, "POST", "/api/admin/outbox", "SUCCESS", null, null, 20);
        assertEquals(1, response.data().size());
        assertThat(response.data().get(0).traceId()).isEqualTo("TRACE-001");
        assertThat(response.data().get(0).requestPath()).isEqualTo("/api/admin/outbox/dead-letters/requeue");
        assertThat(response.data().get(0).resultStatus()).isEqualTo("SUCCESS");
    }
}
