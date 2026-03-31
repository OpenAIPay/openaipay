package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import java.util.List;
import java.util.Optional;

/**
 * 投放创意仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverCreativeRepository {

    /**
     * 按ID查找创意信息。
     */
    Optional<DeliverCreative> findCreativeById(Long id);

    /**
     * 查找ALL创意信息。
     */
    List<DeliverCreative> findAllCreatives();

    /**
     * 保存创意信息。
     */
    DeliverCreative saveCreative(DeliverCreative creative);

    /**
     * 按ID删除创意信息。
     */
    void deleteCreativeById(Long creativeId);
}
