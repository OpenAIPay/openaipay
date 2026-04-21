package cn.openaipay.adapter.shortvideo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.openaipay.adapter.common.GlobalExceptionHandler;
import cn.openaipay.adapter.security.UserSecurityInterceptor;
import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 短视频互动控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@ExtendWith(MockitoExtension.class)
class ShortVideoEngagementControllerTest {

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
                .standaloneSetup(new ShortVideoEngagementController(shortVideoFacade))
                .addInterceptors(new UserSecurityInterceptor(credentialDomainService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void engagementEndpointsShouldRequireAuthorization() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader(null)).thenReturn(null);

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void likeShouldReturnLatestEngagementState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.like(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoEngagementDTO(true, false, 129, 42, 8));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(129))
                .andExpect(jsonPath("$.data.favoriteCount").value(42));

        ArgumentCaptor<LikeShortVideoCommand> captor = ArgumentCaptor.forClass(LikeShortVideoCommand.class);
        verify(shortVideoFacade).like(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVP202603310001", captor.getValue().videoId());
    }

    @Test
    void unlikeShouldReturnLatestEngagementState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.unlike(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoEngagementDTO(false, false, 128, 42, 8));

        mockMvc.perform(delete("/api/short-video/videos/SVP202603310001/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(128));
    }

    @Test
    void favoriteShouldReturnLatestEngagementState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.favorite(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoEngagementDTO(true, true, 128, 43, 8));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/favorite")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").value(43));

        ArgumentCaptor<FavoriteShortVideoCommand> captor = ArgumentCaptor.forClass(FavoriteShortVideoCommand.class);
        verify(shortVideoFacade).favorite(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("SVP202603310001", captor.getValue().videoId());
    }

    @Test
    void unfavoriteShouldReturnLatestEngagementState() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.unfavorite(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoEngagementDTO(true, false, 128, 42, 8));

        mockMvc.perform(delete("/api/short-video/videos/SVP202603310001/favorite")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(false))
                .andExpect(jsonPath("$.data.favoriteCount").value(42));
    }

    @Test
    void repeatedLikeShouldRemainIdempotent() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.like(eq(880100068483692100L), any()))
                .thenReturn(new ShortVideoEngagementDTO(true, false, 129, 42, 8));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(129));

        mockMvc.perform(post("/api/short-video/videos/SVP202603310001/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(129));
    }

    @Test
    void likeShouldReturn404WhenVideoDoesNotExist() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.like(eq(880100068483692100L), any()))
                .thenThrow(new NoSuchElementException("video not found: SVP404"));

        mockMvc.perform(post("/api/short-video/videos/SVP404/like")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("未找到视频"));
    }
}
