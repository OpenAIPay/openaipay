package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppBehaviorEvent;
import cn.openaipay.domain.app.model.AppBehaviorEventQuery;
import cn.openaipay.domain.app.model.AppBehaviorEventReportRow;
import cn.openaipay.domain.app.model.AppBehaviorEventStats;
import java.util.List;

/**
 * App 行为埋点仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public interface AppBehaviorEventRepository {

    /** 保存行为埋点。 */
    AppBehaviorEvent save(AppBehaviorEvent event);

    /**
     * 按条件查询埋点明细。
     */
    List<AppBehaviorEvent> listByQuery(AppBehaviorEventQuery query);

    /**
     * 按条件聚合埋点统计数据。
     */
    AppBehaviorEventStats summarize(AppBehaviorEventQuery query, int topLimit);

    /**
     * 按条件生成埋点报表行。
     */
    List<AppBehaviorEventReportRow> listReportRows(AppBehaviorEventQuery query);
}
