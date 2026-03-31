import { Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendCashierView = {
  userId?: string | number;
  sceneCode?: string;
  sceneConfig?: {
    supportedChannels?: unknown;
    bankCardPolicy?: unknown;
    emptyBankCardText?: unknown;
  };
  payTools?: unknown;
  generatedAt?: unknown;
};

type BackendCashierPayTool = {
  toolType?: unknown;
  toolCode?: unknown;
  paymentToolCode?: unknown;
  toolName?: unknown;
  toolDescription?: unknown;
  defaultSelected?: unknown;
  default?: unknown;
  bankCode?: unknown;
  cardType?: unknown;
  phoneTailNo?: unknown;
  cardTailNo?: unknown;
};

type BackendCashierPricingPreview = {
  userId?: string | number;
  sceneCode?: unknown;
  pricingSceneCode?: unknown;
  paymentMethod?: unknown;
  quoteNo?: unknown;
  ruleCode?: unknown;
  ruleName?: unknown;
  originalAmount?: unknown;
  feeAmount?: unknown;
  payableAmount?: unknown;
  settleAmount?: unknown;
  feeRate?: unknown;
  feeBearer?: unknown;
  availableCouponCount?: unknown;
  recommendedCouponNo?: unknown;
  recommendedCouponAmount?: unknown;
  couponDeductAmount?: unknown;
  payableAfterCoupon?: unknown;
  availableCoupons?: unknown;
};

type NormalizedMoney = {
  amount: string;
  currencyCode: string;
};

@Injectable()
export class CashierService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getCashierView(userId: string, sceneCode?: string): Promise<Record<string, unknown>> {
    const normalizedSceneCode = this.normalizeUppercaseCode(sceneCode, 'TRANSFER');
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendCashierView>>(
        `/api/cashier/users/${userId}/payment-tools?sceneCode=${encodeURIComponent(normalizedSceneCode)}`,
      );
      const payload = unwrapBackendData(response, '上游服务未返回收银台数据');
      return this.normalizeCashierView(payload, userId, normalizedSceneCode);
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询支付工具失败');
    }
  }

  async getPricingPreview(
    userId: string,
    sceneCode: string | undefined,
    paymentMethod: string | undefined,
    amount: string,
    currencyCode: string | undefined,
  ): Promise<Record<string, unknown>> {
    const normalizedSceneCode = this.normalizeUppercaseCode(sceneCode, 'WITHDRAW');
    const normalizedPaymentMethod = this.normalizeUppercaseCode(paymentMethod, 'BANK_CARD');
    const normalizedCurrencyCode = this.normalizeUppercaseCode(currencyCode, 'CNY');

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendCashierPricingPreview>>(
        `/api/cashier/users/${userId}/pricing-preview?sceneCode=${encodeURIComponent(normalizedSceneCode)}&paymentMethod=${encodeURIComponent(normalizedPaymentMethod)}&amount=${encodeURIComponent(amount)}&currencyCode=${encodeURIComponent(normalizedCurrencyCode)}`,
      );
      const payload = unwrapBackendData(response, '上游服务未返回收银台试算数据');
      return this.normalizePricingPreview(payload, {
        userId,
        sceneCode: normalizedSceneCode,
        paymentMethod: normalizedPaymentMethod,
        currencyCode: normalizedCurrencyCode,
        amount,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '提现手续费试算失败');
    }
  }

  private normalizeUppercaseCode(raw: unknown, fallback: string): string {
    if (typeof raw !== 'string') {
      return fallback;
    }
    const normalized = raw?.trim().toUpperCase() ?? '';
    return normalized.length > 0 ? normalized : fallback;
  }

  private normalizeCashierView(
    payload: BackendCashierView,
    requestedUserId: string,
    requestedSceneCode: string,
  ): Record<string, unknown> {
    const userId = normalizePositiveLongId(payload?.userId, { minDigits: 1, maxDigits: 20 }) || requestedUserId;
    const sceneCode = this.normalizeUppercaseCode(payload?.sceneCode, requestedSceneCode);
    const payToolsRaw = Array.isArray(payload?.payTools) ? payload.payTools : [];
    const payTools = payToolsRaw
      .map((item) => this.normalizePayTool(item))
      .filter((item): item is Record<string, unknown> => item !== null);

    return {
      userId,
      sceneCode,
      sceneConfig: this.normalizeSceneConfig(payload?.sceneConfig),
      payTools,
      generatedAt: this.normalizeOptionalText(payload?.generatedAt),
    };
  }

  private normalizeSceneConfig(value: BackendCashierView['sceneConfig']): Record<string, unknown> | undefined {
    if (!value || typeof value !== 'object') {
      return undefined;
    }
    const supportedChannelsRaw = Array.isArray(value.supportedChannels) ? value.supportedChannels : [];
    const supportedChannels = supportedChannelsRaw
      .map((item) => this.normalizeUppercaseCode(item, ''))
      .filter((item) => item.length > 0);
    return {
      supportedChannels,
      bankCardPolicy: this.normalizeOptionalText(value.bankCardPolicy),
      emptyBankCardText: this.normalizeOptionalText(value.emptyBankCardText),
    };
  }

  private normalizePayTool(value: unknown): Record<string, unknown> | null {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      return null;
    }
    const raw = value as BackendCashierPayTool;
    const toolCode = this.normalizeOptionalText(raw.toolCode)
      ?? this.normalizeOptionalText(raw.paymentToolCode);
    if (!toolCode) {
      return null;
    }
    return {
      toolType: this.normalizeUppercaseCode(raw.toolType, 'WALLET'),
      toolCode,
      toolName: this.normalizeOptionalText(raw.toolName) ?? toolCode,
      toolDescription: this.normalizeOptionalText(raw.toolDescription),
      defaultSelected: this.normalizeBoolean(raw.defaultSelected ?? raw.default),
      bankCode: this.normalizeOptionalText(raw.bankCode),
      cardType: this.normalizeUppercaseCode(raw.cardType, '') || undefined,
      phoneTailNo: this.normalizeOptionalText(raw.phoneTailNo) ?? this.normalizeOptionalText(raw.cardTailNo),
    };
  }

  private normalizePricingPreview(
    payload: BackendCashierPricingPreview,
    fallback: {
      userId: string;
      sceneCode: string;
      paymentMethod: string;
      currencyCode: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const userId = normalizePositiveLongId(payload?.userId, { minDigits: 1, maxDigits: 20 }) || fallback.userId;
    const sceneCode = this.normalizeUppercaseCode(payload?.sceneCode, fallback.sceneCode);
    const paymentMethod = this.normalizeUppercaseCode(payload?.paymentMethod, fallback.paymentMethod);
    const originalAmount = this.normalizeMoney(payload?.originalAmount, fallback.currencyCode, fallback.amount);
    const feeAmount = this.normalizeMoney(payload?.feeAmount, originalAmount.currencyCode, '0');
    const payableAmount = this.normalizeMoney(payload?.payableAmount, originalAmount.currencyCode, fallback.amount);
    const settleAmount = this.normalizeMoney(payload?.settleAmount, originalAmount.currencyCode, payableAmount.amount);

    return {
      userId,
      sceneCode,
      pricingSceneCode: this.normalizeUppercaseCode(payload?.pricingSceneCode, sceneCode),
      paymentMethod,
      quoteNo: this.normalizeOptionalText(payload?.quoteNo) ?? '',
      ruleCode: this.normalizeOptionalText(payload?.ruleCode) ?? '',
      ruleName: this.normalizeOptionalText(payload?.ruleName) ?? '',
      originalAmount,
      feeAmount,
      payableAmount,
      settleAmount,
      feeRate: this.normalizeDecimalText(payload?.feeRate) ?? '0',
      feeBearer: this.normalizeUppercaseCode(payload?.feeBearer, 'PAYEE'),
      availableCouponCount: this.normalizeInteger(payload?.availableCouponCount),
      recommendedCouponNo: this.normalizeOptionalText(payload?.recommendedCouponNo),
      recommendedCouponAmount: this.normalizeMoney(payload?.recommendedCouponAmount, originalAmount.currencyCode, '0'),
      couponDeductAmount: this.normalizeMoney(payload?.couponDeductAmount, originalAmount.currencyCode, '0'),
      payableAfterCoupon: this.normalizeMoney(payload?.payableAfterCoupon, originalAmount.currencyCode, payableAmount.amount),
      availableCoupons: this.normalizeCouponList(payload?.availableCoupons, originalAmount.currencyCode),
    };
  }

  private normalizeCouponList(value: unknown, fallbackCurrencyCode: string): Array<Record<string, unknown>> {
    const rawList = Array.isArray(value) ? value : [];
    return rawList
      .map((item) => this.normalizeCouponItem(item, fallbackCurrencyCode))
      .filter((item): item is Record<string, unknown> => item !== null);
  }

  private normalizeCouponItem(value: unknown, fallbackCurrencyCode: string): Record<string, unknown> | null {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      return null;
    }
    const raw = value as Record<string, unknown>;
    const couponNo = this.normalizeOptionalText(raw.couponNo);
    const couponAmount = this.normalizeMoney(raw.couponAmount, fallbackCurrencyCode, '0');
    if (!couponNo) {
      return null;
    }
    return {
      couponNo,
      couponAmount,
      expireAt: this.normalizeOptionalText(raw.expireAt),
    };
  }

  private normalizeInteger(value: unknown): number {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return Math.max(0, Math.trunc(value));
    }
    if (typeof value === 'string') {
      const parsed = Number.parseInt(value.trim(), 10);
      if (Number.isFinite(parsed)) {
        return Math.max(0, parsed);
      }
    }
    return 0;
  }

  private normalizeMoney(value: unknown, fallbackCurrencyCode: string, fallbackAmount: string): NormalizedMoney {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      const raw = value as Record<string, unknown>;
      const nestedCurrency = raw.currencyUnit;
      const currencyFromNested = nestedCurrency && typeof nestedCurrency === 'object' && !Array.isArray(nestedCurrency)
        ? this.normalizeOptionalText((nestedCurrency as Record<string, unknown>).code)
        : undefined;
      return {
        amount: this.normalizeDecimalText(raw.amount ?? raw.value) ?? fallbackAmount,
        currencyCode: (this.normalizeOptionalText(raw.currencyCode)
          ?? this.normalizeOptionalText(raw.currency)
          ?? currencyFromNested
          ?? fallbackCurrencyCode).toUpperCase(),
      };
    }
    return {
      amount: this.normalizeDecimalText(value) ?? fallbackAmount,
      currencyCode: fallbackCurrencyCode,
    };
  }

  private normalizeDecimalText(value: unknown): string | undefined {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value.toString();
    }
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    if (!normalized) {
      return undefined;
    }
    return normalized;
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

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized ? normalized : undefined;
  }
}
