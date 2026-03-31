package cn.openaipay.application.app.facade;

import cn.openaipay.application.app.command.BindAppDeviceLoginUserCommand;
import cn.openaipay.application.app.command.ChangeAppVersionStatusCommand;
import cn.openaipay.application.app.command.CheckAppVersionCommand;
import cn.openaipay.application.app.command.CreateAppInfoCommand;
import cn.openaipay.application.app.command.CreateAppVersionCommand;
import cn.openaipay.application.app.command.GenerateAppBehaviorEventReportCommand;
import cn.openaipay.application.app.command.PublishAppIosPackageCommand;
import cn.openaipay.application.app.command.QueryAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.RecordAppBehaviorEventCommand;
import cn.openaipay.application.app.command.RecordAppVisitCommand;
import cn.openaipay.application.app.command.SummarizeAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.SubmitAppIosPackageReviewCommand;
import cn.openaipay.application.app.command.UpdateAppVersionCommand;
import cn.openaipay.application.app.command.UpdateAppSettingsCommand;
import cn.openaipay.application.app.command.UpsertAppDeviceCommand;
import cn.openaipay.application.app.dto.AppBehaviorEventDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventReportDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventStatsDTO;
import cn.openaipay.application.app.dto.AppDemoProvisioningConfigDTO;
import cn.openaipay.application.app.dto.AppDeviceDTO;
import cn.openaipay.application.app.dto.AppInfoDTO;
import cn.openaipay.application.app.dto.AppLoginDeviceAccountWhitelistDTO;
import cn.openaipay.application.app.dto.AppVersionCheckDTO;
import cn.openaipay.application.app.dto.AppVersionDTO;
import cn.openaipay.application.app.dto.AppVisitRecordDTO;
import java.util.List;

/**
 * App 门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppFacade {

    /**
     * 创建应用信息。
     */
    AppInfoDTO createApp(CreateAppInfoCommand command);

    /**
     * 查询应用信息列表。
     */
    List<AppInfoDTO> listApps();

    /**
     * 更新应用设置信息。
     */
    AppInfoDTO updateAppSettings(UpdateAppSettingsCommand command);

    /**
     * 查询演示账号初始化配置。
     */
    AppDemoProvisioningConfigDTO getDemoProvisioningConfig(String appCode);

    /**
     * 查询版本信息列表。
     */
    List<AppVersionDTO> listVersions(String appCode);

    /**
     * 创建版本信息。
     */
    AppVersionDTO createVersion(CreateAppVersionCommand command);

    /**
     * 更新版本信息。
     */
    AppVersionDTO updateVersion(UpdateAppVersionCommand command);

    /**
     * 处理版本状态。
     */
    AppVersionDTO changeVersionStatus(ChangeAppVersionStatusCommand command);

    /**
     * 提交IOS信息。
     */
    AppVersionDTO submitIosPackageReview(SubmitAppIosPackageReviewCommand command);

    /**
     * 发布IOS信息。
     */
    AppVersionDTO publishIosPackage(PublishAppIosPackageCommand command);

    /**
     * 查询设备信息列表。
     */
    List<AppDeviceDTO> listDevices(String appCode);

    /**
     * 查询访问记录列表。
     */
    List<AppVisitRecordDTO> listRecentVisitRecords(String deviceId, Integer limit);

    /**
     * 保存或更新设备信息。
     */
    AppDeviceDTO upsertDevice(UpsertAppDeviceCommand command);

    /**
     * 处理登录用户信息。
     */
    AppDeviceDTO bindLoginUser(BindAppDeviceLoginUserCommand command);

    /**
     * 查询账号绑定的设备ID。
     */
    String queryBoundDeviceId(Long userId, String loginId);

    /**
     * 判断是否启用登录本机注册校验。
     */
    boolean isLoginDeviceBindingCheckEnabled(String appCode);

    /**
     * 判断账号是否允许在设备白名单登录。
     */
    boolean isLoginWhitelistAllowed(String appCode, String deviceId, String loginId);

    /**
     * 判断账号是否配置登录白名单。
     */
    boolean isLoginWhitelistConfigured(String appCode, String loginId);

    /**
     * 查询设备登录白名单账号列表。
     */
    List<AppLoginDeviceAccountWhitelistDTO> listLoginWhitelistAccounts(String appCode, String deviceId);

    /**
     * 绑定登录设备白名单账号。
     */
    void bindLoginWhitelistAccount(String appCode, String deviceId, String loginId, String nickname);

    /**
     * 记录访问信息。
     */
    AppVisitRecordDTO recordVisit(RecordAppVisitCommand command);

    /**
     * 记录行为埋点信息。
     */
    AppBehaviorEventDTO recordBehaviorEvent(RecordAppBehaviorEventCommand command);

    /**
     * 查询行为埋点明细。
     */
    List<AppBehaviorEventDTO> listBehaviorEvents(QueryAppBehaviorEventsCommand command);

    /**
     * 聚合行为埋点统计数据。
     */
    AppBehaviorEventStatsDTO summarizeBehaviorEvents(SummarizeAppBehaviorEventsCommand command);

    /**
     * 生成行为埋点报表。
     */
    AppBehaviorEventReportDTO generateBehaviorEventReport(GenerateAppBehaviorEventReportCommand command);

    /**
     * 校验版本信息。
     */
    AppVersionCheckDTO checkVersion(CheckAppVersionCommand command);
}
