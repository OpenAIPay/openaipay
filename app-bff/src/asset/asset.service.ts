import { BadGatewayException, BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { rethrowMappedUpstreamError } from '../common/upstream';

type BackendErrorBody = {
  code?: string;
  message?: string;
};

type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: BackendErrorBody;
};

type BackendAssetOverview = {
  userId?: string | number;
  currencyCode?: string;
  availableAmount?: string | number;
  reservedAmount?: string | number;
  totalAmount?: string | number;
  accountStatus?: string;
  generatedAt?: string;
};

type BackendTradeWalletFlow = {
  tradeNo?: string;
  tradeOrderNo?: string;
  tradeType?: string;
  businessSceneCode?: string;
  direction?: string;
  signedWalletAmount?: string | number;
  couponDiscountAmount?: string | number;
  currencyCode?: string;
  counterpartyUserId?: string | number;
  counterpartyNickname?: string;
  counterpartyAvatarUrl?: string;
  displayTitle?: string;
  occurredAt?: string;
};

type BackendBankCard = {
  cardNo?: string | number;
  bankCode?: string;
  bankName?: string;
  defaultCard?: boolean;
};

type AssetChangeItem = {
  tradeNo: string;
  tradeType: string;
  businessSceneCode: string;
  direction: string;
  signedAmount: string;
  couponDiscountAmount: string;
  currencyCode: string;
  counterpartyUserId: string;
  counterpartyNickname: string;
  counterpartyAvatarUrl: string;
  displayTitle: string;
  bankCode: string;
  bankName: string;
  bankCardTailNo: string;
  occurredAt: string;
};

