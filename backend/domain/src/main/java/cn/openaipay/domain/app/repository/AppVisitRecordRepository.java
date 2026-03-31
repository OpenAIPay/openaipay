package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppVisitRecord;
import java.util.List;

/**
 * App 访问记录仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppVisitRecordRepository {

    /** 保存访问记录。 */
    AppVisitRecord save(AppVisitRecord record);

    /**
     * 按设备ID查询记录列表。
     */
    List<AppVisitRecord> listRecentByDeviceId(String deviceId, int limit);
}
