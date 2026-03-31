import { AxiosError } from 'axios';
import { BadGatewayException, BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';

const PAGE_META: Record<string, { title: string; scene: string }> = {
  home: { title: '首页', scene: 'payments+service' },
  transfer: { title: '转账', scene: 'money-transfer' },
  balance: { title: '余额', scene: 'wallet-balance' },
  me: { title: '我的', scene: 'personal-center' },
  settings: { title: '设置', scene: 'security-and-preference' },
};

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
  mobile?: string;
  maskedRealName?: string;
};

type BackendUserSecurity = {
  loginPasswordSet?: boolean;
  payPasswordSet?: boolean;
  biometricEnabled?: boolean;
  twoFactorMode?: string;
  riskLevel?: string;
  deviceLockEnabled?: boolean;
  privacyModeEnabled?: boolean;
};

type BackendUserSettings = {
  allowSearchByMobile?: boolean;
  allowSearchByAipayUid?: boolean;
  hideRealName?: boolean;
  personalizedRecommendationEnabled?: boolean;
  deviceLockEnabled?: boolean;
  privacyModeEnabled?: boolean;
  riskLevel?: string;
};

type BackendUserInit = {
  profile?: BackendUserProfile | null;
  security?: BackendUserSecurity | null;
  settings?: BackendUserSettings | null;
};

@Injectable()
export class PageInitService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async build(page: string, uid?: string | number): Promise<Record<string, unknown>> {
    const meta = PAGE_META[page] ?? { title: page, scene: 'generic' };

    if (!uid) {
      return {
        page,
        meta,
        user: null,
        permissions: {
          canTrade: false,
          canTransfer: false,
        },
      };
    }

    const normalizedUserId = this.normalizeUserIdInput(uid);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendUserInit>>(
        `/api/users/${encodeURIComponent(normalizedUserId)}/init`,
      );
      const init = response.data?.data;
      if (!init) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回页面初始化数据',
        });
      }

      const user = this.normalizeUser(init.profile, normalizedUserId);
      const security = this.normalizeSecurity(init.security);
      const settings = this.normalizeSettings(init.settings);

      return {
        page,
        meta,
        user,
        security,
        settings,
        permissions: {
          canTrade: user?.accountStatus === 'ACTIVE',
          canTransfer: security?.payPasswordSet === true,
        },
        fallback: {
          initFallback: false,
        },
      };
    } catch (error) {
      if (error instanceof BadRequestException || error instanceof NotFoundException || error instanceof BadGatewayException) {
        throw error;
      }
      if (error instanceof AxiosError && error.response?.status === 404) {
        const body = error.response?.data as BackendEnvelope<unknown> | undefined;
        throw new NotFoundException({
          code: body?.error?.code ?? 'USER_NOT_FOUND',
          message: body?.error?.message ?? '用户不存在',
        });
      }

      return {
        page,
        meta,
        user: null,
        security: null,
        settings: null,
        permissions: {
          canTrade: false,
          canTransfer: false,
        },
        fallback: {
          initFallback: true,
        },
      };
    }
  }

  private normalizeUser(raw: BackendUserProfile | null | undefined, fallbackUserId: string): Record<string, unknown> | null {
    if (!raw) {
      return null;
    }
    return {
      userId: this.normalizeUserId(raw.userId, fallbackUserId),
      aipayUid: this.firstNonEmpty(raw.aipayUid, fallbackUserId),
      loginId: this.firstNonEmpty(raw.loginId, fallbackUserId),
      accountStatus: this.firstNonEmpty(raw.accountStatus, 'UNKNOWN'),
      kycLevel: this.firstNonEmpty(raw.kycLevel, 'L0'),
      nickname: this.firstNonEmpty(raw.nickname, `用户${fallbackUserId}`),
      avatarUrl: this.normalizeAvatarUrl(raw.avatarUrl),
      mobile: this.firstNonEmpty(raw.mobile),
      maskedRealName: this.firstNonEmpty(raw.maskedRealName),
    };
  }

  private normalizeSecurity(raw: BackendUserSecurity | null | undefined): Record<string, unknown> | null {
    if (!raw) {
      return null;
    }
    return {
      loginPasswordSet: raw.loginPasswordSet === true,
      payPasswordSet: raw.payPasswordSet === true,
      biometricEnabled: raw.biometricEnabled === true,
      twoFactorMode: this.firstNonEmpty(raw.twoFactorMode, 'NONE'),
      riskLevel: this.firstNonEmpty(raw.riskLevel, 'UNKNOWN'),
      deviceLockEnabled: raw.deviceLockEnabled === true,
      privacyModeEnabled: raw.privacyModeEnabled === true,
    };
  }

  private normalizeSettings(raw: BackendUserSettings | null | undefined): Record<string, unknown> | null {
    if (!raw) {
      return null;
    }
    return {
      allowSearchByMobile: raw.allowSearchByMobile !== false,
      allowSearchByAipayUid: raw.allowSearchByAipayUid !== false,
      hideRealName: raw.hideRealName === true,
      personalizedRecommendationEnabled: raw.personalizedRecommendationEnabled !== false,
      deviceLockEnabled: raw.deviceLockEnabled === true,
      privacyModeEnabled: raw.privacyModeEnabled === true,
      riskLevel: this.firstNonEmpty(raw.riskLevel, 'UNKNOWN'),
    };
  }

  private normalizeAvatarUrl(raw: string | undefined): string {
    const normalized = this.backendHttpService.resolveAbsoluteUrl(raw);
    if (normalized.includes('api.dicebear.com') && normalized.includes('/svg?')) {
      return normalized.replace('/svg?', '/png?');
    }
    return normalized;
  }

  private normalizeUserId(raw: string | number | undefined, fallback: string): string {
    const normalized = normalizePositiveLongId(raw, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    return fallback;
  }

  private normalizeUserIdInput(value: string | number): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'uid 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private firstNonEmpty(...values: Array<string | undefined | null>): string {
    for (const value of values) {
      if (value && value.trim().length > 0) {
        return value.trim();
      }
    }
    return '';
  }
}
