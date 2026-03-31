import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { rethrowMappedUpstreamError } from '../common/upstream';

/**
 * 账单服务。
 *
 * 说明：
 * - 统一收口账单查询参数归一化、上游请求与字段清洗；
 * - 与 AssetService 解耦，避免资产域承担账单聚合职责。
 */
@Injectable()
export class BillService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getUserBillEntries(
    userId: string,
    pageNo: number,
    pageSize: number,
    billMonth?: string,
    businessDomainCode?: string,
    cursor?: string,
  ): Promise<Record<string, unknown>> {
    const normalizedUserId = this.normalizeUserIdInput(userId);
    const normalizedPageNo = Math.max(1, Math.trunc(pageNo));
    const normalizedPageSize = Math.max(1, Math.min(Math.trunc(pageSize), 100));
    const normalizedBillMonth = this.normalizeBillMonth(billMonth);
    const normalizedBusinessDomainCode = this.normalizeBusinessDomainCode(businessDomainCode);
    const normalizedCursor = this.normalizeCursor(cursor);

    const query = new URLSearchParams();
    query.set('pageNo', String(normalizedPageNo));
    query.set('pageSize', String(normalizedPageSize));
    if (normalizedBillMonth) {
      query.set('billMonth', normalizedBillMonth);
    }
    if (normalizedBusinessDomainCode) {
      query.set('businessDomainCode', normalizedBusinessDomainCode);
    }
    if (normalizedCursor) {
      query.set('cursorTradeTime', normalizedCursor.tradeTime);
      query.set('cursorId', String(normalizedCursor.cursorId));
    }
    const path = `/api/bill/users/${normalizedUserId}/entries/page?${query.toString()}`;

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendTradeBillEntryPage>>(path);
      const pageData = response.data?.data;
      const entries = Array.isArray(pageData?.items) ? pageData.items : [];
      const mappedItems: BillEntryView[] = entries.map((entry) => {
        const businessDomainCode = this.normalizeBrandCode(this.firstNonEmpty(entry.businessDomainCode));
        const businessType = this.firstNonEmpty(entry.businessType);
        const rawDisplayTitle = this.firstNonEmpty(entry.displayTitle);
        const rawDisplaySubtitle = this.firstNonEmpty(entry.displaySubtitle);
        const displayTitle = this.resolveBillEntryDisplayTitle(
          businessDomainCode,
          businessType,
          rawDisplayTitle,
          rawDisplaySubtitle,
        );
        const displaySubtitle = this.resolveBillEntryDisplaySubtitle(
          businessDomainCode,
          businessType,
          rawDisplayTitle,
          rawDisplaySubtitle,
        );
        return {
          tradeNo: this.firstNonEmpty(entry.tradeNo, entry.tradeOrderNo),
          businessDomainCode,
          bizOrderNo: this.firstNonEmpty(entry.bizOrderNo),
          productType: this.normalizeBrandCode(this.firstNonEmpty(entry.productType)),
          businessType,
          direction: this.normalizeDirection(entry.direction),
          tradeType: this.normalizeTradeType(entry.tradeType),
          accountNo: this.firstNonEmpty(entry.accountNo),
          billNo: this.firstNonEmpty(entry.billNo),
          billMonth: this.firstNonEmpty(entry.billMonth, normalizedBillMonth ?? ''),
          displayTitle,
          displaySubtitle,
          amount: this.normalizeBillEntryAmount(entry.amount, businessDomainCode, businessType, displayTitle),
          couponDiscountAmount: this.normalizeOptionalAmount(
            entry.couponDiscountAmount ?? entry.discountAmount ?? entry.couponDeductAmount,
          ),
          currencyCode: this.firstNonEmpty(entry.currencyCode, 'CNY'),
          status: this.firstNonEmpty(entry.status, 'UNKNOWN'),
          tradeTime: this.normalizeDateTime(entry.tradeTime),
        };
      });
      const visibleItems = mappedItems.filter((entry) => !this.shouldHideBillEntry(entry));
      const items = this.deduplicateBillEntries(visibleItems);
      const resolvedPageNo = this.normalizePageInteger(pageData?.pageNo, normalizedPageNo) ?? normalizedPageNo;
      const resolvedPageSize = this.normalizePageInteger(pageData?.pageSize, normalizedPageSize) ?? normalizedPageSize;
      const resolvedHasMore = this.normalizeBooleanValue(pageData?.hasMore, false);
      const resolvedNextPageNo = this.normalizePageInteger(pageData?.nextPageNo, null);
      const nextCursor = this.buildCursorToken(pageData?.nextCursorTradeTime, pageData?.nextCursorId);

      return {
        userId: normalizedUserId,
        billMonth: normalizedBillMonth ?? '',
        businessDomainCode: normalizedBusinessDomainCode ?? '',
        generatedAt: new Date().toISOString(),
        items,
        pageNo: resolvedPageNo,
        pageSize: resolvedPageSize,
        hasMore: resolvedHasMore,
        nextPageNo: resolvedNextPageNo,
        cursor: normalizedCursor?.token ?? '',
        nextCursor: nextCursor ?? '',
      };
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询账单失败');
    }
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

  private normalizeBillMonth(raw: string | undefined): string | null {
    if (!raw || raw.trim().length === 0) {
      return null;
    }
    const normalized = raw.trim();
    if (!/^\d{4}-\d{2}$/.test(normalized)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'billMonth 参数格式必须是 yyyy-MM',
      });
    }
    return normalized;
  }

  private normalizeBusinessDomainCode(raw: string | undefined): string | null {
    if (!raw || raw.trim().length === 0) {
      return null;
    }
    return this.normalizeBrandCode(raw);
  }

  private normalizeCursor(raw: string | undefined): NormalizedCursor | null {
    const normalizedRaw = this.firstNonEmpty(raw);
    if (!normalizedRaw) {
      return null;
    }
    const segments = normalizedRaw.split('|');
    if (segments.length !== 2) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'cursor 参数格式必须是 yyyy-MM-dd HH:mm:ss|id',
      });
    }
    const normalizedTradeTime = this.normalizeCursorTradeTime(segments[0] ?? '');
    const normalizedCursorId = this.normalizePositiveLong(segments[1] ?? '');
    return {
      tradeTime: normalizedTradeTime,
      cursorId: normalizedCursorId,
      token: `${normalizedTradeTime}|${normalizedCursorId}`,
    };
  }

  private normalizeCursorTradeTime(raw: string): string {
    const normalized = raw.trim().replace('T', ' ').replace('Z', '');
    const plain = normalized.includes('.') ? normalized.split('.')[0] ?? normalized : normalized;
    if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(plain)) {
      return plain;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'cursor 时间格式必须是 yyyy-MM-dd HH:mm:ss',
    });
  }

  private normalizePositiveLong(raw: string): number {
    if (/^\d+$/.test(raw.trim())) {
      const parsed = Number.parseInt(raw.trim(), 10);
      if (Number.isSafeInteger(parsed) && parsed > 0) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'cursor id 必须是正整数',
    });
  }

  private buildCursorToken(
    rawTradeTime: string | null | undefined,
    rawCursorId: string | number | null | undefined,
  ): string | null {
    if (rawTradeTime === undefined || rawTradeTime === null || rawCursorId === undefined || rawCursorId === null) {
      return null;
    }
    const normalizedTradeTime = this.normalizeDateTime(rawTradeTime);
    if (!/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(normalizedTradeTime)) {
      return null;
    }
    const normalizedCursorId = this.normalizePageInteger(rawCursorId, null);
    if (normalizedCursorId === null || normalizedCursorId <= 0) {
      return null;
    }
    return `${normalizedTradeTime}|${normalizedCursorId}`;
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
    if (raw === undefined) {
      return '';
    }
    const normalized = this.normalizeAmount(raw);
    const parsed = Number(normalized);
    if (!Number.isFinite(parsed)) {
      return '';
    }
    if (Math.abs(parsed) <= 0) {
      return '';
    }
    return Math.abs(parsed).toFixed(2);
  }

  private normalizeBillEntryAmount(
    raw: string | number | undefined,
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
  ): string {
    const normalizedAmount = this.normalizeAmount(raw);
    if (!this.shouldPresentBillEntryAsCredit(businessDomainCode, businessType, displayTitle)) {
      return normalizedAmount;
    }
    const parsed = Number(normalizedAmount);
    if (!Number.isFinite(parsed)) {
      return '0.00';
    }
    return Math.abs(parsed).toFixed(2);
  }

  private normalizeDirection(raw: string | undefined): string {
    const normalized = this.firstNonEmpty(raw).toUpperCase();
    if (normalized === 'DEBIT' || normalized === 'CREDIT') {
      return normalized;
    }
    return '';
  }

  private normalizeTradeType(raw: string | undefined): string {
    return this.firstNonEmpty(raw).toUpperCase();
  }

  private resolveBillEntryDisplayTitle(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): string {
    if (this.isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
      return '爱存收益发放';
    }
    const aiCashRedeemTitle = this.resolveAiCashRedeemBillTitle(
      businessDomainCode,
      businessType,
      displayTitle,
      displaySubtitle,
    );
    if (aiCashRedeemTitle) {
      return aiCashRedeemTitle;
    }
    const aiLoanDrawTitle = this.resolveAiLoanDrawBillTitle(
      businessDomainCode,
      businessType,
      displayTitle,
      displaySubtitle,
    );
    if (aiLoanDrawTitle) {
      return aiLoanDrawTitle;
    }
    return displayTitle;
  }

  private resolveBillEntryDisplaySubtitle(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): string {
    if (this.isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
      return '投资理财';
    }
    return displaySubtitle;
  }

  private shouldPresentBillEntryAsCredit(businessDomainCode: string, businessType: string, displayTitle: string): boolean {
    if (this.isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, '')) {
      return true;
    }
    const normalizedBusinessType = this.normalizeBrandCode(businessType);
    if (
      normalizedBusinessType.includes('FUND_SUBSCRIBE') ||
      normalizedBusinessType.includes('AICASH_TRANSFER_IN')
    ) {
      return true;
    }
    const normalizedDisplayTitle = displayTitle.trim();
    if (normalizedDisplayTitle === '爱存-单次转入') {
      return true;
    }
    return normalizedDisplayTitle.includes('爱存') && normalizedDisplayTitle.includes('单次转入');
  }

  private shouldHideBillEntry(entry: BillEntryView): boolean {
    const businessType = entry.businessType.trim().toUpperCase();
    const businessDomainCode = entry.businessDomainCode.trim().toUpperCase();
    const productType = entry.productType.trim().toUpperCase();
    const displayText = `${entry.displayTitle} ${entry.displaySubtitle}`.trim().toUpperCase();

    if (businessType.includes('FUND_PAY_FREEZE')) {
      return true;
    }
    if (businessType.includes('PAY_FREEZE') && (businessType.includes('FUND') || businessType.includes('AICASH'))) {
      return true;
    }
    if (businessDomainCode.includes('AICASH') && businessType.includes('PAY_FREEZE')) {
      return true;
    }
    if (productType.includes('AICASH') && businessType.includes('PAY_FREEZE')) {
      return true;
    }
    return displayText.includes('爱存申购冻结');
  }

  private isAiCashIncomeSettleBillEntry(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): boolean {
    if (this.normalizeBrandCode(businessDomainCode) !== 'AICASH') {
      return false;
    }
    const combined = [businessType, displayTitle, displaySubtitle]
      .map((item) => this.firstNonEmpty(item).toUpperCase())
      .join(' ');
    return (
      combined.includes('YIELD_SETTLE') ||
      combined.includes('INCOME_SETTLE') ||
      combined.includes('YIELD') ||
      combined.includes('收益发放') ||
      combined.includes('收益入账')
    );
  }

  private resolveAiCashRedeemBillTitle(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): string | null {
    if (!this.isAiCashRedeemBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
      return null;
    }
    const bankName = this.extractBankName(displayTitle, displaySubtitle);
    if (bankName) {
      return `爱存转出至${bankName}`;
    }
    if (this.hasBankDestinationHint(displayTitle, displaySubtitle)) {
      return '爱存转出至银行卡';
    }
    return '爱存转出至余额';
  }

  private resolveAiLoanDrawBillTitle(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): string | null {
    if (!this.isAiLoanDrawBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
      return null;
    }
    const bankName = this.extractLoanDestinationBankName(displayTitle, displaySubtitle);
    if (bankName) {
      return `爱借放款至${bankName}`;
    }
    if (this.hasBankDestinationHint(displayTitle, displaySubtitle)) {
      return '爱借放款至银行卡';
    }
    if (this.hasWalletDestinationHint(displayTitle, displaySubtitle)) {
      return '爱借放款至余额';
    }
    return '爱借放款至余额';
  }

  private isAiLoanDrawBillEntry(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): boolean {
    if (this.normalizeBrandCode(businessDomainCode) !== 'AILOAN') {
      return false;
    }
    const combinedUpper = [businessType, displayTitle, displaySubtitle]
      .map((item) => this.firstNonEmpty(item).toUpperCase())
      .join(' ');
    if (combinedUpper.includes('REPAY') || displayTitle.includes('还款') || displaySubtitle.includes('还款')) {
      return false;
    }
    return (
      combinedUpper.includes('LOAN')
      || combinedUpper.includes('DRAW')
      || displayTitle.includes('借款')
      || displayTitle.includes('放款')
      || displaySubtitle.includes('借款')
      || displaySubtitle.includes('放款')
    );
  }

  private extractLoanDestinationBankName(displayTitle: string, displaySubtitle: string): string | null {
    const bankName = this.extractBankName(displayTitle, displaySubtitle);
    if (!bankName) {
      return null;
    }
    const normalized = this.normalizeLoanDestinationBankName(bankName);
    if (!normalized || normalized === '余额' || normalized === '银行卡') {
      return null;
    }
    return normalized;
  }

  private normalizeLoanDestinationBankName(raw: string): string | null {
    let stripped = this.firstNonEmpty(raw);
    if (!stripped) {
      return null;
    }
    while (true) {
      const next = stripped
        .replace(/^爱借放款至/, '')
        .replace(/^爱借放款到/, '')
        .replace(/^爱借借款至/, '')
        .replace(/^爱借借款到/, '')
        .replace(/^放款至/, '')
        .replace(/^放款到/, '')
        .replace(/^借款至/, '')
        .replace(/^借款到/, '')
        .replace(/^转入至/, '')
        .replace(/^转入到/, '')
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

  private isAiCashRedeemBillEntry(
    businessDomainCode: string,
    businessType: string,
    displayTitle: string,
    displaySubtitle: string,
  ): boolean {
    if (this.normalizeBrandCode(businessDomainCode) !== 'AICASH') {
      return false;
    }
    if (this.isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
      return false;
    }
    const normalizedBusinessType = businessType.trim().toUpperCase();
    const normalizedTitle = displayTitle.trim().toUpperCase();
    const normalizedSubtitle = displaySubtitle.trim().toUpperCase();
    if (normalizedBusinessType.includes('FUND_FAST_REDEEM') || normalizedBusinessType.includes('FUND_REDEEM')) {
      return true;
    }
    if (
      normalizedTitle.includes('FUND_FAST_REDEEM') ||
      normalizedTitle.includes('FUND_REDEEM') ||
      normalizedSubtitle.includes('FUND_FAST_REDEEM') ||
      normalizedSubtitle.includes('FUND_REDEEM')
    ) {
      return true;
    }
    return (
      (displayTitle.includes('爱存') && displayTitle.includes('转出')) ||
      (displaySubtitle.includes('爱存') && displaySubtitle.includes('转出'))
    );
  }

  private extractBankName(displayTitle: string, displaySubtitle: string): string | null {
    const candidates = [displayTitle, displaySubtitle];
    for (const candidateRaw of candidates) {
      const candidate = this.firstNonEmpty(candidateRaw);
      if (!candidate || candidate.includes('余额')) {
        continue;
      }
      const matches = candidate.match(/([A-Za-z0-9\u4e00-\u9fa5]{2,20}银行)/g);
      if (!matches || matches.length === 0) {
        continue;
      }
      const resolved = [...matches]
        .sort((lhs, rhs) => rhs.length - lhs.length)
        .find((item) => item.trim().length > 0 && item.trim() !== '银行');
      if (resolved) {
        const normalizedBankName = this.normalizeRedeemBankName(resolved);
        if (normalizedBankName) {
          return normalizedBankName;
        }
      }
    }
    return null;
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

  private hasBankDestinationHint(displayTitle: string, displaySubtitle: string): boolean {
    const title = this.firstNonEmpty(displayTitle);
    const subtitle = this.firstNonEmpty(displaySubtitle);
    const combined = `${title} ${subtitle}`.trim().toUpperCase();
    if (combined.includes('BANK_CARD') || combined.includes('BANK') || combined.includes('CARD')) {
      return true;
    }
    return (
      title.includes('银行卡')
      || title.includes('银行')
      || title.includes('借记卡')
      || title.includes('储蓄卡')
      || title.includes('尾号')
      || subtitle.includes('银行卡')
      || subtitle.includes('银行')
      || subtitle.includes('借记卡')
      || subtitle.includes('储蓄卡')
      || subtitle.includes('尾号')
    );
  }

  private hasWalletDestinationHint(displayTitle: string, displaySubtitle: string): boolean {
    const title = this.firstNonEmpty(displayTitle).toUpperCase();
    const subtitle = this.firstNonEmpty(displaySubtitle).toUpperCase();
    if (title.includes('余额') || subtitle.includes('余额')) {
      return true;
    }
    return title.includes('WALLET') || subtitle.includes('WALLET');
  }

  private deduplicateBillEntries(items: BillEntryView[]): BillEntryView[] {
    if (items.length <= 1) {
      return items;
    }
    const withUniqueTradeNo = this.deduplicateByTradeNo(items);
    return this.filterMobileTopUpCompanionRows(withUniqueTradeNo);
  }

  private deduplicateByTradeNo(items: BillEntryView[]): BillEntryView[] {
    const indexByTradeNo = new Map<string, number>();
    const result: BillEntryView[] = [];

    for (const item of items) {
      const normalizedTradeNo = item.tradeNo.trim().toUpperCase();
      if (!normalizedTradeNo) {
        result.push(item);
        continue;
      }
      const existingIndex = indexByTradeNo.get(normalizedTradeNo);
      if (existingIndex === undefined) {
        indexByTradeNo.set(normalizedTradeNo, result.length);
        result.push(item);
        continue;
      }
      const existing = result[existingIndex];
      result[existingIndex] = this.pickPreferredBillEntry(existing, item);
    }

    return result;
  }

  private pickPreferredBillEntry(lhs: BillEntryView, rhs: BillEntryView): BillEntryView {
    const lhsScore = this.billEntryCompletenessScore(lhs);
    const rhsScore = this.billEntryCompletenessScore(rhs);
    let preferred = lhs;
    let companion = rhs;
    if (rhsScore > lhsScore) {
      preferred = rhs;
      companion = lhs;
    } else if (rhsScore == lhsScore) {
      const lhsTradeTime = this.parseTradeTimeMillis(lhs.tradeTime) ?? 0;
      const rhsTradeTime = this.parseTradeTimeMillis(rhs.tradeTime) ?? 0;
      if (rhsTradeTime >= lhsTradeTime) {
        preferred = rhs;
        companion = lhs;
      }
    }
    return this.mergeMobileTopUpPreferredEntry(preferred, companion);
  }

  private billEntryCompletenessScore(entry: BillEntryView): number {
    let score = 0;
    if (this.parseCouponAmount(entry.couponDiscountAmount) > 0) {
      score += 10;
    }
    if (!this.isGenericCategorySubtitle(entry.displaySubtitle)) {
      score += 5;
    }
    if (this.isSpecificMobileTopUpEntry(entry)) {
      score += 6;
    }
    if (!entry.displayTitle.startsWith('APP_')) {
      score += 2;
    }
    return score;
  }

  private filterMobileTopUpCompanionRows(items: BillEntryView[]): BillEntryView[] {
    if (items.length <= 1) {
      return items;
    }

    const normalizedItems = [...items];
    const removedIndexes = new Set<number>();
    for (let index = 0; index < normalizedItems.length; index += 1) {
      const row = normalizedItems[index];
      if (!row || !this.isGenericMobileTopUpCompanion(row)) {
        continue;
      }

      const preferredIndex = normalizedItems.findIndex((candidate, candidateIndex) => {
        if (candidateIndex === index || removedIndexes.has(candidateIndex) || !candidate) {
          return false;
        }
        if (!this.isSpecificMobileTopUpEntry(candidate)) {
          return false;
        }
        if (!this.belongsToSameMobileTopUpEvent(row, candidate)) {
          return false;
        }
        return this.isDuplicatedMobileTopUpAmountPair(row, candidate);
      });

      if (preferredIndex >= 0) {
        normalizedItems[preferredIndex] = this.mergeMobileTopUpPreferredEntry(normalizedItems[preferredIndex], row);
        removedIndexes.add(index);
      }
    }

    if (removedIndexes.size === 0) {
      return normalizedItems;
    }
    return normalizedItems.filter((_, index) => !removedIndexes.has(index));
  }

  private isMobileTopUpEntry(entry: BillEntryView): boolean {
    const businessType = entry.businessType.trim().toUpperCase();
    const tradeType = entry.tradeType.trim().toUpperCase();
    const title = entry.displayTitle.trim().toUpperCase();
    const subtitle = entry.displaySubtitle.trim().toUpperCase();

    if (businessType.includes('MOBILE_HALL_TOP_UP') || businessType.includes('APP_MOBILE')) {
      return true;
    }
    if (tradeType.includes('MOBILE') || tradeType.includes('TOP_UP') || tradeType.includes('TOPUP')) {
      return true;
    }
    if (title.includes('话费充值') || subtitle.includes('话费充值')) {
      return true;
    }
    return title.includes('MOBILE_HALL_TOP_UP') || subtitle.includes('MOBILE_HALL_TOP_UP');
  }

  private isSpecificMobileTopUpEntry(entry: BillEntryView): boolean {
    if (!this.isMobileTopUpEntry(entry)) {
      return false;
    }
    if (this.isGenericCategorySubtitle(entry.displaySubtitle)) {
      return false;
    }
    return true;
  }

  private isGenericMobileTopUpCompanion(entry: BillEntryView): boolean {
    if (!this.isMobileTopUpEntry(entry)) {
      return false;
    }
    return this.isGenericCategorySubtitle(entry.displaySubtitle);
  }

  private isGenericCategorySubtitle(value: string): boolean {
    const normalized = value.trim();
    return normalized === '其他' || normalized === '其它';
  }

  private belongsToSameMobileTopUpEvent(lhs: BillEntryView, rhs: BillEntryView): boolean {
    const lhsDirection = lhs.direction.trim().toUpperCase();
    const rhsDirection = rhs.direction.trim().toUpperCase();
    if (lhsDirection && rhsDirection && lhsDirection !== rhsDirection) {
      return false;
    }

    const lhsTime = this.parseTradeTimeMillis(lhs.tradeTime);
    const rhsTime = this.parseTradeTimeMillis(rhs.tradeTime);
    if (lhsTime === null || rhsTime === null) {
      return false;
    }
    if (Math.abs(lhsTime - rhsTime) > 300_000) {
      return false;
    }

    const lhsPhoneDigits = this.extractPhoneDigits(lhs.displayTitle);
    const rhsPhoneDigits = this.extractPhoneDigits(rhs.displayTitle);
    if (lhsPhoneDigits && rhsPhoneDigits) {
      return lhsPhoneDigits === rhsPhoneDigits;
    }

    const lhsTitle = lhs.displayTitle.trim().toUpperCase();
    const rhsTitle = rhs.displayTitle.trim().toUpperCase();
    return lhsTitle.length > 0 && lhsTitle === rhsTitle;
  }

  private isDuplicatedMobileTopUpAmountPair(lhs: BillEntryView, rhs: BillEntryView): boolean {
    const lhsAmount = this.parseAbsoluteAmount(lhs.amount);
    const rhsAmount = this.parseAbsoluteAmount(rhs.amount);
    if (lhsAmount === null || rhsAmount === null) {
      return false;
    }
    if (this.isAmountClose(lhsAmount, rhsAmount)) {
      return true;
    }

    const lhsCoupon = this.parseCouponAmount(lhs.couponDiscountAmount);
    if (lhsCoupon > 0 && this.isAmountClose(rhsAmount, lhsAmount + lhsCoupon)) {
      return true;
    }
    const rhsCoupon = this.parseCouponAmount(rhs.couponDiscountAmount);
    if (rhsCoupon > 0 && this.isAmountClose(lhsAmount, rhsAmount + rhsCoupon)) {
      return true;
    }
    const amountDelta = Math.abs(lhsAmount - rhsAmount);
    if (amountDelta <= 0) {
      return false;
    }
    if (!this.hasSameOrderIdentity(lhs, rhs) && !this.isStrongMobileTopUpCompanionMatch(lhs, rhs)) {
      return false;
    }
    return amountDelta <= 100;
  }

  private mergeMobileTopUpPreferredEntry(preferred: BillEntryView, companion: BillEntryView): BillEntryView {
    const preferredCoupon = this.parseCouponAmount(preferred.couponDiscountAmount);
    const companionCoupon = this.parseCouponAmount(companion.couponDiscountAmount);
    let mergedCoupon = Math.max(preferredCoupon, companionCoupon);

    if (
      mergedCoupon <= 0 &&
      (this.hasSameOrderIdentity(preferred, companion) || this.isStrongMobileTopUpCompanionMatch(preferred, companion))
    ) {
      const preferredAmount = this.parseAbsoluteAmount(preferred.amount);
      const companionAmount = this.parseAbsoluteAmount(companion.amount);
      if (preferredAmount !== null && companionAmount !== null) {
        const amountDelta = Math.abs(companionAmount - preferredAmount);
        if (amountDelta > 0 && amountDelta <= 100) {
          mergedCoupon = amountDelta;
        }
      }
    }

    if (mergedCoupon <= 0 || this.isAmountClose(mergedCoupon, preferredCoupon)) {
      return preferred;
    }
    return {
      ...preferred,
      couponDiscountAmount: mergedCoupon.toFixed(2),
    };
  }

  private hasSameOrderIdentity(lhs: BillEntryView, rhs: BillEntryView): boolean {
    const identityPairs: Array<[string, string]> = [
      [lhs.tradeNo, rhs.tradeNo],
      [lhs.bizOrderNo, rhs.bizOrderNo],
      [lhs.billNo, rhs.billNo],
    ];
    for (const [leftRaw, rightRaw] of identityPairs) {
      const left = leftRaw.trim().toUpperCase();
      const right = rightRaw.trim().toUpperCase();
      if (left && right && left === right) {
        return true;
      }
    }
    return false;
  }

  private isStrongMobileTopUpCompanionMatch(lhs: BillEntryView, rhs: BillEntryView): boolean {
    const lhsPhoneDigits = this.extractPhoneDigits(lhs.displayTitle);
    const rhsPhoneDigits = this.extractPhoneDigits(rhs.displayTitle);
    if (!lhsPhoneDigits || !rhsPhoneDigits || lhsPhoneDigits !== rhsPhoneDigits) {
      return false;
    }
    const lhsTime = this.parseTradeTimeMillis(lhs.tradeTime);
    const rhsTime = this.parseTradeTimeMillis(rhs.tradeTime);
    if (lhsTime === null || rhsTime === null) {
      return false;
    }
    return Math.abs(lhsTime - rhsTime) <= 30_000;
  }

  private parseTradeTimeMillis(raw: string): number | null {
    const normalized = raw.trim();
    if (!normalized) {
      return null;
    }
    const isoLike = normalized.includes('T') ? normalized : normalized.replace(' ', 'T');
    const parsed = Date.parse(isoLike);
    return Number.isFinite(parsed) ? parsed : null;
  }

  private extractPhoneDigits(raw: string): string {
    const digits = raw.replace(/\D/g, '');
    if (digits.length < 11) {
      return '';
    }
    return digits.slice(0, 11);
  }

  private parseAbsoluteAmount(raw: string): number | null {
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return null;
    }
    return Math.abs(parsed);
  }

  private parseCouponAmount(raw: string): number {
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return 0;
    }
    return Math.abs(parsed);
  }

  private isAmountClose(lhs: number, rhs: number): boolean {
    return Math.abs(lhs - rhs) < 0.01;
  }

  private normalizeDateTime(raw: string | null | undefined): string {
    if (!raw || raw.trim().length === 0) {
      return new Date().toISOString().replace('T', ' ').slice(0, 19);
    }
    const normalized = raw.trim().replace('T', ' ').replace('Z', '');
    const plain = normalized.includes('.') ? normalized.split('.')[0] ?? normalized : normalized;
    if (plain.length >= 19) {
      return plain.slice(0, 19);
    }
    return plain;
  }

  private normalizePageInteger(raw: string | number | null | undefined, fallback: number | null): number | null {
    if (typeof raw === 'number' && Number.isFinite(raw) && raw > 0) {
      return Math.trunc(raw);
    }
    if (typeof raw === 'string' && /^\d+$/.test(raw.trim())) {
      const parsed = Number.parseInt(raw.trim(), 10);
      if (parsed > 0) {
        return parsed;
      }
    }
    return fallback;
  }

  private normalizeBooleanValue(raw: boolean | string | number | null | undefined, fallback: boolean): boolean {
    if (typeof raw === 'boolean') {
      return raw;
    }
    if (typeof raw === 'number') {
      return raw !== 0;
    }
    if (typeof raw === 'string') {
      const normalized = raw.trim().toLowerCase();
      if (normalized === 'true' || normalized === '1') {
        return true;
      }
      if (normalized === 'false' || normalized === '0') {
        return false;
      }
    }
    return fallback;
  }

  private firstNonEmpty(...values: Array<string | undefined | null>): string {
    for (const value of values) {
      if (value && value.trim().length > 0) {
        return value.trim();
      }
    }
    return '';
  }

  private normalizeBrandCode(raw: string | undefined | null): string {
    const normalized = (raw ?? '').trim().toUpperCase();
    if (!normalized) {
      return '';
    }
    return normalized.replace(/AICASH/g, 'AICASH').replace(/AICREDIT/g, 'AICREDIT').replace(/AILOAN/g, 'AILOAN');
  }
}

