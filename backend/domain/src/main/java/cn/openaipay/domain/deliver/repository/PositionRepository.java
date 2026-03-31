package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.Position;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 展位仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface PositionRepository {

    /**
     * 按编码查找记录。
     */
    Optional<Position> findPublishedByCode(String positionCode, LocalDateTime now);

    /**
     * 按ID查找广告位信息。
     */
    Optional<Position> findPositionById(Long id);

    /**
     * 查找ALL广告位信息。
     */
    List<Position> findAllPositions();

    /**
     * 保存广告位信息。
     */
    Position savePosition(Position position);
}
