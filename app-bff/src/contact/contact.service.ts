import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizeAvatarUrl } from '../common/data-normalizer';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendContactSearch = {
  userId?: string | number;
  aipayUid?: string;
  nickname?: string;
  avatarUrl?: string;
  mobile?: string;
  maskedRealName?: string;
  friend?: boolean | number | string;
  blocked?: boolean | number | string;
  remark?: string;
};

type BackendContactRequest = {
  requestNo?: unknown;
  requesterUserId?: unknown;
  targetUserId?: unknown;
  requesterNickname?: unknown;
  requesterMaskedRealName?: unknown;
  requesterMobileMasked?: unknown;
  requesterAvatarUrl?: unknown;
  applyMessage?: unknown;
  status?: unknown;
  handledByUserId?: unknown;
  handledAt?: unknown;
  createdAt?: unknown;
};

@Injectable()
export class ContactService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async searchContacts(ownerUserId: string, keyword: string, limit: number): Promise<Record<string, unknown>[]> {
    const normalizedOwnerUserId = this.assertRequiredUserId(ownerUserId, 'ownerUserId');
    const normalizedKeyword = this.assertKeyword(keyword);
    const safeLimit = this.normalizeLimit(limit, 100, 30);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendContactSearch[]>>(
        `/api/contacts/search?ownerUserId=${encodeURIComponent(normalizedOwnerUserId)}&keyword=${encodeURIComponent(normalizedKeyword)}&limit=${safeLimit}`,
      );
      const contacts = unwrapBackendData(response, '上游服务未返回联系人搜索结果');
      if (!Array.isArray(contacts)) {
        return [];
      }
      return contacts
        .map((item) => this.normalizeSearchItem(item))
        .filter((item): item is Record<string, unknown> => item !== null);
    } catch (error) {
      rethrowMappedUpstreamError(error, '联系人搜索失败');
    }
  }

  async applyFriendRequest(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requesterUserId = this.assertRequiredUserId(payload.requesterUserId, 'requesterUserId');
    const targetUserId = this.assertRequiredUserId(payload.targetUserId, 'targetUserId');
    const upstreamPayload = this.buildApplyRequestPayload(payload, {
      requesterUserId,
      targetUserId,
    });

    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendContactRequest>, Record<string, unknown>>(
        '/api/contacts/requests',
        upstreamPayload,
      );
      const request = unwrapBackendData(response, '上游服务未返回好友申请结果');
      return this.normalizeContactRequest(request, {
        requesterUserId,
        targetUserId,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '发送好友申请失败');
    }
  }

  async listReceivedFriendRequests(targetUserId: string, limit: number): Promise<Record<string, unknown>[]> {
    const normalizedTargetUserId = this.assertRequiredUserId(targetUserId, 'targetUserId');
    const safeLimit = this.normalizeLimit(limit, 100, 20);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendContactRequest[]>>(
        `/api/contacts/requests/received?targetUserId=${encodeURIComponent(normalizedTargetUserId)}&limit=${safeLimit}`,
      );
      const requests = unwrapBackendData(response, '上游服务未返回好友申请列表');
      if (!Array.isArray(requests)) {
        return [];
      }
      return requests.map((item) =>
        this.normalizeContactRequest(item, {
          targetUserId: normalizedTargetUserId,
        }),
      );
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询好友申请失败');
    }
  }

  async listSentFriendRequests(requesterUserId: string, limit: number): Promise<Record<string, unknown>[]> {
    const normalizedRequesterUserId = this.assertRequiredUserId(requesterUserId, 'requesterUserId');
    const safeLimit = this.normalizeLimit(limit, 100, 20);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendContactRequest[]>>(
        `/api/contacts/requests/sent?requesterUserId=${encodeURIComponent(normalizedRequesterUserId)}&limit=${safeLimit}`,
      );
      const requests = unwrapBackendData(response, '上游服务未返回好友申请列表');
      if (!Array.isArray(requests)) {
        return [];
      }
      return requests.map((item) =>
        this.normalizeContactRequest(item, {
          requesterUserId: normalizedRequesterUserId,
        }),
      );
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询好友申请失败');
    }
  }

  async handleFriendRequest(requestNo: string, payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const normalizedRequestNo = this.assertRequestNo(requestNo);
    const operatorUserId = this.assertRequiredUserId(payload.operatorUserId, 'operatorUserId');
    const action = this.assertHandleAction(payload.action);

    const upstreamPayload = this.buildHandleRequestPayload(payload, {
      operatorUserId,
      action,
    });

    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendContactRequest>, Record<string, unknown>>(
        `/api/contacts/requests/${encodeURIComponent(normalizedRequestNo)}/handle`,
        upstreamPayload,
      );
      const request = unwrapBackendData(response, '上游服务未返回好友申请处理结果');
      return this.normalizeContactRequest(request, {
        requestNo: normalizedRequestNo,
        handledByUserId: operatorUserId,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '处理好友申请失败');
    }
  }

  private normalizeSearchItem(raw: BackendContactSearch): Record<string, unknown> | null {
    const userId = normalizePositiveLongId(raw.userId, { minDigits: 1, maxDigits: 20 });
    if (!userId) {
      return null;
    }
    return {
      userId,
      aipayUid: this.normalizeOptionalText(raw.aipayUid),
      nickname: this.normalizeOptionalText(raw.nickname),
      avatarUrl: normalizeAvatarUrl(this.backendHttpService, this.normalizeOptionalText(raw.avatarUrl)),
      mobile: this.normalizeOptionalText(raw.mobile),
      maskedRealName: this.normalizeOptionalText(raw.maskedRealName),
      friend: this.normalizeBoolean(raw.friend, false),
      blocked: this.normalizeBoolean(raw.blocked, false),
      remark: this.normalizeOptionalText(raw.remark),
    };
  }

  private normalizeContactRequest(
    value: BackendContactRequest,
    fallback: {
      requestNo?: string;
      requesterUserId?: string;
      targetUserId?: string;
      handledByUserId?: string;
    },
  ): Record<string, unknown> {
    const requesterUserId = normalizePositiveLongId(value?.requesterUserId, { minDigits: 1, maxDigits: 20 })
      ?? fallback.requesterUserId
      ?? fallback.targetUserId
      ?? '0';
    const targetUserId = normalizePositiveLongId(value?.targetUserId, { minDigits: 1, maxDigits: 20 })
      ?? fallback.targetUserId
      ?? requesterUserId;
    const handledByUserId = normalizePositiveLongId(value?.handledByUserId, { minDigits: 1, maxDigits: 20 })
      ?? fallback.handledByUserId;
    return {
      requestNo: this.normalizeOptionalText(value?.requestNo) ?? fallback.requestNo ?? '',
      requesterUserId,
      targetUserId,
      requesterNickname: this.normalizeOptionalText(value?.requesterNickname),
      requesterMaskedRealName: this.normalizeOptionalText(value?.requesterMaskedRealName),
      requesterMobileMasked: this.normalizeOptionalText(value?.requesterMobileMasked),
      requesterAvatarUrl: normalizeAvatarUrl(this.backendHttpService, this.normalizeOptionalText(value?.requesterAvatarUrl)),
      applyMessage: this.normalizeOptionalText(value?.applyMessage),
      status: this.normalizeOptionalText(value?.status) ?? 'PENDING',
      handledByUserId,
      handledAt: this.normalizeOptionalText(value?.handledAt),
      createdAt: this.normalizeOptionalText(value?.createdAt),
    };
  }

  private buildApplyRequestPayload(
    payload: Record<string, unknown>,
    required: {
      requesterUserId: string;
      targetUserId: string;
    },
  ): Record<string, unknown> {
    const upstreamPayload: Record<string, unknown> = {
      requesterUserId: required.requesterUserId,
      targetUserId: required.targetUserId,
    };
    const applyMessage = this.normalizeOptionalText(payload.applyMessage);
    if (applyMessage) {
      upstreamPayload.applyMessage = applyMessage;
    }
    return upstreamPayload;
  }

  private buildHandleRequestPayload(
    payload: Record<string, unknown>,
    required: {
      operatorUserId: string;
      action: 'ACCEPT' | 'REJECT';
    },
  ): Record<string, unknown> {
    const upstreamPayload: Record<string, unknown> = {
      operatorUserId: required.operatorUserId,
      action: required.action,
    };
    const applyMessage = this.normalizeOptionalText(payload.applyMessage);
    if (applyMessage) {
      upstreamPayload.applyMessage = applyMessage;
    }
    return upstreamPayload;
  }

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private normalizeBoolean(value: unknown, fallback: boolean): boolean {
    if (typeof value === 'boolean') {
      return value;
    }
    if (typeof value === 'number') {
      return value !== 0;
    }
    if (typeof value === 'string') {
      const normalized = value.trim().toLowerCase();
      if (normalized === 'true' || normalized === '1') {
        return true;
      }
      if (normalized === 'false' || normalized === '0') {
        return false;
      }
    }
    return fallback;
  }

  private assertKeyword(value: unknown): string {
    if (typeof value === 'number' && Number.isFinite(value)) {
      const normalizedNumberText = String(Math.trunc(value));
      if (normalizedNumberText.length > 0) {
        return normalizedNumberText;
      }
    }
    if (typeof value !== 'string') {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'keyword 参数不能为空',
      });
    }
    const normalized = value.trim();
    if (normalized.length > 0) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'keyword 参数不能为空',
    });
  }

  private assertRequiredUserId(value: unknown, field: string): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 参数格式不正确，请使用字符串传递18位ID`,
    });
  }

  private assertRequestNo(value: unknown): string {
    if (typeof value === 'string') {
      const normalized = value.trim();
      if (normalized.length > 0) {
        return normalized;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'requestNo 参数不能为空',
    });
  }

  private assertHandleAction(value: unknown): 'ACCEPT' | 'REJECT' {
    if (typeof value === 'string') {
      const normalized = value.trim().toUpperCase();
      if (normalized === 'ACCEPT' || normalized === 'REJECT') {
        return normalized;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'action 参数必须为 ACCEPT 或 REJECT',
    });
  }

  private normalizeLimit(value: unknown, max: number, fallback: number): number {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      return fallback;
    }
    const normalized = Math.trunc(value);
    if (normalized <= 0) {
      return fallback;
    }
    return Math.min(normalized, max);
  }
}
