package cn.openaipay.adapter.shortvideo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.openaipay.adapter.common.GlobalExceptionHandler;
import cn.openaipay.adapter.security.UserSecurityInterceptor;
import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedItemDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoPlaybackDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoStatsSnapshotDTO;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 短视频信息流控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@ExtendWith(MockitoExtension.class)
class ShortVideoFeedControllerTest {

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
                .standaloneSetup(new ShortVideoFeedController(shortVideoFacade))
                .addInterceptors(new UserSecurityInterceptor(credentialDomainService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listFeedShouldRequireAuthorization() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader(null)).thenReturn(null);

        mockMvc.perform(get("/api/short-video/feed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listFeedShouldReturnFirstPageForAuthenticatedUser() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listFeed(eq(880100068483692100L), any())).thenReturn(new ShortVideoFeedPageDTO(
                java.util.List.of(feedItem("SVP202603310001")),
                "bmV4dA",
                true
        ));

        mockMvc.perform(get("/api/short-video/feed")
                        .header("Authorization", "Bearer demo-token")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].videoId").value("SVP202603310001"))
                .andExpect(jsonPath("$.data.items[0].author.nickname").value("顾郡"))
                .andExpect(jsonPath("$.data.items[0].playback.playbackUrl").value("https://cdn.example.com/video.mp4"))
                .andExpect(jsonPath("$.data.hasMore").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("bmV4dA"));

        ArgumentCaptor<ListShortVideoFeedQuery> captor = ArgumentCaptor.forClass(ListShortVideoFeedQuery.class);
        verify(shortVideoFacade).listFeed(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().cursor());
        org.junit.jupiter.api.Assertions.assertEquals(3, captor.getValue().limit());
    }

    @Test
    void listFeedShouldPassCursorToFacadeForPagination() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listFeed(eq(880100068483692100L), any())).thenReturn(new ShortVideoFeedPageDTO(
                java.util.List.of(feedItem("SVP202603310002")),
                "bmV4dDI",
                true
        ));

        mockMvc.perform(get("/api/short-video/feed")
                        .header("Authorization", "Bearer demo-token")
                        .param("cursor", "Y3Vyc29yLTE")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].videoId").value("SVP202603310002"))
                .andExpect(jsonPath("$.data.nextCursor").value("bmV4dDI"));

        ArgumentCaptor<ListShortVideoFeedQuery> captor = ArgumentCaptor.forClass(ListShortVideoFeedQuery.class);
        verify(shortVideoFacade).listFeed(eq(880100068483692100L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("Y3Vyc29yLTE", captor.getValue().cursor());
        org.junit.jupiter.api.Assertions.assertEquals(2, captor.getValue().limit());
    }

    @Test
    void listFeedShouldReturnEmptyPageWhenNoDataExists() throws Exception {
        when(credentialDomainService.resolveSubjectIdFromAuthorizationHeader("Bearer demo-token"))
                .thenReturn(880100068483692100L);
        when(shortVideoFacade.listFeed(eq(880100068483692100L), any())).thenReturn(new ShortVideoFeedPageDTO(
                java.util.List.of(),
                null,
                false
        ));

        mockMvc.perform(get("/api/short-video/feed")
                        .header("Authorization", "Bearer demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.hasMore").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()));
    }

    private ShortVideoFeedItemDTO feedItem(String videoId) {
        return new ShortVideoFeedItemDTO(
                videoId,
                "测试视频",
                new ShortVideoAuthorDTO(880100068483692100L, "顾郡", "https://cdn.example.com/avatar.jpg"),
                "https://cdn.example.com/cover.jpg",
                new ShortVideoPlaybackDTO(
                        "https://cdn.example.com/video.mp4",
                        "MP4",
                        "video/mp4",
                        15000L,
                        720,
                        1280
                ),
                new ShortVideoStatsSnapshotDTO(true, false, 128, 42, 8)
        );
    }
}
