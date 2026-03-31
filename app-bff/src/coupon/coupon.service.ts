import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendCouponIssue = {
  couponNo?: unknown;
  couponAmount?: unknown;
  currencyCode?: unknown;
  expireAt?: unknown;
  expireDate?: unknown;
  status?: unknown;
};

type BackendClaimCouponResult = {
  couponNo?: unknown;
  templateCode?: unknown;
  couponAmount?: unknown;
  currencyCode?: unknown;
  claimedAt?: unknown;
  dailyClaimLimit?: unknown;
  todayClaimedCount?: unknown;
  remainingClaimCount?: unknown;
};

@Injectable()
export class CouponService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async listMobileTopUpRewardCoupons(currentUserId: string): Promise<Record<string, unknown>[]> {
    const normalizedUserId = this.normalizeUserIdInput(currentUserId);
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendCouponIssue[]>>(
        `/api/coupons/mobile-topup-reward/available?userId=${encodeURIComponent(normalizedUserId)}`,
      );
      const payload = unwrapBackendData(response, '上游服务未返回红包列表结果');
      if (!Array.isArray(payload)) {
        return [];
      }
      return payload.map((item) => this.normalizeCouponIssue(item));
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询话费红包失败');
    }
  }

  async claimMobileTopUpRewardCoupon(currentUserId: string, payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const normalizedUserId = this.normalizeUserIdInput(currentUserId);
    const upstreamPayload = this.buildClaimPayload(payload, {
      userId: normalizedUserId,
    });

    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendClaimCouponResult>, Record<string, unknown>>(
        '/api/coupons/mobile-topup-reward/claim',
        upstreamPayload,
      );
      const result = unwrapBackendData(response, '上游服务未返回红包领取结果');
      return this.normalizeClaimResult(result);
    } catch (error) {
      rethrowMappedUpstreamError(error, '领取话费红包失败');
    }
  }

  private buildClaimPayload(
    payload: Record<string, unknown>,
    required: {
      userId: string;
    },
  ): Record<string, unknown> {
    const upstreamPayload: Record<string, unknown> = {
      userId: required.userId,
    };
    const businessNo = this.normalizeOptionalText(payload.businessNo, 64);
    if (businessNo) {
      upstreamPayload.businessNo = businessNo;
    }
    return upstreamPayload;
  }

  private normalizeCouponIssue(payload: BackendCouponIssue): Record<string, unknown> {
    return {
      couponNo: this.normalizeOptionalText(payload?.couponNo, 64) ?? '',
      couponAmount: this.normalizeDecimalText(payload?.couponAmount) ?? '0.00',
      currencyCode: this.normalizeOptionalUppercaseText(payload?.currencyCode) ?? 'CNY',
      expireAt: this.normalizeOptionalText(payload?.expireAt, 64),
      expireDate: this.normalizeOptionalText(payload?.expireDate, 64),
      status: this.normalizeOptionalUppercaseText(payload?.status) ?? 'UNUSED',
    };
  }

  private normalizeClaimResult(payload: BackendClaimCouponResult): Record<string, unknown> {
    return {
      couponNo: this.normalizeOptionalText(payload?.couponNo, 64) ?? '',
      templateCode: this.normalizeOptionalText(payload?.templateCode, 64) ?? '',
      couponAmount: this.normalizeDecimalText(payload?.couponAmount) ?? '0.00',
      currencyCode: this.normalizeOptionalUppercaseText(payload?.currencyCode) ?? 'CNY',
      claimedAt: this.normalizeOptionalText(payload?.claimedAt, 64),
      dailyClaimLimit: this.normalizeOptionalInteger(payload?.dailyClaimLimit) ?? 0,
      todayClaimedCount: this.normalizeOptionalInteger(payload?.todayClaimedCount) ?? 0,
      remainingClaimCount: this.normalizeOptionalInteger(payload?.remainingClaimCount) ?? 0,
    };
  }

  private assertRequiredString(value: unknown, field: string): string {
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private normalizeUserIdInput(userId: unknown): string {
    const normalized = normalizePositiveLongId(userId, { minDigits: 1, maxDigits: 20 });
    if (!normalized) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'userId 参数格式不正确，请使用字符串传递18位ID',
      });
    }
    return normalized;
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

  private normalizeOptionalUppercaseText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    if (!normalized) {
      return undefined;
    }
    return normalized.toUpperCase();
  }

  private normalizeDecimalText(value: unknown): string | undefined {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value.toString();
    }
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized ? normalized : undefined;
  }

  private normalizeOptionalInteger(value: unknown): number | undefined {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return Math.trunc(value);
    }
    if (typeof value === 'string' && /^\d+$/.test(value.trim())) {
      return Number.parseInt(value.trim(), 10);
    }
    return undefined;
  }
}
