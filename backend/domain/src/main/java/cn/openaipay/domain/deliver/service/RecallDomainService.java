package cn.openaipay.domain.deliver.service;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 召回领域服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface RecallDomainService {

    /**
     * 处理业务数据。
     */
    List<DeliverCreative> recall(Long positionId, boolean fallback, LocalDateTime now);
}
