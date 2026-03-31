import { BadGatewayException, BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { AxiosError } from 'axios';
import { BackendHttpService } from '../common/backend-http.service';
import {
  firstNonEmpty,
  normalizeAvatarUrl,
  normalizeFirstAvailableUserId,
  normalizeOptionalUserId,
  normalizeUserId,
} from '../common/data-normalizer';

type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: {
    code?: string;
    message?: string;
  };
};

type BackendUserProfile = {
  userId?: string | number;
  aipayUid?: string;
  loginId?: string;
  accountStatus?: string;
  kycLevel?: string;
  nickname?: string;
  avatarUrl?: string;
  countryCode?: string;
  mobile?: string;
  maskedRealName?: string;
  idCardNo?: string;
  gender?: string;
  region?: string;
  birthday?: string;
};

type BackendRecentContact = {
  ownerUserId?: string | number;
  contactUserId?: string | number;
  contactAipayUid?: string;
  contactNickname?: string;
  contactDisplayName?: string;
  contactMaskedRealName?: string;
  contactAvatarUrl?: string;
  contactMobileMasked?: string;
  interactionSceneCode?: string;
  interactionRemark?: string;
  interactionCount?: string | number;
  lastInteractionAt?: string;
};

type BackendContactFriend = {
  friendUserId?: string | number;
  aipayUid?: string;
  nickname?: string;
  maskedRealName?: string;
  mobileMasked?: string;
  avatarUrl?: string;
  remark?: string;
  createdAt?: string;
};

type BackendRegisterCheck = {
  userExists?: boolean;
  realNameVerified?: boolean;
  kycLevel?: string;
};