type BackendErrorBody = {
  code?: string;
  message?: string;
};

type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: BackendErrorBody;
};

type BackendTradeBillEntry = {
  tradeNo?: string;
  tradeOrderNo?: string;
  businessDomainCode?: string;
  bizOrderNo?: string;
  productType?: string;
  businessType?: string;
  direction?: string;
  tradeType?: string;
  accountNo?: string;
  billNo?: string;
  billMonth?: string;
  displayTitle?: string;
  displaySubtitle?: string;
  amount?: string | number;
  couponDiscountAmount?: string | number;
  discountAmount?: string | number;
  couponDeductAmount?: string | number;
  currencyCode?: string;
  status?: string;
  tradeTime?: string;
};

type BackendTradeBillEntryPage = {
  items?: BackendTradeBillEntry[];
  pageNo?: string | number;
  pageSize?: string | number;
  hasMore?: boolean | string | number;
  nextPageNo?: string | number | null;
  nextCursorTradeTime?: string | null;
  nextCursorId?: string | number | null;
};

type NormalizedCursor = {
  tradeTime: string;
  cursorId: number;
  token: string;
};

type BillEntryView = {
  tradeNo: string;
  businessDomainCode: string;
  bizOrderNo: string;
  productType: string;
  businessType: string;
  direction: string;
  tradeType: string;
  accountNo: string;
  billNo: string;
  billMonth: string;
  displayTitle: string;
  displaySubtitle: string;
  amount: string;
  couponDiscountAmount: string;
  currencyCode: string;
  status: string;
  tradeTime: string;
};
