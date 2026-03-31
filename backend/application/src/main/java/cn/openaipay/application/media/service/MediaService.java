package cn.openaipay.application.media.service;

import cn.openaipay.application.media.command.UploadImageCommand;
import cn.openaipay.application.media.dto.MediaAssetDTO;
import cn.openaipay.application.media.dto.MediaBinaryDTO;
import java.util.List;

/**
 * 媒体应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface MediaService {

    /**
     * 处理业务数据。
     */
    MediaAssetDTO uploadImage(UploadImageCommand command);

    /**
     * 获取媒体信息。
     */
    MediaAssetDTO getMedia(String mediaId);

    /**
     * 按用户ID查询记录列表。
     */
    List<MediaAssetDTO> listByOwnerUserId(Long ownerUserId, Integer limit);

    /**
     * 加载业务数据。
     */
    MediaBinaryDTO loadBinary(String mediaId);
}
