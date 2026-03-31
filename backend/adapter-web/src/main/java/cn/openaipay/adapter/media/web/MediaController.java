package cn.openaipay.adapter.media.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.media.command.UploadImageCommand;
import cn.openaipay.application.media.dto.MediaAssetDTO;
import cn.openaipay.application.media.dto.MediaBinaryDTO;
import cn.openaipay.application.media.facade.MediaFacade;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    /** 媒体门面。 */
    private final MediaFacade mediaFacade;

    /** 创建媒体控制器并注入媒体门面。 */
    public MediaController(MediaFacade mediaFacade) {
        this.mediaFacade = mediaFacade;
    }

    /**
     * 处理业务数据。
     */
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAssetDTO> uploadImage(@RequestParam("ownerUserId") Long ownerUserId,
                                                   @RequestPart("file") MultipartFile file) {
        try {
            return ApiResponse.success(mediaFacade.uploadImage(new UploadImageCommand(
                    ownerUserId,
                    file.getOriginalFilename() == null ? "image" : file.getOriginalFilename(),
                    file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
                    file.getBytes()
            )));
        } catch (Exception ex) {
            throw new IllegalStateException("upload image failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * 获取媒体信息。
     */
    @GetMapping("/{mediaId}")
    public ApiResponse<MediaAssetDTO> getMedia(@PathVariable("mediaId") String mediaId) {
        return ApiResponse.success(mediaFacade.getMedia(mediaId));
    }

    /**
     * 查询媒体信息列表。
     */
    @GetMapping("/owners/{ownerUserId}")
    public ApiResponse<List<MediaAssetDTO>> listOwnerMedia(@PathVariable("ownerUserId") Long ownerUserId,
                                                           @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(mediaFacade.listByOwnerUserId(ownerUserId, limit));
    }

    /**
     * 加载内容信息。
     */
    @GetMapping("/{mediaId}/content")
    public ResponseEntity<byte[]> loadContent(@PathVariable("mediaId") String mediaId) {
        MediaBinaryDTO binary = mediaFacade.loadBinary(mediaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(binary.mimeType()));
        headers.setContentDisposition(ContentDisposition.inline().filename(binary.originalName()).build());
        return ResponseEntity.ok().headers(headers).body(binary.bytes());
    }
}
