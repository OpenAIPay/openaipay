package cn.openaipay.application.media.port;

/**
 * 媒体存储端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public interface MediaStoragePort {

    /**
     * 处理业务数据。
     */
    MediaStoredObject storeImage(Long ownerUserId, String originalName, String mimeType, byte[] content);

    /**
     * 加载业务数据。
     */
    MediaLoadedObject loadFile(String storagePath, String fallbackMimeType);
}
