package cn.openaipay.infrastructure.media;

import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.media.repository.MediaRepository;
import cn.openaipay.infrastructure.media.dataobject.MediaAssetDO;
import cn.openaipay.infrastructure.media.mapper.MediaAssetMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 媒体资源仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class MediaRepositoryImpl implements MediaRepository {

    /** 媒体资源信息 */
    private final MediaAssetMapper mediaAssetMapper;

    public MediaRepositoryImpl(MediaAssetMapper mediaAssetMapper) {
        this.mediaAssetMapper = mediaAssetMapper;
    }

    /**
     * 按媒体ID查找记录。
     */
    @Override
    public Optional<MediaAsset> findByMediaId(String mediaId) {
        return mediaAssetMapper.findByMediaId(mediaId).map(this::toDomain);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public MediaAsset save(MediaAsset mediaAsset) {
        MediaAssetDO entity = mediaAssetMapper.findByMediaId(mediaAsset.getMediaId())
                .orElse(new MediaAssetDO());
        fillDO(entity, mediaAsset);
        return toDomain(mediaAssetMapper.save(entity));
    }

    /**
     * 按用户ID查询记录列表。
     */
    @Override
    public List<MediaAsset> listByOwnerUserId(Long ownerUserId, int limit) {
        return mediaAssetMapper.listByOwnerUserId(ownerUserId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MediaAsset toDomain(MediaAssetDO entity) {
        return new MediaAsset(
                entity.getId(),
                entity.getMediaId(),
                entity.getOwnerUserId(),
                entity.getMediaType(),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes() == null ? 0 : entity.getSizeBytes(),
                entity.getCompressedSizeBytes(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getStoragePath(),
                entity.getThumbnailPath(),
                entity.getSha256(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(MediaAssetDO entity, MediaAsset mediaAsset) {
        LocalDateTime now = LocalDateTime.now();
        entity.setMediaId(mediaAsset.getMediaId());
        entity.setOwnerUserId(mediaAsset.getOwnerUserId());
        entity.setMediaType(mediaAsset.getMediaType());
        entity.setOriginalName(mediaAsset.getOriginalName());
        entity.setMimeType(mediaAsset.getMimeType());
        entity.setSizeBytes(mediaAsset.getSizeBytes());
        entity.setCompressedSizeBytes(mediaAsset.getCompressedSizeBytes());
        entity.setWidth(mediaAsset.getWidth());
        entity.setHeight(mediaAsset.getHeight());
        entity.setStoragePath(mediaAsset.getStoragePath());
        entity.setThumbnailPath(mediaAsset.getThumbnailPath());
        entity.setSha256(mediaAsset.getSha256());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(mediaAsset.getCreatedAt() == null ? now : mediaAsset.getCreatedAt());
        }
        entity.setUpdatedAt(mediaAsset.getUpdatedAt() == null ? now : mediaAsset.getUpdatedAt());
    }
}
