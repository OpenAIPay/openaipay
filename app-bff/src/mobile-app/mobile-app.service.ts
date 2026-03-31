import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

const APP_VISIT_API_NAME_MAX_LENGTH = 1024;
const APP_BEHAVIOR_EVENT_NAME_MAX_LENGTH = 128;

type BackendAppVersionCheck = {
  appCode?: unknown;
  currentVersionNo?: unknown;
  versionPromptEnabled?: unknown;
  demoAutoLoginEnabled?: unknown;
  latestVersionCode?: unknown;
  latestVersionNo?: unknown;
  updateAvailable?: unknown;
  forceUpdate?: unknown;
  updateType?: unknown;
  updatePromptFrequency?: unknown;
  versionDescription?: unknown;
  minSupportedVersionNo?: unknown;
  appStoreUrl?: unknown;
  packageSizeBytes?: unknown;
  md5?: unknown;
  releaseStatus?: unknown;
};

type BackendAppDeviceReport = {
  deviceId?: unknown;
  appCode?: unknown;
  clientIds?: unknown;
  status?: unknown;
  installedAt?: unknown;
  startedAt?: unknown;
  lastOpenedAt?: unknown;
  currentAppVersionId?: unknown;
  currentVersionCode?: unknown;
  currentVersionNo?: unknown;
  currentIosPackageId?: unknown;
  currentIosCode?: unknown;
  appUpdatedAt?: unknown;
  deviceBrand?: unknown;
  osVersion?: unknown;
};

type BackendAppVisitRecord = {
  id?: unknown;
  deviceId?: unknown;
  appCode?: unknown;
  clientId?: unknown;
  ipAddress?: unknown;
  locationInfo?: unknown;
  tenantCode?: unknown;
  clientType?: unknown;
  networkType?: unknown;
  appVersionId?: unknown;
  deviceBrand?: unknown;
  osVersion?: unknown;
  apiName?: unknown;
  requestParamsText?: unknown;
  calledAt?: unknown;
  resultSummary?: unknown;
  durationMs?: unknown;
};

type BackendAppBehaviorEvent = {
  id?: unknown;
  eventId?: unknown;
  sessionId?: unknown;
  appCode?: unknown;
  eventName?: unknown;
  eventType?: unknown;
  eventCode?: unknown;
  pageName?: unknown;
  actionName?: unknown;
  resultStatus?: unknown;
  traceId?: unknown;
  deviceId?: unknown;
  clientId?: unknown;
  userId?: unknown;
  aipayUid?: unknown;
  loginId?: unknown;
  accountStatus?: unknown;
  kycLevel?: unknown;
  nickname?: unknown;
  mobile?: unknown;
  ipAddress?: unknown;
  locationInfo?: unknown;
  tenantCode?: unknown;
  networkType?: unknown;
  appVersionNo?: unknown;
  appBuildNo?: unknown;
  deviceBrand?: unknown;
  deviceModel?: unknown;
  deviceName?: unknown;
  deviceType?: unknown;
  osName?: unknown;
  osVersion?: unknown;
  locale?: unknown;
  timezone?: unknown;
  language?: unknown;
  countryCode?: unknown;
  carrierName?: unknown;
  screenWidth?: unknown;
  screenHeight?: unknown;
  viewportWidth?: unknown;
  viewportHeight?: unknown;
  durationMs?: unknown;
  loginDurationMs?: unknown;
  eventOccurredAt?: unknown;
  payloadJson?: unknown;
  createdAt?: unknown;
};

