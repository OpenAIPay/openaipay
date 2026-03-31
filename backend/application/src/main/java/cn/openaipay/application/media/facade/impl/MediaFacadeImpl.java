package cn.openaipay.application.media.facade.impl;

import cn.openaipay.application.media.command.UploadImageCommand;
import cn.openaipay.application.media.dto.MediaAssetDTO;
import cn.openaipay.application.media.dto.MediaBinaryDTO;
import cn.openaipay.application.media.facade.MediaFacade;
import cn.openaipay.application.media.service.MediaService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 媒体门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class MediaFacadeImpl implements MediaFacade {

    /** 媒体信息 */
    private final MediaService mediaService;

    public MediaFacadeImpl(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public MediaAssetDTO uploadImage(UploadImageCommand command) {
        return mediaService.uploadImage(command);
    }

    /**
     * 获取媒体信息。
     */
    @Override
    public MediaAssetDTO getMedia(String mediaId) {
        return mediaService.getMedia(mediaId);
    }

    /**
     * 按用户ID查询记录列表。
     */
    @Override
    public List<MediaAssetDTO> listByOwnerUserId(Long ownerUserId, Integer limit) {
        return mediaService.listByOwnerUserId(ownerUserId, limit);
    }

    /**
     * 加载业务数据。
     */
    @Override
    public MediaBinaryDTO loadBinary(String mediaId) {
        return mediaService.loadBinary(mediaId);
    }
}