@Injectable()
export class AssetService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getUserAssetOverview(userId: string): Promise<Record<string, string>> {
    const normalizedUserId = this.normalizeUserIdInput(userId);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendAssetOverview>>(
        `/api/assets/users/${normalizedUserId}/overview`,
      );
      const data = response.data?.data;
      if (!data) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回资产数据',
        });
      }

      return {
        userId: this.normalizeUserId(data.userId, normalizedUserId),
        currencyCode: data.currencyCode ?? 'CNY',
        availableAmount: this.normalizeAmount(data.availableAmount),
        reservedAmount: this.normalizeAmount(data.reservedAmount),
        totalAmount: this.normalizeAmount(data.totalAmount),
        accountStatus: data.accountStatus ?? 'UNKNOWN',
        generatedAt: data.generatedAt ?? new Date().toISOString(),
      };
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询失败');
    }
  }

  async getUserAssetChanges(userId: string, limit: number): Promise<Record<string, string | AssetChangeItem[]>> {
    const normalizedUserId = this.normalizeUserIdInput(userId);
    const normalizedLimit = Math.max(1, Math.min(limit, 100));

    try {
      const rawFlows = await this.loadRecentWalletFlows(normalizedUserId, normalizedLimit);
      const defaultBankCard = this.requiresBankCardMetadata(rawFlows)
        ? await this.loadDefaultActiveBankCard(normalizedUserId)
        : null;
      const currencyCode = this.resolveChangesCurrencyCode(rawFlows);
      const items = rawFlows.map((item, index) =>
        this.normalizeAssetChangeItem(item, currencyCode, defaultBankCard, index),
      );

      return {
        userId: normalizedUserId,
        currencyCode,
        generatedAt: new Date().toISOString(),
        items,
      };
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询失败');
    }
  }


  private normalizeAssetChangeItem(
    raw: BackendTradeWalletFlow,
    fallbackCurrencyCode: string,
    defaultBankCard: BackendBankCard | null,
    sequenceNo: number,
  ): AssetChangeItem {
    const fallbackTradeNo = `trade_${Date.now()}_${sequenceNo + 1}`;
    const tradeNo = this.firstNonEmpty(raw.tradeNo, raw.tradeOrderNo, fallbackTradeNo);
    const tradeType = this.firstNonEmpty(raw.tradeType, 'UNKNOWN');
    const businessSceneCode = this.firstNonEmpty(raw.businessSceneCode, 'UNKNOWN');
    const direction = this.normalizeDirection(raw.direction, businessSceneCode);
    const counterpartyUserId = this.normalizeOptionalUserId(raw.counterpartyUserId);
    const counterpartyNickname = this.firstNonEmpty(raw.counterpartyNickname);
    const counterpartyAvatarUrl = this.firstNonEmpty(
      this.normalizeAvatarUrl(raw.counterpartyAvatarUrl),
      counterpartyUserId.length > 0 ? this.defaultCounterpartyAvatarUrl(counterpartyUserId) : '',
    );
    const isWithdrawOrDeposit = tradeType === 'WITHDRAW' || tradeType === 'DEPOSIT';

    return {
      tradeNo,
      tradeType,
      businessSceneCode,
      direction,
      signedAmount: this.normalizeSignedAmount(raw.signedWalletAmount, direction, businessSceneCode),
      couponDiscountAmount: this.normalizeOptionalAmount(raw.couponDiscountAmount),
      currencyCode: this.firstNonEmpty(raw.currencyCode, fallbackCurrencyCode, 'CNY'),
      counterpartyUserId,
      counterpartyNickname,
      counterpartyAvatarUrl,
      displayTitle: this.resolveFlowTitle(raw, direction, counterpartyNickname, defaultBankCard),
      bankCode: isWithdrawOrDeposit ? this.firstNonEmpty(defaultBankCard?.bankCode) : '',
      bankName: isWithdrawOrDeposit ? this.firstNonEmpty(defaultBankCard?.bankName) : '',
      bankCardTailNo: isWithdrawOrDeposit ? this.extractCardTail(defaultBankCard?.cardNo) : '',
      occurredAt: this.normalizeDateTime(raw.occurredAt),
    };
  }

  private normalizeDirection(rawDirection: string | undefined, businessSceneCode: string): string {
    if (businessSceneCode === 'FUND_FAST_REDEEM') {
      return 'CREDIT';
    }
    return rawDirection === 'DEBIT' ? 'DEBIT' : 'CREDIT';
  }

  private resolveFlowTitle(
    raw: BackendTradeWalletFlow,
    direction: string,
    counterpartyNickname: string | null,
    defaultBankCard: BackendBankCard | null,
  ): string {
    const tradeType = this.firstNonEmpty(raw.tradeType, 'UNKNOWN');
    const businessSceneCode = this.firstNonEmpty(raw.businessSceneCode, 'UNKNOWN');
    const providedTitle = this.firstNonEmpty(raw.displayTitle);
    if (businessSceneCode === 'FUND_FAST_REDEEM') {
      return this.resolveAiCashRedeemFlowTitle(providedTitle, defaultBankCard);
    }
    if (counterpartyNickname) {
      if (tradeType === 'TRANSFER') {
        return direction === 'DEBIT' ? `向${counterpartyNickname}转账` : `收到${counterpartyNickname}转账`;
      }
      if (providedTitle) {
        return providedTitle.replace(/用户\d{1,20}/g, counterpartyNickname);
      }
    }
    if (providedTitle) {
      return providedTitle;
    }
    return this.fallbackFlowTitle(tradeType, direction, businessSceneCode);
  }

  private fallbackFlowTitle(tradeType: string, direction: string, businessSceneCode: string): string {
    if (businessSceneCode === 'FUND_FAST_REDEEM') {
      return '爱存转出至余额';
    }
    if (tradeType === 'TRANSFER') {
      return direction === 'DEBIT' ? '转账支出' : '转账收入';
    }
    if (tradeType === 'PAY') {
      return '支付消费';
    }
    if (tradeType === 'WITHDRAW') {
      return '余额提现';
    }
    if (tradeType === 'DEPOSIT') {
      return '余额充值';
    }
    if (tradeType === 'REFUND') {
      return '退款入账';
    }
    return direction === 'DEBIT' ? '余额扣减' : '余额增加';
  }

  private resolveAiCashRedeemFlowTitle(providedTitle: string, defaultBankCard: BackendBankCard | null): string {
    const normalizedTitle = providedTitle.trim();
    const upperTitle = normalizedTitle.toUpperCase();
    if (normalizedTitle.includes('余额') || upperTitle.includes('WALLET')) {
      return '爱存转出至余额';
    }
    const bankNameFromTitle = this.extractBankNameFromText(normalizedTitle);
    if (bankNameFromTitle) {
      return `爱存转出至${bankNameFromTitle}`;
    }
    if (this.hasBankDestinationHint(normalizedTitle, upperTitle)) {
      const fallbackBankName = this.firstNonEmpty(defaultBankCard?.bankName);
      if (fallbackBankName) {
        return `爱存转出至${fallbackBankName}`;
      }
      return '爱存转出至银行卡';
    }
    return '爱存转出至余额';
  }

  private extractBankNameFromText(value: string): string | null {
    const normalized = value.trim();
    if (!normalized || normalized.includes('余额')) {
      return null;
    }
    const matches = normalized.match(/([A-Za-z0-9\u4e00-\u9fa5]{2,20}银行)/g);
    if (!matches || matches.length === 0) {
      return null;
    }
    const resolved = [...matches]
      .sort((lhs, rhs) => rhs.length - lhs.length)
      .find((item) => item.trim().length > 0 && item.trim() !== '银行');
    if (!resolved) {
      return null;
    }
    return this.normalizeRedeemBankName(resolved);
  }

  private normalizeRedeemBankName(rawBankName: string): string | null {
    const normalized = this.firstNonEmpty(rawBankName);
    if (!normalized) {
      return null;
    }
    let stripped = normalized.trim();
    while (true) {
      const next = stripped
        .replace(/^爱存转出至/, '')
        .replace(/^爱存转出到/, '')
        .replace(/^余额宝转出至/, '')
        .replace(/^余额宝转出到/, '')
        .replace(/^转出至/, '')
        .replace(/^转出到/, '')
        .replace(/^提现至/, '')
        .replace(/^提现到/, '')
        .trim();
      if (next === stripped) {
        break;
      }
      stripped = next;
    }
    stripped = stripped
      .replace(/^[：:\s]+/, '')
      .replace(/[：:\s]+$/, '');
    if (!stripped || stripped === '银行' || stripped === '银行卡') {
      return null;
    }
    return stripped;
  }

  private hasBankDestinationHint(normalizedTitle: string, upperTitle: string): boolean {
    return (
      normalizedTitle.includes('银行卡')
      || normalizedTitle.includes('银行')
      || normalizedTitle.includes('借记卡')
      || normalizedTitle.includes('储蓄卡')
      || normalizedTitle.includes('尾号')
      || upperTitle.includes('BANK')
      || upperTitle.includes('CARD')
    );
  }

  private async loadDefaultActiveBankCard(userId: string): Promise<BackendBankCard | null> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendBankCard[]>>(
        `/api/bankcards/users/${userId}/active`,
      );
      const cards = Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
      if (cards.length === 0) {
        return null;
      }
      const defaultCard = cards.find((card) => card.defaultCard);
      return defaultCard ?? cards[0] ?? null;
    } catch {
      return null;
    }
  }

  private async loadRecentWalletFlows(userId: string, limit: number): Promise<BackendTradeWalletFlow[]> {
    const response = await this.backendHttpService.get<BackendEnvelope<BackendTradeWalletFlow[]>>(
      `/api/trade/users/${userId}/recent-wallet-flows?limit=${limit}`,
    );
    return Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
  }

  private requiresBankCardMetadata(items: BackendTradeWalletFlow[]): boolean {
    return items.some((item) => {
      const tradeType = this.firstNonEmpty(item.tradeType, 'UNKNOWN');
      return tradeType === 'WITHDRAW' || tradeType === 'DEPOSIT';
    });
  }

  private normalizeSignedAmount(raw: string | number | undefined, direction: string, businessSceneCode: string): string {
    const parsedNumber =
      typeof raw === 'number'
        ? raw
        : typeof raw === 'string' && raw.trim().length > 0
          ? Number(raw.trim())
          : Number.NaN;
    if (Number.isFinite(parsedNumber)) {
      const absText = Math.abs(parsedNumber).toFixed(2);
      if (businessSceneCode === 'FUND_FAST_REDEEM') {
        return absText;
      }
      if (parsedNumber < 0) {
        return `-${absText}`;
      }
      if (parsedNumber > 0) {
        return direction === 'DEBIT' ? `-${absText}` : absText;
      }
      return '0.00';
    }
    return '0.00';
  }

  private normalizeDateTime(raw: string | undefined): string {
    if (!raw || raw.trim().length === 0) {
      return new Date().toISOString().replace('T', ' ').slice(0, 19);
    }
    const normalized = raw.trim().replace('T', ' ').replace('Z', '');
    const plain = normalized.includes('.') ? normalized.split('.')[0] : normalized;
    if (plain.length >= 19) {
      return plain.slice(0, 19);
    }
    return plain;
  }

  private normalizeAvatarUrl(raw: string | undefined): string {
    const normalized = this.backendHttpService.resolveAbsoluteUrl(raw);
    if (normalized.includes('api.dicebear.com') && normalized.includes('/svg?')) {
      return normalized.replace('/svg?', '/png?');
    }
    return normalized;
  }

  private defaultCounterpartyAvatarUrl(counterpartyUserId: string): string {
    const seed = encodeURIComponent(counterpartyUserId);
    return `https://api.dicebear.com/9.x/fun-emoji/png?seed=${seed}&backgroundColor=b6e3f4,c0aede,d1d4f9`;
  }

  private normalizeUserId(raw: string | number | undefined, fallback: string): string {
    const normalized = normalizePositiveLongId(raw, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    return fallback;
  }

  private normalizeUserIdInput(value: string): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'userId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private normalizeOptionalUserId(raw: string | number | undefined): string {
    return normalizePositiveLongId(raw, { minDigits: 1, maxDigits: 20 });
  }

  private extractCardTail(raw: string | number | undefined): string {
    if (typeof raw === 'number' && Number.isSafeInteger(raw) && raw > 0) {
      const digits = Math.trunc(raw).toString();
      return digits.length >= 4 ? digits.slice(-4) : '';
    }
    if (typeof raw === 'string') {
      const digits = raw.replace(/\D/g, '');
      return digits.length >= 4 ? digits.slice(-4) : '';
    }
    return '';
  }

  private normalizeAmount(raw: string | number | undefined): string {
    if (typeof raw === 'number' && Number.isFinite(raw)) {
      return raw.toFixed(2);
    }
    if (typeof raw === 'string' && raw.trim().length > 0) {
      const parsed = Number(raw);
      if (Number.isFinite(parsed)) {
        return parsed.toFixed(2);
      }
    }
    return '0.00';
  }

  private normalizeOptionalAmount(raw: string | number | undefined): string {
    if (raw === undefined || raw === null) {
      return '';
    }
    const normalized = this.normalizeAmount(raw);
    const parsed = Number(normalized);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return '';
    }
    return parsed.toFixed(2);
  }

  private resolveChangesCurrencyCode(items: BackendTradeWalletFlow[]): string {
    for (const item of items) {
      const currencyCode = this.firstNonEmpty(item.currencyCode);
      if (currencyCode) {
        return currencyCode;
      }
    }
    return 'CNY';
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
