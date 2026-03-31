import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendTradeResult = {
  tradeNo?: unknown;
  requestNo?: unknown;
  status?: unknown;
  payerUserId?: unknown;
  payeeUserId?: unknown;
  feeAmount?: unknown;
  payableAmount?: unknown;
  settleAmount?: unknown;
};

@Injectable()
export class TradeService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async createTransferTrade(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestNo = this.assertRequiredString(payload.requestNo, 'requestNo');
    const payerUserId = this.assertRequiredUserId(payload.payerUserId, 'payerUserId');
    const payeeUserId = this.assertRequiredUserId(payload.payeeUserId, 'payeeUserId');
    const amount = this.normalizeRequiredAmountText(payload.amount, 'amount');
    const response = await this.postTradeAction(
      '/api/trade/transfer',
      this.buildTransferPayload(payload, {
        requestNo,
        payerUserId,
        payeeUserId,
        amount,
      }),
      '创建转账交易失败',
      '上游服务未返回转账交易结果',
    );
    return this.normalizeTradeResult(response, {
      requestNo,
      payerUserId,
      payeeUserId,
      currencyCode: this.normalizeOptionalUppercaseText(payload.currencyCode) ?? 'CNY',
    });
  }

  async createDepositTrade(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestNo = this.assertRequiredString(payload.requestNo, 'requestNo');
    const payerUserId = this.assertRequiredUserId(payload.payerUserId, 'payerUserId');
    const amount = this.normalizeRequiredAmountText(payload.amount, 'amount');
    const response = await this.postTradeAction(
      '/api/trade/deposit',
      this.buildDepositPayload(payload, {
        requestNo,
        payerUserId,
        amount,
      }),
      '创建充值交易失败',
      '上游服务未返回充值交易结果',
    );
    return this.normalizeTradeResult(response, {
      requestNo,
      payerUserId,
      payeeUserId: this.normalizeOptionalUserId(payload.payeeUserId),
      currencyCode: this.resolveTradeCurrencyCode(payload.amount, payload.currencyCode),
    });
  }

  async createWithdrawTrade(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestNo = this.assertRequiredString(payload.requestNo, 'requestNo');
    const payerUserId = this.assertRequiredUserId(payload.payerUserId, 'payerUserId');
    const amount = this.normalizeRequiredAmountText(payload.amount, 'amount');
    const response = await this.postTradeAction(
      '/api/trade/withdraw',
      this.buildWithdrawPayload(payload, {
        requestNo,
        payerUserId,
        amount,
      }),
      '创建提现交易失败',
      '上游服务未返回提现交易结果',
    );
    return this.normalizeTradeResult(response, {
      requestNo,
      payerUserId,
      payeeUserId: this.normalizeOptionalUserId(payload.payeeUserId),
      currencyCode: this.resolveTradeCurrencyCode(payload.amount, payload.currencyCode),
    });
  }

  async createPayTrade(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const requestNo = this.assertRequiredString(payload.requestNo, 'requestNo');
    const payerUserId = this.assertRequiredUserId(payload.payerUserId, 'payerUserId');
    const payeeUserId = this.assertRequiredUserId(payload.payeeUserId, 'payeeUserId');
    const amount = this.normalizeRequiredAmountText(payload.amount, 'amount');
    const response = await this.postTradeAction(
      '/api/trade/pay',
      this.buildPayPayload(payload, {
        requestNo,
        payerUserId,
        payeeUserId,
        amount,
      }),
      '创建支付交易失败',
      '上游服务未返回支付交易结果',
    );
    return this.normalizeTradeResult(response, {
      requestNo,
      payerUserId,
      payeeUserId,
      currencyCode: this.resolveTradeCurrencyCode(payload.amount, payload.currencyCode),
    });
  }

  private async postTradeAction(
    path: string,
    payload: Record<string, unknown>,
    fallbackMessage: string,
    emptyMessage: string,
  ): Promise<BackendTradeResult> {
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendTradeResult>, Record<string, unknown>>(path, payload);
      const payloadData = unwrapBackendData(response, emptyMessage);
      return payloadData && typeof payloadData === 'object' ? payloadData : {};
    } catch (error) {
      rethrowMappedUpstreamError(error, fallbackMessage);
    }
  }

  private buildTransferPayload(
    payload: Record<string, unknown>,
    required: {
      requestNo: string;
      payerUserId: string;
      payeeUserId: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const normalizedPayload: Record<string, unknown> = {
      requestNo: required.requestNo,
      payerUserId: required.payerUserId,
      payeeUserId: required.payeeUserId,
      amount: required.amount,
      currencyCode: this.normalizeOptionalUppercaseText(payload.currencyCode) ?? 'CNY',
    };
    this.assignOptionalText(normalizedPayload, 'businessSceneCode', payload.businessSceneCode);
    this.assignOptionalUppercaseText(normalizedPayload, 'paymentMethod', payload.paymentMethod);
    this.assignOptionalText(normalizedPayload, 'paymentToolCode', payload.paymentToolCode);
    this.assignOptionalText(normalizedPayload, 'metadata', payload.metadata);
    this.assignOptionalAmountText(normalizedPayload, 'walletDebitAmount', payload.walletDebitAmount);
    this.assignOptionalAmountText(normalizedPayload, 'fundDebitAmount', payload.fundDebitAmount);
    this.assignOptionalAmountText(normalizedPayload, 'creditDebitAmount', payload.creditDebitAmount);
    this.assignOptionalAmountText(normalizedPayload, 'inboundDebitAmount', payload.inboundDebitAmount);
    return normalizedPayload;
  }

  private buildDepositPayload(
    payload: Record<string, unknown>,
    required: {
      requestNo: string;
      payerUserId: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const normalizedPayload: Record<string, unknown> = {
      requestNo: required.requestNo,
      payerUserId: required.payerUserId,
      amount: this.normalizeMoneyLikePayload(payload.amount, required.amount, 'CNY'),
    };
    const payeeUserId = this.normalizeOptionalUserId(payload.payeeUserId);
    if (payeeUserId) {
      normalizedPayload.payeeUserId = payeeUserId;
    }
    this.assignOptionalText(normalizedPayload, 'businessSceneCode', payload.businessSceneCode);
    this.assignOptionalUppercaseText(normalizedPayload, 'paymentMethod', payload.paymentMethod);
    this.assignOptionalText(normalizedPayload, 'paymentToolCode', payload.paymentToolCode);
    this.assignOptionalText(normalizedPayload, 'metadata', payload.metadata);
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'walletDebitAmount', payload.walletDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'fundDebitAmount', payload.fundDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'creditDebitAmount', payload.creditDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'inboundDebitAmount', payload.inboundDebitAmount, 'CNY');
    return normalizedPayload;
  }

  private buildWithdrawPayload(
    payload: Record<string, unknown>,
    required: {
      requestNo: string;
      payerUserId: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const normalizedPayload: Record<string, unknown> = {
      requestNo: required.requestNo,
      payerUserId: required.payerUserId,
      amount: this.normalizeMoneyLikePayload(payload.amount, required.amount, 'CNY'),
    };
    const payeeUserId = this.normalizeOptionalUserId(payload.payeeUserId);
    if (payeeUserId) {
      normalizedPayload.payeeUserId = payeeUserId;
    }
    this.assignOptionalText(normalizedPayload, 'businessSceneCode', payload.businessSceneCode);
    this.assignOptionalUppercaseText(normalizedPayload, 'paymentMethod', payload.paymentMethod);
    this.assignOptionalText(normalizedPayload, 'paymentToolCode', payload.paymentToolCode);
    this.assignOptionalText(normalizedPayload, 'metadata', payload.metadata);
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'walletDebitAmount', payload.walletDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'fundDebitAmount', payload.fundDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'creditDebitAmount', payload.creditDebitAmount, 'CNY');
    return normalizedPayload;
  }

  private buildPayPayload(
    payload: Record<string, unknown>,
    required: {
      requestNo: string;
      payerUserId: string;
      payeeUserId: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const normalizedPayload: Record<string, unknown> = {
      requestNo: required.requestNo,
      payerUserId: required.payerUserId,
      payeeUserId: required.payeeUserId,
      amount: this.normalizeMoneyLikePayload(payload.amount, required.amount, 'CNY'),
    };
    this.assignOptionalText(normalizedPayload, 'businessSceneCode', payload.businessSceneCode);
    this.assignOptionalUppercaseText(normalizedPayload, 'paymentMethod', payload.paymentMethod);
    this.assignOptionalText(normalizedPayload, 'paymentToolCode', payload.paymentToolCode);
    this.assignOptionalText(normalizedPayload, 'couponNo', payload.couponNo);
    this.assignOptionalText(normalizedPayload, 'metadata', payload.metadata);
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'walletDebitAmount', payload.walletDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'fundDebitAmount', payload.fundDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'creditDebitAmount', payload.creditDebitAmount, 'CNY');
    this.assignOptionalMoneyLikePayload(normalizedPayload, 'inboundDebitAmount', payload.inboundDebitAmount, 'CNY');
    return normalizedPayload;
  }

  private normalizeTradeResult(
    raw: BackendTradeResult,
    fallback: {
      requestNo: string;
      payerUserId: string;
      payeeUserId?: string;
      currencyCode: string;
    },
  ): Record<string, unknown> {
    const payerUserId = this.normalizeOptionalUserId(raw.payerUserId) ?? fallback.payerUserId;
    const payeeUserId = this.normalizeOptionalUserId(raw.payeeUserId) ?? fallback.payeeUserId;
    return {
      tradeNo: this.normalizeOptionalText(raw.tradeNo) ?? '',
      requestNo: this.normalizeOptionalText(raw.requestNo) ?? fallback.requestNo,
      status: this.normalizeOptionalUppercaseText(raw.status) ?? 'ACCEPTED',
      payerUserId,
      payeeUserId,
      feeAmount: this.normalizeMoneyLikePayload(raw.feeAmount, '0', fallback.currencyCode),
      payableAmount: this.normalizeMoneyLikePayload(raw.payableAmount, '0', fallback.currencyCode),
      settleAmount: this.normalizeMoneyLikePayload(raw.settleAmount, '0', fallback.currencyCode),
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

  private normalizeRequiredAmountText(value: unknown, field: string): string {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value.toString();
    }
    if (typeof value === 'string') {
      const normalized = value.trim();
      if (normalized.length > 0) {
        return normalized;
      }
    }
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      const raw = value as Record<string, unknown>;
      return this.normalizeRequiredAmountText(raw.amount ?? raw.value, field);
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private resolveTradeCurrencyCode(amount: unknown, currencyCode: unknown): string {
    const explicit = this.normalizeOptionalUppercaseText(currencyCode);
    if (explicit) {
      return explicit;
    }
    if (amount && typeof amount === 'object' && !Array.isArray(amount)) {
      const raw = amount as Record<string, unknown>;
      const nested = raw.currencyUnit;
      if (nested && typeof nested === 'object' && !Array.isArray(nested)) {
        const nestedCode = this.normalizeOptionalUppercaseText((nested as Record<string, unknown>).code);
        if (nestedCode) {
          return nestedCode;
        }
      }
      const amountCode = this.normalizeOptionalUppercaseText(raw.currencyCode ?? raw.currency);
      if (amountCode) {
        return amountCode;
      }
    }
    return 'CNY';
  }

  private normalizeMoneyLikePayload(value: unknown, fallbackAmount: string, fallbackCurrencyCode: string): unknown {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      const raw = value as Record<string, unknown>;
      const nested = raw.currencyUnit;
      const nestedCode = nested && typeof nested === 'object' && !Array.isArray(nested)
        ? this.normalizeOptionalUppercaseText((nested as Record<string, unknown>).code)
        : undefined;
      const currencyCode = this.normalizeOptionalUppercaseText(raw.currencyCode ?? raw.currency)
        ?? nestedCode
        ?? fallbackCurrencyCode;
      return {
        amount: this.normalizeRequiredAmountText(raw.amount ?? raw.value ?? fallbackAmount, 'amount'),
        currencyCode,
      };
    }
    return this.normalizeRequiredAmountText(value ?? fallbackAmount, 'amount');
  }

  private normalizeOptionalUserId(value: unknown): string | undefined {
    return normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 }) || undefined;
  }

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private normalizeOptionalUppercaseText(value: unknown): string | undefined {
    const normalized = this.normalizeOptionalText(value);
    return normalized ? normalized.toUpperCase() : undefined;
  }

  private assignOptionalText(target: Record<string, unknown>, field: string, value: unknown): void {
    const normalized = this.normalizeOptionalText(value);
    if (normalized !== undefined) {
      target[field] = normalized;
    }
  }

  private assignOptionalUppercaseText(target: Record<string, unknown>, field: string, value: unknown): void {
    const normalized = this.normalizeOptionalUppercaseText(value);
    if (normalized !== undefined) {
      target[field] = normalized;
    }
  }

  private assignOptionalAmountText(target: Record<string, unknown>, field: string, value: unknown): void {
    if (value === undefined || value === null || value === '') {
      return;
    }
    target[field] = this.normalizeRequiredAmountText(value, field);
  }

  private assignOptionalMoneyLikePayload(
    target: Record<string, unknown>,
    field: string,
    value: unknown,
    fallbackCurrencyCode: string,
  ): void {
    if (value === undefined || value === null || value === '') {
      return;
    }
    target[field] = this.normalizeMoneyLikePayload(value, '0', fallbackCurrencyCode);
  }
}
