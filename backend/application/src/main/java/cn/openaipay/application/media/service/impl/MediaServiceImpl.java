package cn.openaipay.application.media.service.impl;

import cn.openaipay.application.media.command.UploadImageCommand;
import cn.openaipay.application.media.dto.MediaAssetDTO;
import cn.openaipay.application.media.dto.MediaBinaryDTO;
import cn.openaipay.application.media.port.MediaLoadedObject;
import cn.openaipay.application.media.port.MediaStoragePort;
import cn.openaipay.application.media.port.MediaStoredObject;
import cn.openaipay.application.media.service.MediaService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.media.repository.MediaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 媒体应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class MediaServiceImpl implements MediaService {

    /** 默认信息 */
    private static final int DEFAULT_LIMIT = 20;
    /** 最大信息 */
    private static final int MAX_LIMIT = 100;
    /** 媒体ID业务类型编码 */
    private static final String MEDIA_ID_BIZ_TYPE = "93";

    /** 媒体信息 */
    private final MediaRepository mediaRepository;
    /** 媒体信息 */
    private final MediaStoragePort mediaStoragePort;
    /** 全局ID生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;

    public MediaServiceImpl(MediaRepository mediaRepository,
                                       MediaStoragePort mediaStoragePort,
                                       AiPayIdGenerator aiPayIdGenerator) {
        this.mediaRepository = mediaRepository;
        this.mediaStoragePort = mediaStoragePort;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public MediaAssetDTO uploadImage(UploadImageCommand command) {
        Long ownerUserId = requirePositive(command.ownerUserId(), "ownerUserId");
        String originalName = normalizeRequired(command.originalName(), "originalName");
        String mimeType = normalizeRequired(command.mimeType(), "mimeType").toLowerCase(Locale.ROOT);
        byte[] bytes = command.bytes();
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("image bytes must not be empty");
        }
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("only image/* is supported");
        }

        MediaStoredObject stored = mediaStoragePort.storeImage(ownerUserId, originalName, mimeType, bytes);
        MediaAsset mediaAsset = MediaAsset.create(
                buildMediaId(ownerUserId),
                ownerUserId,
                "IMAGE",
                originalName,
                stored.mimeType(),
                stored.sizeBytes(),
                stored.compressedSizeBytes(),
                stored.width(),
                stored.height(),
                stored.storagePath(),
                stored.thumbnailPath(),
                stored.sha256(),
                LocalDateTime.now()
        );
        return toDTO(mediaRepository.save(mediaAsset));
    }

    /**
     * 获取媒体信息。
     */
    @Override
    @Transactional(readOnly = true)
    public MediaAssetDTO getMedia(String mediaId) {
        return toDTO(mustGet(mediaId));
    }

    /**
     * 按用户ID查询记录列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaAssetDTO> listByOwnerUserId(Long ownerUserId, Integer limit) {
        return mediaRepository.listByOwnerUserId(requirePositive(ownerUserId, "ownerUserId"), normalizeLimit(limit))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 加载业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public MediaBinaryDTO loadBinary(String mediaId) {
        MediaAsset mediaAsset = mustGet(mediaId);
        MediaLoadedObject loaded = mediaStoragePort.loadFile(mediaAsset.getStoragePath(), mediaAsset.getMimeType());
        return new MediaBinaryDTO(
                mediaAsset.getMediaId(),
                loaded.mimeType(),
                mediaAsset.getOriginalName(),
                loaded.bytes()
        );
    }

    private MediaAsset mustGet(String mediaId) {
        return mediaRepository.findByMediaId(normalizeRequired(mediaId, "mediaId"))
                .orElseThrow(() -> new NoSuchElementException("media not found: " + mediaId));
    }

    private MediaAssetDTO toDTO(MediaAsset mediaAsset) {
        return new MediaAssetDTO(
                mediaAsset.getMediaId(),
                mediaAsset.getOwnerUserId(),
                mediaAsset.getMediaType(),
                mediaAsset.getOriginalName(),
                mediaAsset.getMimeType(),
                mediaAsset.getSizeBytes(),
                mediaAsset.getCompressedSizeBytes(),
                mediaAsset.getWidth(),
                mediaAsset.getHeight(),
                "/api/media/" + mediaAsset.getMediaId() + "/content",
                mediaAsset.getCreatedAt()
        );
    }

    private String buildMediaId(Long ownerUserId) {
        return "MED" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_FUND_ACCOUNT,
                MEDIA_ID_BIZ_TYPE,
                String.valueOf(requirePositive(ownerUserId, "ownerUserId"))
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }
}