@Injectable()
export class MobileAppService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async checkVersion(appCode: string, currentVersionNo?: string, deviceId?: string): Promise<Record<string, unknown>> {
    const query: string[] = [];
    if (typeof currentVersionNo === 'string' && currentVersionNo.trim().length > 0) {
      query.push(`currentVersionNo=${encodeURIComponent(currentVersionNo.trim())}`);
    }
    if (typeof deviceId === 'string' && deviceId.trim().length > 0) {
      query.push(`deviceId=${encodeURIComponent(deviceId.trim())}`);
    }
    const suffix = query.length > 0 ? `?${query.join('&')}` : '';
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendAppVersionCheck>>(
        `/api/apps/${appCode}/versions/check${suffix}`,
      );
      const payload = unwrapBackendData(response, '上游服务未返回版本检查结果');
      return this.normalizeVersionCheck(payload, appCode);
    } catch (error) {
      rethrowMappedUpstreamError(error, '版本检查失败');
    }
  }

  async upsertDevice(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestPayload = this.buildDeviceUpsertPayload(payload);
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendAppDeviceReport>, Record<string, unknown>>(
        '/api/apps/devices',
        requestPayload,
      );
      const report = unwrapBackendData(response, '上游服务未返回设备上报结果');
      return this.normalizeDeviceReport(report, requestPayload);
    } catch (error) {
      rethrowMappedUpstreamError(error, '设备上报失败');
    }
  }

  async recordVisit(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestPayload = this.buildVisitRecordPayload(payload);
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendAppVisitRecord>, Record<string, unknown>>(
        '/api/apps/visit-records',
        requestPayload,
      );
      const record = unwrapBackendData(response, '上游服务未返回访问记录结果');
      return this.normalizeVisitRecord(record, requestPayload);
    } catch (error) {
      rethrowMappedUpstreamError(error, '访问记录上报失败');
    }
  }

  async recordBehaviorEvent(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestPayload = this.buildBehaviorEventPayload(payload);
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendAppBehaviorEvent>, Record<string, unknown>>(
        '/api/apps/behavior-events',
        requestPayload,
      );
      const event = unwrapBackendData(response, '上游服务未返回埋点日志结果');
      return this.normalizeBehaviorEvent(event, requestPayload);
    } catch (error) {
      rethrowMappedUpstreamError(error, '行为埋点上报失败');
    }
  }

  private buildDeviceUpsertPayload(payload: Record<string, unknown>): Record<string, unknown> {
    const deviceId = this.assertRequiredString(payload.deviceId, 'deviceId', 64);
    const appCode = this.assertRequiredString(payload.appCode, 'appCode', 64);
    const clientIds = this.normalizeRequiredStringArray(payload.clientIds, 'clientIds', 20, 64);

    const requestPayload: Record<string, unknown> = {
      deviceId,
      appCode,
      clientIds,
    };
    this.assignOptionalText(requestPayload, 'deviceBrand', payload.deviceBrand, 64);
    this.assignOptionalText(requestPayload, 'osVersion', payload.osVersion, 64);
    this.assignOptionalText(requestPayload, 'currentVersionCode', payload.currentVersionCode, 64);
    this.assignOptionalText(requestPayload, 'currentVersionNo', payload.currentVersionNo, 64);
    if (typeof payload.started === 'boolean') {
      requestPayload.started = payload.started;
    }
    return requestPayload;
  }

  private buildVisitRecordPayload(payload: Record<string, unknown>): Record<string, unknown> {
    const deviceId = this.assertRequiredString(payload.deviceId, 'deviceId', 64);
    const appCode = this.assertRequiredString(payload.appCode, 'appCode', 64);
    const apiName = this.assertRequiredString(payload.apiName, 'apiName', APP_VISIT_API_NAME_MAX_LENGTH);

    const requestPayload: Record<string, unknown> = {
      deviceId,
      appCode,
      apiName,
    };

    this.assignOptionalText(requestPayload, 'clientId', payload.clientId, 64);
    this.assignOptionalText(requestPayload, 'ipAddress', payload.ipAddress, 64);
    this.assignOptionalText(requestPayload, 'locationInfo', payload.locationInfo, 255);
    this.assignOptionalText(requestPayload, 'tenantCode', payload.tenantCode, 64);
    this.assignOptionalText(requestPayload, 'networkType', payload.networkType, 32);
    this.assignOptionalText(requestPayload, 'currentVersionCode', payload.currentVersionCode, 64);
    this.assignOptionalText(requestPayload, 'currentVersionNo', payload.currentVersionNo, 64);
    this.assignOptionalText(requestPayload, 'deviceBrand', payload.deviceBrand, 64);
    this.assignOptionalText(requestPayload, 'osVersion', payload.osVersion, 64);
    this.assignOptionalText(requestPayload, 'requestParamsText', payload.requestParamsText, 5000);
    this.assignOptionalText(requestPayload, 'resultSummary', payload.resultSummary, 1000);

    if (typeof payload.durationMs === 'number' && Number.isFinite(payload.durationMs)) {
      requestPayload.durationMs = Math.max(Math.trunc(payload.durationMs), 0);
    }

    return requestPayload;
  }

  private buildBehaviorEventPayload(payload: Record<string, unknown>): Record<string, unknown> {
    const deviceId = this.assertRequiredString(payload.deviceId, 'deviceId', 64);
    const appCode = this.assertRequiredString(payload.appCode, 'appCode', 64);
    const eventName = this.assertRequiredString(payload.eventName, 'eventName', APP_BEHAVIOR_EVENT_NAME_MAX_LENGTH);

    const requestPayload: Record<string, unknown> = {
      deviceId,
      appCode,
      eventName,
    };

    this.assignOptionalText(requestPayload, 'eventId', payload.eventId, 64);
    this.assignOptionalText(requestPayload, 'sessionId', payload.sessionId, 64);
    this.assignOptionalText(requestPayload, 'eventType', payload.eventType, 32);
    this.assignOptionalText(requestPayload, 'eventCode', payload.eventCode, 128);
    this.assignOptionalText(requestPayload, 'pageName', payload.pageName, 128);
    this.assignOptionalText(requestPayload, 'actionName', payload.actionName, 128);
    this.assignOptionalText(requestPayload, 'resultStatus', payload.resultStatus, 64);
    this.assignOptionalText(requestPayload, 'traceId', payload.traceId, 64);
    this.assignOptionalText(requestPayload, 'clientId', payload.clientId, 64);
    this.assignOptionalText(requestPayload, 'aipayUid', payload.aipayUid, 64);
    this.assignOptionalText(requestPayload, 'loginId', payload.loginId, 64);
    this.assignOptionalText(requestPayload, 'accountStatus', payload.accountStatus, 32);
    this.assignOptionalText(requestPayload, 'kycLevel', payload.kycLevel, 32);
    this.assignOptionalText(requestPayload, 'nickname', payload.nickname, 128);
    this.assignOptionalText(requestPayload, 'mobile', payload.mobile, 32);
    this.assignOptionalText(requestPayload, 'ipAddress', payload.ipAddress, 64);
    this.assignOptionalText(requestPayload, 'locationInfo', payload.locationInfo, 255);
    this.assignOptionalText(requestPayload, 'tenantCode', payload.tenantCode, 64);
    this.assignOptionalText(requestPayload, 'networkType', payload.networkType, 32);
    this.assignOptionalText(requestPayload, 'appVersionNo', payload.appVersionNo, 64);
    this.assignOptionalText(requestPayload, 'appBuildNo', payload.appBuildNo, 32);
    this.assignOptionalText(requestPayload, 'deviceBrand', payload.deviceBrand, 64);
    this.assignOptionalText(requestPayload, 'deviceModel', payload.deviceModel, 64);
    this.assignOptionalText(requestPayload, 'deviceName', payload.deviceName, 128);
    this.assignOptionalText(requestPayload, 'deviceType', payload.deviceType, 32);
    this.assignOptionalText(requestPayload, 'osName', payload.osName, 64);
    this.assignOptionalText(requestPayload, 'osVersion', payload.osVersion, 64);
    this.assignOptionalText(requestPayload, 'locale', payload.locale, 64);
    this.assignOptionalText(requestPayload, 'timezone', payload.timezone, 64);
    this.assignOptionalText(requestPayload, 'language', payload.language, 64);
    this.assignOptionalText(requestPayload, 'countryCode', payload.countryCode, 16);
    this.assignOptionalText(requestPayload, 'carrierName', payload.carrierName, 64);
    this.assignOptionalText(requestPayload, 'payloadJson', payload.payloadJson, 20000);
    this.assignOptionalPositiveInteger(requestPayload, 'userId', payload.userId);
    this.assignOptionalNonNegativeInteger(requestPayload, 'screenWidth', payload.screenWidth);
    this.assignOptionalNonNegativeInteger(requestPayload, 'screenHeight', payload.screenHeight);
    this.assignOptionalNonNegativeInteger(requestPayload, 'viewportWidth', payload.viewportWidth);
    this.assignOptionalNonNegativeInteger(requestPayload, 'viewportHeight', payload.viewportHeight);
    this.assignOptionalNonNegativeInteger(requestPayload, 'durationMs', payload.durationMs);
    this.assignOptionalNonNegativeInteger(requestPayload, 'loginDurationMs', payload.loginDurationMs);
    this.assignOptionalNonNegativeInteger(requestPayload, 'eventAtEpochMs', payload.eventAtEpochMs);
    return requestPayload;
  }

  private normalizeVersionCheck(payload: BackendAppVersionCheck, requestedAppCode: string): Record<string, unknown> {
    return {
      appCode: this.normalizeOptionalText(payload?.appCode, 64) ?? requestedAppCode,
      currentVersionNo: this.normalizeOptionalText(payload?.currentVersionNo, 64),
      versionPromptEnabled: this.normalizeBoolean(payload?.versionPromptEnabled),
      demoAutoLoginEnabled: this.normalizeBooleanWithDefault(payload?.demoAutoLoginEnabled, true),
      latestVersionCode: this.normalizeOptionalText(payload?.latestVersionCode, 64),
      latestVersionNo: this.normalizeOptionalText(payload?.latestVersionNo, 64),
      updateAvailable: this.normalizeBoolean(payload?.updateAvailable),
      forceUpdate: this.normalizeBoolean(payload?.forceUpdate),
      updateType: this.normalizeOptionalText(payload?.updateType, 64),
      updatePromptFrequency: this.normalizeOptionalText(payload?.updatePromptFrequency, 64),
      versionDescription: this.normalizeOptionalText(payload?.versionDescription, 500),
      minSupportedVersionNo: this.normalizeOptionalText(payload?.minSupportedVersionNo, 64),
      appStoreUrl: this.normalizeOptionalText(payload?.appStoreUrl, 512),
      packageSizeBytes: this.normalizeOptionalInteger(payload?.packageSizeBytes),
      md5: this.normalizeOptionalText(payload?.md5, 128),
      releaseStatus: this.normalizeOptionalText(payload?.releaseStatus, 64),
    };
  }

  private normalizeDeviceReport(
    payload: BackendAppDeviceReport,
    requestPayload: Record<string, unknown>,
  ): Record<string, unknown> {
    return {
      deviceId: this.normalizeOptionalText(payload?.deviceId, 64) ?? (requestPayload.deviceId as string),
      appCode: this.normalizeOptionalText(payload?.appCode, 64) ?? (requestPayload.appCode as string),
      clientIds: this.normalizeStringArray(payload?.clientIds, 20, 64) ?? (requestPayload.clientIds as string[]),
      status: this.normalizeOptionalText(payload?.status, 32) ?? 'ACTIVE',
      installedAt: this.normalizeOptionalText(payload?.installedAt, 64),
      startedAt: this.normalizeOptionalText(payload?.startedAt, 64),
      lastOpenedAt: this.normalizeOptionalText(payload?.lastOpenedAt, 64),
      currentAppVersionId: this.normalizeOptionalInteger(payload?.currentAppVersionId),
      currentVersionCode: this.normalizeOptionalText(payload?.currentVersionCode, 64),
      currentVersionNo: this.normalizeOptionalText(payload?.currentVersionNo, 64),
      currentIosPackageId: this.normalizeOptionalInteger(payload?.currentIosPackageId),
      currentIosCode: this.normalizeOptionalText(payload?.currentIosCode, 64),
      appUpdatedAt: this.normalizeOptionalText(payload?.appUpdatedAt, 64),
      deviceBrand: this.normalizeOptionalText(payload?.deviceBrand, 64),
      osVersion: this.normalizeOptionalText(payload?.osVersion, 64),
    };
  }

  private normalizeVisitRecord(
    payload: BackendAppVisitRecord,
    requestPayload: Record<string, unknown>,
  ): Record<string, unknown> {
    return {
      id: this.normalizeOptionalInteger(payload?.id),
      deviceId: this.normalizeOptionalText(payload?.deviceId, 64) ?? (requestPayload.deviceId as string),
      appCode: this.normalizeOptionalText(payload?.appCode, 64) ?? (requestPayload.appCode as string),
      clientId: this.normalizeOptionalText(payload?.clientId, 64),
      ipAddress: this.normalizeOptionalText(payload?.ipAddress, 64),
      locationInfo: this.normalizeOptionalText(payload?.locationInfo, 255),
      tenantCode: this.normalizeOptionalText(payload?.tenantCode, 64),
      clientType: this.normalizeOptionalText(payload?.clientType, 32) ?? 'IOS_APP',
      networkType: this.normalizeOptionalText(payload?.networkType, 32),
      appVersionId: this.normalizeOptionalInteger(payload?.appVersionId),
      deviceBrand: this.normalizeOptionalText(payload?.deviceBrand, 64),
      osVersion: this.normalizeOptionalText(payload?.osVersion, 64),
      apiName:
        this.normalizeOptionalText(payload?.apiName, APP_VISIT_API_NAME_MAX_LENGTH) ??
        (requestPayload.apiName as string),
      requestParamsText: this.normalizeOptionalText(payload?.requestParamsText, 5000),
      calledAt: this.normalizeOptionalText(payload?.calledAt, 64),
      resultSummary: this.normalizeOptionalText(payload?.resultSummary, 1000),
      durationMs: this.normalizeOptionalInteger(payload?.durationMs),
    };
  }

  private normalizeBehaviorEvent(
    payload: BackendAppBehaviorEvent,
    requestPayload: Record<string, unknown>,
  ): Record<string, unknown> {
    return {
      id: this.normalizeOptionalInteger(payload?.id),
      eventId: this.normalizeOptionalText(payload?.eventId, 64) ?? (requestPayload.eventId as string | undefined),
      sessionId: this.normalizeOptionalText(payload?.sessionId, 64),
      appCode: this.normalizeOptionalText(payload?.appCode, 64) ?? (requestPayload.appCode as string),
      eventName:
        this.normalizeOptionalText(payload?.eventName, APP_BEHAVIOR_EVENT_NAME_MAX_LENGTH) ??
        (requestPayload.eventName as string),
      eventType: this.normalizeOptionalText(payload?.eventType, 32),
      eventCode: this.normalizeOptionalText(payload?.eventCode, 128),
      pageName: this.normalizeOptionalText(payload?.pageName, 128),
      actionName: this.normalizeOptionalText(payload?.actionName, 128),
      resultStatus: this.normalizeOptionalText(payload?.resultStatus, 64),
      traceId: this.normalizeOptionalText(payload?.traceId, 64),
      deviceId: this.normalizeOptionalText(payload?.deviceId, 64) ?? (requestPayload.deviceId as string),
      clientId: this.normalizeOptionalText(payload?.clientId, 64),
      userId: this.normalizeOptionalInteger(payload?.userId),
      aipayUid: this.normalizeOptionalText(payload?.aipayUid, 64),
      loginId: this.normalizeOptionalText(payload?.loginId, 64),
      accountStatus: this.normalizeOptionalText(payload?.accountStatus, 32),
      kycLevel: this.normalizeOptionalText(payload?.kycLevel, 32),
      nickname: this.normalizeOptionalText(payload?.nickname, 128),
      mobile: this.normalizeOptionalText(payload?.mobile, 32),
      ipAddress: this.normalizeOptionalText(payload?.ipAddress, 64),
      locationInfo: this.normalizeOptionalText(payload?.locationInfo, 255),
      tenantCode: this.normalizeOptionalText(payload?.tenantCode, 64),
      networkType: this.normalizeOptionalText(payload?.networkType, 32),
      appVersionNo: this.normalizeOptionalText(payload?.appVersionNo, 64),
      appBuildNo: this.normalizeOptionalText(payload?.appBuildNo, 32),
      deviceBrand: this.normalizeOptionalText(payload?.deviceBrand, 64),
      deviceModel: this.normalizeOptionalText(payload?.deviceModel, 64),
      deviceName: this.normalizeOptionalText(payload?.deviceName, 128),
      deviceType: this.normalizeOptionalText(payload?.deviceType, 32),
      osName: this.normalizeOptionalText(payload?.osName, 64),
      osVersion: this.normalizeOptionalText(payload?.osVersion, 64),
      locale: this.normalizeOptionalText(payload?.locale, 64),
      timezone: this.normalizeOptionalText(payload?.timezone, 64),
      language: this.normalizeOptionalText(payload?.language, 64),
      countryCode: this.normalizeOptionalText(payload?.countryCode, 16),
      carrierName: this.normalizeOptionalText(payload?.carrierName, 64),
      screenWidth: this.normalizeOptionalInteger(payload?.screenWidth),
      screenHeight: this.normalizeOptionalInteger(payload?.screenHeight),
      viewportWidth: this.normalizeOptionalInteger(payload?.viewportWidth),
      viewportHeight: this.normalizeOptionalInteger(payload?.viewportHeight),
      durationMs: this.normalizeOptionalInteger(payload?.durationMs),
      loginDurationMs: this.normalizeOptionalInteger(payload?.loginDurationMs),
      eventOccurredAt: this.normalizeOptionalText(payload?.eventOccurredAt, 64),
      payloadJson: this.normalizeOptionalText(payload?.payloadJson, 20000),
      createdAt: this.normalizeOptionalText(payload?.createdAt, 64),
    };
  }

  private assertRequiredString(value: unknown, field: string, maxLength: number): string {
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim().slice(0, maxLength);
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private normalizeRequiredStringArray(
    value: unknown,
    field: string,
    maxItems: number,
    maxTextLength: number,
  ): string[] {
    const normalized = this.normalizeStringArray(value, maxItems, maxTextLength) ?? [];
    if (normalized.length === 0) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: `${field} 不能为空`,
      });
    }
    return normalized;
  }

  private normalizeStringArray(value: unknown, maxItems: number, maxTextLength: number): string[] | undefined {
    if (!Array.isArray(value)) {
      return undefined;
    }
    const normalized = value
      .filter((item): item is string => typeof item === 'string')
      .map((item) => item.trim())
      .filter((item) => item.length > 0)
      .slice(0, maxItems)
      .map((item) => item.slice(0, maxTextLength));
    return normalized.length > 0 ? normalized : [];
  }

  private normalizeOptionalText(value: unknown, maxLength: number): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    if (!normalized) {
      return undefined;
    }
    return normalized.slice(0, maxLength);
  }

  private normalizeBoolean(value: unknown): boolean {
    if (typeof value === 'boolean') {
      return value;
    }
    if (typeof value === 'number') {
      return value !== 0;
    }
    if (typeof value === 'string') {
      const normalized = value.trim().toLowerCase();
      return normalized === 'true' || normalized === '1' || normalized === 'yes';
    }
    return false;
  }

  private normalizeBooleanWithDefault(value: unknown, defaultValue: boolean): boolean {
    if (value === undefined || value === null) {
      return defaultValue;
    }
    return this.normalizeBoolean(value);
  }

  private normalizeOptionalInteger(value: unknown): number | undefined {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return Math.trunc(value);
    }
    if (typeof value === 'string' && /^-?\d+$/.test(value.trim())) {
      return Number.parseInt(value.trim(), 10);
    }
    return undefined;
  }

  private assignOptionalText(target: Record<string, unknown>, field: string, value: unknown, maxLength: number): void {
    const normalized = this.normalizeOptionalText(value, maxLength);
    if (normalized !== undefined) {
      target[field] = normalized;
    }
  }

  private assignOptionalPositiveInteger(target: Record<string, unknown>, field: string, value: unknown): void {
    const normalized = this.normalizeOptionalInteger(value);
    if (normalized !== undefined && normalized > 0) {
      target[field] = normalized;
    }
  }

  private assignOptionalNonNegativeInteger(target: Record<string, unknown>, field: string, value: unknown): void {
    const normalized = this.normalizeOptionalInteger(value);
    if (normalized !== undefined && normalized >= 0) {
      target[field] = normalized;
    }
  }
}
