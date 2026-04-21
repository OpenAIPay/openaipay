package cn.openaipay.adapter.shortvideo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.openaipay.adapter.common.GlobalExceptionHandler;
import cn.openaipay.adapter.security.UserSecurityInterceptor;
import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 短视频评论控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@ExtendWith(MockitoExtension.class)
class ShortVideoCommentControllerTest {

    /** 短视频门面。 */
    @Mock
    private ShortVideoFacade shortVideoFacade;

    /** 鉴权领域服务。 */
    @Mock
    private CredentialDomainService credentialDomainService;

    /** MockMvc。 */
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShortVideoCommentController(shortVideoFacade))
                .addInterceptors(new UserSecurityInterceptor(credentialDomainService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listCommentsShouldRequireAuthorization() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader(null)).thenReturn(null);

        mockMvc.perform(get("/api/short-video/videos/SVP202603310001/comments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listCommentsShouldReturnCommentPage() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listComments(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoCommentPageDTO(
                        List.of(comment("SVC-1", "第一条评论")),
                        "bmV4dA",
                        true
                ));

        mockMvc.perform(get("/api/short-video/videos/SVP202603310001/comments")
                        .header("Authorization", "Bearer demo-token")
                        .param("cursor", "Y3Vyc29yLTE")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].commentId").value("SVC-1"))
                .andExpect(jsonPath("$.data.items[0].user.nickname").value("顾郡"))
                .andExpect(jsonPath("$.data.hasMore").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("bmV4dA"));

        ArgumentCaptor<ListShortVideoCommentsQuery> captor = ArgumentCaptor.forClass(ListShortVideoCommentsQuery.class);
        verify(shortVideoFacade).listComments(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVP202603310001", captor.getValue().videoId());
        org.junit.jupiter.api.Assertions.assertEquals("Y3Vyc29yLTE", captor.getValue().cursor());
        org.junit.jupiter.api.Assertions.assertEquals(10, captor.getValue().limit());
    }

    @Test
    void createCommentShouldReturnCreatedComment() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.createComment(eq(880100068483692100L), any()))
                .thenReturn(comment("SVC-2", "发布成功"));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/comments")
                        .header("Authorization", "Bearer demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"发布成功"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value("SVC-2"))
                .andExpect(jsonPath("$.data.content").value("发布成功"));

        ArgumentCaptor<CreateShortVideoCommentCommand> captor = ArgumentCaptor.forClass(CreateShortVideoCommentCommand.class);
        verify(shortVideoFacade).createComment(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVP202603310001", captor.getValue().videoId());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().parentCommentId());
        org.junit.jupiter.api.Assertions.assertEquals("发布成功", captor.getValue().content());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().imageMediaId());
    }

    @Test
    void createCommentShouldAllowImageOnlyPayload() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.createComment(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoCommentDTO(
                        "SVC-IMAGE-1",
                        "SVP202603310001",
                        null,
                        null,
                        new ShortVideoAuthorDTO(880100068483692100L, "顾郡", "https://cdn.example.com/avatar.jpg"),
                        null,
                        "https://cdn.example.com/comment-image.jpg",
                        false,
                        0L,
                        0L,
                        List.of(),
                        LocalDateTime.of(2026, 4, 3, 11, 0)
                ));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/comments")
                        .header("Authorization", "Bearer demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"   ","imageMediaId":"MED-COMMENT-001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value("SVC-IMAGE-1"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://cdn.example.com/comment-image.jpg"));
    }

    @Test
    void listCommentsShouldReturn404WhenVideoNotFound() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listComments(eq(880100068483692100L), any()))
                .thenThrow(new NoSuchElementException("video not found: SVP404"));

        mockMvc.perform(get("/api/short-video/videos/SVP404/comments")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("未找到视频"));
    }

    @Test
    void listRepliesShouldReturnReplyPage() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listReplies(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoCommentPageDTO(
                        List.of(reply("SVC-REPLY-1", "展开后的第一条回复")),
                        "bmV4dC1yZXBseQ",
                        true
                ));

        mockMvc.perform(get("/api/short-video/comments/SVC-ROOT-1/replies")
                        .header("Authorization", "Bearer demo-token")
                        .param("cursor", "reply-cursor")
                        .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].commentId").value("SVC-REPLY-1"))
                .andExpect(jsonPath("$.data.items[0].parentCommentId").value("SVC-ROOT-1"))
                .andExpect(jsonPath("$.data.hasMore").value(true));

        ArgumentCaptor<ListShortVideoCommentRepliesQuery> captor = ArgumentCaptor.forClass(ListShortVideoCommentRepliesQuery.class);
        verify(shortVideoFacade).listReplies(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVC-ROOT-1", captor.getValue().commentId());
        org.junit.jupiter.api.Assertions.assertEquals("reply-cursor", captor.getValue().cursor());
        org.junit.jupiter.api.Assertions.assertEquals(8, captor.getValue().limit());
    }

    @Test
    void likeCommentShouldReturnLatestLikeState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.likeComment(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoCommentLikeDTO("SVC-1", true, 12));

        mockMvc.perform(post("/api/short-video/comments/SVC-1/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value("SVC-1"))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(12));

        ArgumentCaptor<LikeShortVideoCommentCommand> captor = ArgumentCaptor.forClass(LikeShortVideoCommentCommand.class);
        verify(shortVideoFacade).likeComment(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVC-1", captor.getValue().commentId());
    }

    @Test
    void unlikeCommentShouldReturnLatestLikeState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.unlikeComment(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoCommentLikeDTO("SVC-1", false, 11));

        mockMvc.perform(delete("/api/short-video/comments/SVC-1/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value("SVC-1"))
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(11));

        ArgumentCaptor<LikeShortVideoCommentCommand> captor = ArgumentCaptor.forClass(LikeShortVideoCommentCommand.class);
        verify(shortVideoFacade).unlikeComment(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVC-1", captor.getValue().commentId());
    }

    private ShortVideoCommentDTO comment(String commentId, String content) {
        return new ShortVideoCommentDTO(
                commentId,
                "SVP202603310001",
                null,
                null,
                new ShortVideoAuthorDTO(880100068483692100L, "顾郡", "https://cdn.example.com/avatar.jpg"),
                content,
                null,
                false,
                0L,
                0L,
                List.of(),
                LocalDateTime.of(2026, 3, 31, 16, 30)
        );
    }

    private ShortVideoCommentDTO reply(String commentId, String content) {
        return new ShortVideoCommentDTO(
                commentId,
                "SVP202603310001",
                "SVC-ROOT-1",
                "SVC-ROOT-1",
                new ShortVideoAuthorDTO(880902068943900002L, "祁欣", "https://cdn.example.com/avatar-2.jpg"),
                content,
                null,
                false,
                3L,
                0L,
                List.of(),
                LocalDateTime.of(2026, 4, 3, 10, 30)
        );
    }
}
