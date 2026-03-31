package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverUnit;
import java.util.List;
import java.util.Optional;

/**
 * 投放单元仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverUnitRepository {

    /**
     * 按ID查找单元信息。
     */
    Optional<DeliverUnit> findUnitById(Long id);

    /**
     * 查找ALL单元信息。
     */
    List<DeliverUnit> findAllUnits();

    /**
     * 保存单元信息。
     */
    DeliverUnit saveUnit(DeliverUnit unit);
}
