package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.PositionUnitCreativeRelationDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * PositionUnitCreativeRelationMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Mapper
public interface PositionUnitCreativeRelationMapper extends BaseMapper<PositionUnitCreativeRelationDO> {

    /**
     * 按广告位ID查找记录。
     */
    default List<PositionUnitCreativeRelationDO> findByPositionId(Long positionId) {
        QueryWrapper<PositionUnitCreativeRelationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("position_id", positionId);
        return selectList(wrapper);
    }

    @Select("""
            SELECT
                c.id AS creative_id,
                c.creative_code AS creative_code,
                c.creative_name AS creative_name,
                c.unit_code AS unit_code,
                c.material_code AS material_code,
                c.landing_url AS landing_url,
                c.schema_json AS schema_json,
                c.priority AS priority,
                c.weight AS weight,
                r.display_order AS display_order,
                c.is_fallback AS fallback,
                c.preview_image AS preview_image,
                c.status AS status,
                c.active_from AS active_from,
                c.active_to AS active_to,
                c.created_at AS created_at,
                c.updated_at AS updated_at
            FROM deliver_position_unit_creative_relation r
            INNER JOIN deliver_unit u ON u.id = r.unit_id
            INNER JOIN deliver_creative c ON c.id = r.creative_id
            WHERE r.position_id = #{positionId}
              AND r.enabled = 1
              AND r.is_fallback = #{fallback}
              AND u.status = 'PUBLISHED'
              AND (u.active_from IS NULL OR u.active_from <= #{now})
              AND (u.active_to IS NULL OR u.active_to >= #{now})
              AND c.status = 'PUBLISHED'
              AND (c.active_from IS NULL OR c.active_from <= #{now})
              AND (c.active_to IS NULL OR c.active_to >= #{now})
            ORDER BY r.display_order ASC, c.priority ASC, c.id ASC
            """)
    List<DeliverCreativeSnapshotRow> selectDeliverCreativeSnapshots(@Param("positionId") Long positionId,
                                                                   @Param("fallback") boolean fallback,
                                                                   @Param("now") LocalDateTime now);
}