@Injectable()
export class UserService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getProfile(userId: string): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendUserProfile>>(`/api/users/${userId}/profile`);
      const profile = response.data?.data;
      if (!profile) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回用户资料',
        });
      }
      return this.normalizeProfile(profile, userId);
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询用户资料失败');
    }
  }

  async getProfileByLoginId(loginId: string): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendUserProfile>>(
        `/api/users/profile-by-login?loginId=${encodeURIComponent(loginId)}`,
      );
      const profile = response.data?.data;
      if (!profile) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回用户资料',
        });
      }
      return this.normalizeProfile(profile, loginId);
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询用户资料失败');
    }
  }

  async updateProfile(userId: string, payload: Record<string, unknown>): Promise<null> {
    const normalizedPayload = this.normalizeProfileUpdatePayload(payload);
    if (Object.keys(normalizedPayload).length === 0) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '未提供可更新的资料字段',
      });
    }
    try {
      await this.backendHttpService.put<BackendEnvelope<unknown>, Record<string, unknown>>(
        `/api/users/${userId}/profile`,
        normalizedPayload,
      );
      return null;
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '更新用户资料失败');
    }
  }

  async getRecentContacts(userId: string, limit: number): Promise<Record<string, unknown>[]> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendRecentContact[]>>(
        `/api/users/${userId}/recent-contacts?limit=${limit}`,
      );
      const items = Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
      return items.map((item) => this.normalizeRecentContact(item, userId));
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询最近联系人失败');
    }
  }

  async getFriends(userId: string, limit: number): Promise<Record<string, unknown>[]> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendContactFriend[]>>(
        `/api/contacts/friends/${userId}?limit=${limit}`,
      );
      const items = Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
      return items.map((item) => this.normalizeFriend(item));
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询好友列表失败');
    }
  }

  private normalizeProfile(raw: BackendUserProfile, fallbackUserId: string): Record<string, unknown> {
    const normalizedUserId = normalizeFirstAvailableUserId(raw.userId, raw.aipayUid) || fallbackUserId;
    return {
      userId: normalizedUserId,
      aipayUid: firstNonEmpty(raw.aipayUid, normalizedUserId, fallbackUserId),
      loginId: firstNonEmpty(raw.loginId, normalizedUserId, fallbackUserId),
      accountStatus: firstNonEmpty(raw.accountStatus, 'ACTIVE'),
      kycLevel: firstNonEmpty(raw.kycLevel, 'L0'),
      nickname: firstNonEmpty(raw.nickname, `用户${fallbackUserId}`),
      avatarUrl: normalizeAvatarUrl(this.backendHttpService, raw.avatarUrl),
      countryCode: firstNonEmpty(raw.countryCode),
      mobile: firstNonEmpty(raw.mobile),
      maskedRealName: firstNonEmpty(raw.maskedRealName),
      idCardNo: firstNonEmpty(raw.idCardNo),
      gender: firstNonEmpty(raw.gender),
      region: firstNonEmpty(raw.region),
      birthday: firstNonEmpty(raw.birthday),
    };
  }

  private normalizeRecentContact(raw: BackendRecentContact, fallbackOwnerUserId: string): Record<string, unknown> {
    const ownerUserId = normalizeUserId(raw.ownerUserId, fallbackOwnerUserId);
    const contactUserId = normalizeFirstAvailableUserId(raw.contactUserId, raw.contactAipayUid);
    const contactNickname = firstNonEmpty(raw.contactNickname, raw.contactDisplayName);
    return {
      ownerUserId,
      contactUserId,
      contactAipayUid: firstNonEmpty(raw.contactAipayUid, contactUserId),
      contactNickname,
      contactDisplayName: firstNonEmpty(raw.contactDisplayName, contactNickname, raw.contactAipayUid, contactUserId),
      contactMaskedRealName: firstNonEmpty(raw.contactMaskedRealName),
      contactAvatarUrl: normalizeAvatarUrl(this.backendHttpService, raw.contactAvatarUrl),
      contactMobileMasked: firstNonEmpty(raw.contactMobileMasked),
      interactionSceneCode: firstNonEmpty(raw.interactionSceneCode, 'UNKNOWN'),
      interactionRemark: firstNonEmpty(raw.interactionRemark),
      interactionCount: this.normalizeCount(raw.interactionCount),
      lastInteractionAt: firstNonEmpty(raw.lastInteractionAt),
    };
  }

  private normalizeFriend(raw: BackendContactFriend): Record<string, unknown> {
    const friendUserId = normalizeFirstAvailableUserId(raw.friendUserId, raw.aipayUid);
    return {
      friendUserId,
      aipayUid: firstNonEmpty(raw.aipayUid, friendUserId),
      nickname: firstNonEmpty(raw.nickname, raw.aipayUid, friendUserId),
      maskedRealName: firstNonEmpty(raw.maskedRealName),
      mobileMasked: firstNonEmpty(raw.mobileMasked),
      avatarUrl: normalizeAvatarUrl(this.backendHttpService, raw.avatarUrl),
      remark: firstNonEmpty(raw.remark),
      createdAt: firstNonEmpty(raw.createdAt),
    };
  }

  private normalizeProfileUpdatePayload(payload: Record<string, unknown>): Record<string, unknown> {
    const allowedFields = ['nickname', 'avatarUrl', 'mobile', 'gender', 'region', 'birthday'];
    const normalized: Record<string, unknown> = {};
    allowedFields.forEach((field) => {
      const value = payload[field];
      if (typeof value === 'string') {
        const trimmed = value.trim();
        if (trimmed.length > 0) {
          normalized[field] = trimmed;
        }
      }
    });
    return normalized;
  }

  private normalizeCount(raw: string | number | undefined): number {
    if (typeof raw === 'number' && Number.isFinite(raw)) {
      return Math.max(0, Math.trunc(raw));
    }
    if (typeof raw === 'string' && /^\d+$/.test(raw.trim())) {
      return Number.parseInt(raw.trim(), 10);
    }
    return 0;
  }

  private rethrowMappedUpstreamError(error: unknown, fallbackMessage: string): never {
    if (error instanceof AxiosError) {
      const status = error.response?.status ?? 502;
      const body = error.response?.data as BackendEnvelope<unknown> | undefined;
      const message = body?.error?.message ?? fallbackMessage;
      const code = body?.error?.code ?? 'UPSTREAM_ERROR';
      if (status === 400) {
        throw new BadRequestException({ code, message });
      }
      if (status === 404) {
        throw new NotFoundException({ code, message });
      }
      throw new BadGatewayException({ code, message });
    }
    if (error instanceof BadRequestException || error instanceof NotFoundException || error instanceof BadGatewayException) {
      throw error;
    }
    throw new BadGatewayException({
      code: 'UPSTREAM_ERROR',
      message: fallbackMessage,
    });
  }
}
