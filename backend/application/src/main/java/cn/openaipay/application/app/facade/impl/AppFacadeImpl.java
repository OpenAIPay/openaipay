package cn.openaipay.application.app.facade.impl;

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
import cn.openaipay.application.app.facade.AppFacade;
import cn.openaipay.application.app.service.AppService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * App 门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class AppFacadeImpl implements AppFacade {

    /** 应用信息 */
    private final AppService appService;

    public AppFacadeImpl(AppService appService) {
        this.appService = appService;
    }

    /**
     * 创建应用信息。
     */
    @Override
    public AppInfoDTO createApp(CreateAppInfoCommand command) {
        return appService.createApp(command);
    }

    /**
     * 查询应用信息列表。
     */
    @Override
    public List<AppInfoDTO> listApps() {
        return appService.listApps();
    }

    /**
     * 更新应用设置信息。
     */
    @Override
    public AppInfoDTO updateAppSettings(UpdateAppSettingsCommand command) {
        return appService.updateAppSettings(command);
    }

    @Override
    public AppDemoProvisioningConfigDTO getDemoProvisioningConfig(String appCode) {
        return appService.getDemoProvisioningConfig(appCode);
    }

    /**
     * 查询版本信息列表。
     */
    @Override
    public List<AppVersionDTO> listVersions(String appCode) {
        return appService.listVersions(appCode);
    }

    /**
     * 创建版本信息。
     */
    @Override
    public AppVersionDTO createVersion(CreateAppVersionCommand command) {
        return appService.createVersion(command);
    }

    /**
     * 更新版本信息。
     */
    @Override
    public AppVersionDTO updateVersion(UpdateAppVersionCommand command) {
        return appService.updateVersion(command);
    }

    /**
     * 处理版本状态。
     */
    @Override
    public AppVersionDTO changeVersionStatus(ChangeAppVersionStatusCommand command) {
        return appService.changeVersionStatus(command);
    }

    /**
     * 提交IOS信息。
     */
    @Override
    public AppVersionDTO submitIosPackageReview(SubmitAppIosPackageReviewCommand command) {
        return appService.submitIosPackageReview(command);
    }

    /**
     * 发布IOS信息。
     */
    @Override
    public AppVersionDTO publishIosPackage(PublishAppIosPackageCommand command) {
        return appService.publishIosPackage(command);
    }

    /**
     * 查询设备信息列表。
     */
    @Override
    public List<AppDeviceDTO> listDevices(String appCode) {
        return appService.listDevices(appCode);
    }

    /**
     * 查询访问记录列表。
     */
    @Override
    public List<AppVisitRecordDTO> listRecentVisitRecords(String deviceId, Integer limit) {
        return appService.listRecentVisitRecords(deviceId, limit);
    }

    /**
     * 保存或更新设备信息。
     */
    @Override
    public AppDeviceDTO upsertDevice(UpsertAppDeviceCommand command) {
        return appService.upsertDevice(command);
    }

    /**
     * 处理登录用户信息。
     */
    @Override
    public AppDeviceDTO bindLoginUser(BindAppDeviceLoginUserCommand command) {
        return appService.bindLoginUser(command);
    }

    /**
     * 查询账号绑定的设备ID。
     */
    @Override
    public String queryBoundDeviceId(Long userId, String loginId) {
        return appService.queryBoundDeviceId(userId, loginId);
    }

    /**
     * 判断是否启用登录本机注册校验。
     */
    @Override
    public boolean isLoginDeviceBindingCheckEnabled(String appCode) {
        return appService.isLoginDeviceBindingCheckEnabled(appCode);
    }

    /**
     * 判断账号是否允许在设备白名单登录。
     */
    @Override
    public boolean isLoginWhitelistAllowed(String appCode, String deviceId, String loginId) {
        return appService.isLoginWhitelistAllowed(appCode, deviceId, loginId);
    }

    /**
     * 判断账号是否配置登录白名单。
     */
    @Override
    public boolean isLoginWhitelistConfigured(String appCode, String loginId) {
        return appService.isLoginWhitelistConfigured(appCode, loginId);
    }

    /**
     * 查询设备登录白名单账号列表。
     */
    @Override
    public List<AppLoginDeviceAccountWhitelistDTO> listLoginWhitelistAccounts(String appCode, String deviceId) {
        return appService.listLoginWhitelistAccounts(appCode, deviceId);
    }

    /**
     * 绑定登录设备白名单账号。
     */
    @Override
    public void bindLoginWhitelistAccount(String appCode, String deviceId, String loginId, String nickname) {
        appService.bindLoginWhitelistAccount(appCode, deviceId, loginId, nickname);
    }

    /**
     * 记录访问信息。
     */
    @Override
    public AppVisitRecordDTO recordVisit(RecordAppVisitCommand command) {
        return appService.recordVisit(command);
    }

    /**
     * 记录行为埋点信息。
     */
    @Override
    public AppBehaviorEventDTO recordBehaviorEvent(RecordAppBehaviorEventCommand command) {
        return appService.recordBehaviorEvent(command);
    }

    /**
     * 查询行为埋点明细。
     */
    @Override
    public List<AppBehaviorEventDTO> listBehaviorEvents(QueryAppBehaviorEventsCommand command) {
        return appService.listBehaviorEvents(command);
    }

    /**
     * 聚合行为埋点统计数据。
     */
    @Override
    public AppBehaviorEventStatsDTO summarizeBehaviorEvents(SummarizeAppBehaviorEventsCommand command) {
        return appService.summarizeBehaviorEvents(command);
    }

    /**
     * 生成行为埋点报表。
     */
    @Override
    public AppBehaviorEventReportDTO generateBehaviorEventReport(GenerateAppBehaviorEventReportCommand command) {
        return appService.generateBehaviorEventReport(command);
    }

    /**
     * 校验版本信息。
     */
    @Override
    public AppVersionCheckDTO checkVersion(CheckAppVersionCommand command) {
        return appService.checkVersion(command);
    }
}
