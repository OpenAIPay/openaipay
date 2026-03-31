package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverMaterial;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 素材仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverMaterialRepository {

    /**
     * 按编码查找记录。
     */
    Map<String, DeliverMaterial> findByCodes(Collection<String> materialCodes);

    /**
     * 按ID查找素材信息。
     */
    Optional<DeliverMaterial> findMaterialById(Long id);

    /**
     * 查找ALL素材信息。
     */
    List<DeliverMaterial> findAllMaterials();

    /**
     * 保存素材信息。
     */
    DeliverMaterial saveMaterial(DeliverMaterial material);
}
