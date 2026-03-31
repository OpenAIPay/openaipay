import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { isUnsafePositiveLongNumber, normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

@Injectable()
export class AccountService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getCreditAccount(userId: string): Promise<unknown> {
    const payload = await this.getData(
      `/api/account-query/credit/users/${userId}`,
      '查询信用账户失败',
      '上游服务未返回信用账户数据',
    );
    return this.normalizeAccountOwnerUserId(payload, userId);
  }

  async getCreditCurrentBillDetail(userId: string): Promise<unknown> {
    return this.getData(
      `/api/account-query/credit/users/${userId}/current-bill-detail`,
      '查询当前账单明细失败',
      '上游服务未返回当前账单明细',
    );
  }

  async getCreditNextBillDetail(userId: string): Promise<unknown> {
    return this.getData(
      `/api/account-query/credit/users/${userId}/next-bill-detail`,
      '查询下期账单明细失败',
      '上游服务未返回下期账单明细',
    );
  }

  async getLoanAccount(userId: string): Promise<unknown> {
    const payload = await this.getData(
      `/api/account-query/loan/users/${userId}`,
      '查询借贷账户失败',
      '上游服务未返回借贷账户数据',
    );
    return this.normalizeAccountOwnerUserId(payload, userId);
  }

  async getFundAccount(userId: string): Promise<unknown> {
    const payload = await this.getData(
      `/api/account-query/fund/users/${userId}`,
      '查询基金账户失败',
      '上游服务未返回基金账户数据',
    );
    return this.normalizeAccountOwnerUserId(payload, userId);
  }

  async getFundOpenAgreementPack(userId: string, fundCode?: string, currencyCode?: string): Promise<unknown> {
    const query = new URLSearchParams();
    query.set('userId', userId);
    if (fundCode && fundCode.trim().length > 0) {
      query.set('fundCode', fundCode.trim());
    }
    if (currencyCode && currencyCode.trim().length > 0) {
      query.set('currencyCode', currencyCode.trim());
    }
    const payload = await this.getData(
      `/api/agreements/packs/fund-account-open?${query.toString()}`,
      '查询基金开通协议失败',
      '上游服务未返回基金开通协议包',
    );
    return this.normalizeFundAgreementResponseUserId(payload, userId);
  }

  async getCreditOpenAgreementPack(userId: string, productCode?: string): Promise<unknown> {
    const normalizedProductCode = this.normalizeCreditProductCode(productCode);
    const query = new URLSearchParams();
    query.set('userId', userId);
    query.set('productCode', normalizedProductCode);
    const payload = await this.getData(
      `/api/agreements/packs/credit-product-open?${query.toString()}`,
      '查询信用产品开通协议失败',
      '上游服务未返回信用产品开通协议包',
    );
    return this.normalizeCreditAgreementResponseUserId(payload, userId, normalizedProductCode);
  }

  async openFundAccountWithAgreement(payload: Record<string, unknown>): Promise<unknown> {
    const normalizedPayload = this.normalizeFundOpenAgreementPayload(payload);
    this.assertRequiredString(normalizedPayload.idempotencyKey, 'idempotencyKey');
    const normalizedUserId = this.normalizeFundOpenUserId(
      normalizedPayload.userId,
      normalizedPayload.idempotencyKey,
    );
    normalizedPayload.userId = normalizedUserId;
    normalizedPayload.agreementAccepts = await this.completeAgreementAccepts(
      normalizedPayload.agreementAccepts,
      normalizedUserId,
      this.normalizeOptionalScalar(normalizedPayload.fundCode) ?? 'AICASH',
      this.normalizeOptionalScalar(normalizedPayload.currencyCode) ?? 'CNY',
    );
    this.assertAgreementAccepts(normalizedPayload.agreementAccepts);
    const response = await this.postData(
      '/api/agreements/sign/fund-account-open',
      normalizedPayload,
      '签约并开通基金账户失败',
      '上游服务未返回签约开通结果',
    );
    return this.normalizeFundAgreementResponseUserId(response, normalizedUserId);
  }

  async openCreditProductWithAgreement(payload: Record<string, unknown>): Promise<unknown> {
    const normalizedPayload = this.normalizeCreditOpenAgreementPayload(payload);
    this.assertRequiredString(normalizedPayload.idempotencyKey, 'idempotencyKey');
    const normalizedProductCode = this.normalizeCreditProductCode(normalizedPayload.productCode);
    const normalizedUserId = this.normalizeCreditOpenUserId(
      normalizedPayload.userId,
      normalizedPayload.idempotencyKey,
    );
    normalizedPayload.userId = normalizedUserId;
    normalizedPayload.productCode = normalizedProductCode;
    normalizedPayload.agreementAccepts = await this.completeCreditAgreementAccepts(
      normalizedPayload.agreementAccepts,
      normalizedUserId,
      normalizedProductCode,
    );
    this.assertAgreementAccepts(normalizedPayload.agreementAccepts);
    const response = await this.postData(
      '/api/agreements/sign/credit-product-open',
      normalizedPayload,
      '签约并开通信用产品失败',
      '上游服务未返回签约开通结果',
    );
    return this.normalizeCreditAgreementResponseUserId(response, normalizedUserId, normalizedProductCode);
  }

  async createFundAccount(payload: Record<string, unknown>): Promise<unknown> {
    const userId = this.normalizeRequiredIdentifier(payload.userId, 'userId');
    const fundCode = this.assertRequiredString(payload.fundCode, 'fundCode');
    const currencyCode = this.assertRequiredString(payload.currencyCode, 'currencyCode');
    return this.postData(
      '/api/fund-accounts',
      this.buildCreateFundAccountPayload(payload, { userId, fundCode, currencyCode }),
      '创建基金账户失败',
      '上游服务未返回基金账户创建结果',
    );
  }

  async createFundSubscribe(payload: Record<string, unknown>): Promise<unknown> {
    const orderNo = this.assertRequiredString(payload.orderNo, 'orderNo');
    const userId = this.normalizeRequiredIdentifier(payload.userId, 'userId');
    const fundCode = this.assertRequiredString(payload.fundCode, 'fundCode');
    const amount = this.normalizeRequiredAmountText(payload.amount, 'amount');
    return this.postData(
      '/api/fund-accounts/subscribe',
      this.buildFundSubscribePayload(payload, {
        orderNo,
        userId,
        fundCode,
        amount,
      }),
      '基金申购失败',
      '上游服务未返回基金申购结果',
    );
  }

  async confirmFundSubscribe(payload: Record<string, unknown>): Promise<unknown> {
    const orderNo = this.assertRequiredString(payload.orderNo, 'orderNo');
    const confirmedShare = this.normalizeRequiredAmountText(payload.confirmedShare, 'confirmedShare');
    const nav = this.normalizeRequiredAmountText(payload.nav, 'nav');
    return this.postData(
      '/api/fund-accounts/subscribe/confirm',
      { orderNo, confirmedShare, nav },
      '基金申购确认失败',
      '上游服务未返回基金申购确认结果',
    );
  }

  async createFundFastRedeem(payload: Record<string, unknown>): Promise<unknown> {
    const orderNo = this.assertRequiredString(payload.orderNo, 'orderNo');
    const userId = this.normalizeRequiredIdentifier(payload.userId, 'userId');
    const fundCode = this.assertRequiredString(payload.fundCode, 'fundCode');
    const share = this.normalizeRequiredAmountText(payload.share, 'share');
    return this.postData(
      '/api/fund-accounts/fast-redeem',
      this.buildFundFastRedeemPayload(payload, {
        orderNo,
        userId,
        fundCode,
        share,
      }),
      '基金快速赎回失败',
      '上游服务未返回基金快速赎回结果',
    );
  }

  async confirmFundRedeem(payload: Record<string, unknown>): Promise<unknown> {
    const orderNo = this.assertRequiredString(payload.orderNo, 'orderNo');
    return this.postData(
      '/api/fund-accounts/redeem/confirm',
      { orderNo },
      '基金转出确认失败',
      '上游服务未返回基金转出确认结果',
    );
  }

  private async getData(path: string, fallbackMessage: string, emptyMessage: string): Promise<unknown> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<unknown>>(path);
      return unwrapBackendData(response, emptyMessage);
    } catch (error) {
      rethrowMappedUpstreamError(error, fallbackMessage);
    }
  }

  private async postData(
    path: string,
    payload: Record<string, unknown>,
    fallbackMessage: string,
    emptyMessage: string,
  ): Promise<unknown> {
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<unknown>, Record<string, unknown>>(path, payload);
      return unwrapBackendData(response, emptyMessage);
    } catch (error) {
      rethrowMappedUpstreamError(error, fallbackMessage);
    }
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

  private assertRequiredStringOrNumber(value: unknown, field: string): void {
    if (typeof value === 'string' && value.trim().length > 0) {
      return;
    }
    if (typeof value === 'number' && Number.isFinite(value) && !isUnsafePositiveLongNumber(value)) {
      return;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private assertAgreementAccepts(value: unknown): void {
    if (!Array.isArray(value) || value.length === 0) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'agreementAccepts 不能为空',
      });
    }
    for (const item of value) {
      if (!item || typeof item !== 'object' || Array.isArray(item)) {
        throw new BadRequestException({
          code: 'INVALID_ARGUMENT',
          message: 'agreementAccepts 格式不正确',
        });
      }
      const templateCode = this.readObjectValue(item as Record<string, unknown>, ['templateCode', 'template_code']);
      const templateVersion = this.readObjectValue(item as Record<string, unknown>, ['templateVersion', 'template_version']);
      this.assertRequiredString(templateCode, 'agreementAccepts.templateCode');
      this.assertRequiredString(templateVersion, 'agreementAccepts.templateVersion');
    }
  }

  private normalizeRequiredIdentifier(value: unknown, field: string): string {
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (trimmed.length > 0) {
        return trimmed;
      }
    }
    if (isUnsafePositiveLongNumber(value)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: `${field} 精度丢失，请传字符串`,
      });
    }
    const normalizedLongId = normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 });
    if (normalizedLongId) {
      return normalizedLongId;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private normalizeFundOpenUserId(userId: unknown, idempotencyKey: unknown): string {
    if (isUnsafePositiveLongNumber(userId)) {
      const recovered = this.extractUserIdFromIdempotencyKey(idempotencyKey);
      if (recovered) {
        return recovered;
      }
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'userId 精度丢失，请传字符串',
      });
    }
    return this.normalizeRequiredIdentifier(userId, 'userId');
  }

  private extractUserIdFromIdempotencyKey(value: unknown): string | null {
    if (typeof value !== 'string') {
      return null;
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }
    const fundOpenMatch = trimmed.match(/(?:^|_)FUND_OPEN_(\d{6,20})(?:_|$)/i);
    if (fundOpenMatch?.[1]) {
      return fundOpenMatch[1];
    }
    const creditOpenMatch = trimmed.match(/(?:^|_)(?:AUTO_REG|AICREDIT_OPEN|AILOAN_OPEN|AICREDIT_OPEN|AILOAN_OPEN)_(\d{6,20})(?:_|$)/i);
    if (creditOpenMatch?.[1]) {
      return creditOpenMatch[1];
    }
    return null;
  }

  private normalizeFundOpenAgreementPayload(payload: Record<string, unknown>): Record<string, unknown> {
    const agreementAccepts = Array.isArray(payload.agreementAccepts)
      ? payload.agreementAccepts
          .filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === 'object' && !Array.isArray(item))
          .map((item) => ({
            templateCode: this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code'])),
              templateVersion: this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version'])),
            }))
      : payload.agreementAccepts;

    const normalizedPayload: Record<string, unknown> = {
      userId: payload.userId,
      fundCode: this.normalizeOptionalScalar(this.readObjectValue(payload, ['fundCode', 'fund_code'])),
      currencyCode: this.normalizeOptionalScalar(this.readObjectValue(payload, ['currencyCode', 'currency_code'])),
      idempotencyKey: this.normalizeOptionalScalar(this.readObjectValue(payload, ['idempotencyKey', 'idempotency_key'])),
      agreementAccepts,
    };
    return normalizedPayload;
  }

  private normalizeCreditOpenAgreementPayload(payload: Record<string, unknown>): Record<string, unknown> {
    const agreementAccepts = Array.isArray(payload.agreementAccepts)
      ? payload.agreementAccepts
          .filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === 'object' && !Array.isArray(item))
          .map((item) => ({
            templateCode: this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code'])),
              templateVersion: this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version'])),
            }))
      : payload.agreementAccepts;

    const normalizedPayload: Record<string, unknown> = {
      userId: payload.userId,
      productCode: this.normalizeOptionalScalar(this.readObjectValue(payload, ['productCode', 'product_code'])),
      idempotencyKey: this.normalizeOptionalScalar(this.readObjectValue(payload, ['idempotencyKey', 'idempotency_key'])),
      agreementAccepts,
    };
    return normalizedPayload;
  }

  private normalizeFundAgreementResponseUserId(payload: unknown, requestedUserId: string): unknown {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      return payload;
    }
    return {
      ...(payload as Record<string, unknown>),
      userId: requestedUserId,
    };
  }

  private normalizeCreditAgreementResponseUserId(
    payload: unknown,
    requestedUserId: string,
    productCode: string,
  ): unknown {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      return payload;
    }
    return {
      ...(payload as Record<string, unknown>),
      userId: requestedUserId,
      productCode,
    };
  }

  private normalizeAccountOwnerUserId(payload: unknown, requestedUserId: string): unknown {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      return payload;
    }
    return {
      ...(payload as Record<string, unknown>),
      userId: requestedUserId,
    };
  }

  private readObjectValue(record: Record<string, unknown>, candidateKeys: string[]): unknown {
    for (const key of candidateKeys) {
      if (Object.prototype.hasOwnProperty.call(record, key)) {
        return record[key];
      }
    }
    const normalizedCandidates = new Set(candidateKeys.map((key) => key.replace(/[_-]/g, '').toLowerCase()));
    for (const [rawKey, rawValue] of Object.entries(record)) {
      const normalizedKey = rawKey.replace(/[_-]/g, '').toLowerCase();
      if (normalizedCandidates.has(normalizedKey)) {
        return rawValue;
      }
    }
    return undefined;
  }

  private async completeAgreementAccepts(
    value: unknown,
    userId: string,
    fundCode: string,
    currencyCode: string,
  ): Promise<unknown> {
    const normalizedItems = Array.isArray(value)
      ? value.filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === 'object' && !Array.isArray(item))
      : [];
    const shouldFetchPack =
      normalizedItems.length === 0 ||
      normalizedItems.some((item) => {
        const templateCode = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code']));
        const templateVersion = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version']));
        return !templateCode || !templateVersion;
      });

    if (!shouldFetchPack) {
      return value;
    }

    const query = new URLSearchParams();
    query.set('userId', userId);
    query.set('fundCode', fundCode);
    query.set('currencyCode', currencyCode);
    const packPayload = await this.getData(
      `/api/agreements/packs/fund-account-open?${query.toString()}`,
      '查询基金开通协议失败',
      '上游服务未返回基金开通协议包',
    );
    const packAgreements = this.extractPackAgreements(packPayload);
    if (packAgreements.length === 0) {
      return value;
    }

    const packByCode = new Map(
      packAgreements
        .filter((item) => item.templateCode)
        .map((item) => [item.templateCode.toUpperCase(), item] as const),
    );
    const completedItems = normalizedItems
      .map((item) => {
        const templateCode = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code']));
        const templateVersion = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version']));
        const matched = templateCode ? packByCode.get(templateCode.toUpperCase()) : undefined;
        return {
          templateCode: templateCode ?? matched?.templateCode ?? '',
          templateVersion: templateVersion ?? matched?.templateVersion ?? '',
        };
      })
      .filter((item) => item.templateCode || item.templateVersion);

    if (completedItems.length > 0) {
      const validCompletedItems = completedItems.filter((item) => item.templateCode && item.templateVersion);
      if (validCompletedItems.length > 0) {
        return validCompletedItems;
      }
    }

    return packAgreements
      .filter((item) => item.required)
      .map((item) => ({
        templateCode: item.templateCode,
        templateVersion: item.templateVersion,
      }));
  }

  private async completeCreditAgreementAccepts(
    value: unknown,
    userId: string,
    productCode: string,
  ): Promise<unknown> {
    const normalizedItems = Array.isArray(value)
      ? value.filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === 'object' && !Array.isArray(item))
      : [];
    const shouldFetchPack =
      normalizedItems.length === 0 ||
      normalizedItems.some((item) => {
        const templateCode = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code']));
        const templateVersion = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version']));
        return !templateCode || !templateVersion;
      });

    if (!shouldFetchPack) {
      return value;
    }

    const query = new URLSearchParams();
    query.set('userId', userId);
    query.set('productCode', productCode);
    const packPayload = await this.getData(
      `/api/agreements/packs/credit-product-open?${query.toString()}`,
      '查询信用产品开通协议失败',
      '上游服务未返回信用产品开通协议包',
    );
    const packAgreements = this.extractPackAgreements(packPayload);
    if (packAgreements.length === 0) {
      return value;
    }

    const packByCode = new Map(
      packAgreements
        .filter((item) => item.templateCode)
        .map((item) => [item.templateCode.toUpperCase(), item] as const),
    );
    const completedItems = normalizedItems
      .map((item) => {
        const templateCode = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code']));
        const templateVersion = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version']));
        const matched = templateCode ? packByCode.get(templateCode.toUpperCase()) : undefined;
        return {
          templateCode: templateCode ?? matched?.templateCode ?? '',
          templateVersion: templateVersion ?? matched?.templateVersion ?? '',
        };
      })
      .filter((item) => item.templateCode || item.templateVersion);

    if (completedItems.length > 0) {
      const validCompletedItems = completedItems.filter((item) => item.templateCode && item.templateVersion);
      if (validCompletedItems.length > 0) {
        return validCompletedItems;
      }
    }

    return packAgreements
      .filter((item) => item.required)
      .map((item) => ({
        templateCode: item.templateCode,
        templateVersion: item.templateVersion,
      }));
  }

  private extractPackAgreements(payload: unknown): Array<{
    templateCode: string;
    templateVersion: string;
    required: boolean;
  }> {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      return [];
    }
    const agreements = (payload as Record<string, unknown>).agreements;
    if (!Array.isArray(agreements)) {
      return [];
    }
    return agreements
      .filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === 'object' && !Array.isArray(item))
      .map((item) => {
        const templateCode = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateCode', 'template_code'])) ?? '';
        const templateVersion = this.normalizeOptionalScalar(this.readObjectValue(item, ['templateVersion', 'template_version'])) ?? '';
        const requiredRaw = this.readObjectValue(item, ['required', 'required_flag']);
        return {
          templateCode,
          templateVersion,
          required: this.normalizeBoolean(requiredRaw),
        };
      })
      .filter((item) => item.templateCode && item.templateVersion);
  }

  private normalizeBoolean(value: unknown): boolean {
    if (typeof value === 'boolean') {
      return value;
    }
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value !== 0;
    }
    if (typeof value === 'string') {
      const normalized = value.trim().toLowerCase();
      return normalized === '1' || normalized === 'true' || normalized === 'yes';
    }
    return false;
  }

  private normalizeOptionalScalar(value: unknown): string | undefined {
    if (typeof value === 'string') {
      const trimmed = value.trim();
      return trimmed.length > 0 ? trimmed : '';
    }
    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value);
    }
    return undefined;
  }

  private normalizeCreditProductCode(value: unknown): string {
    const normalized = (this.normalizeOptionalScalar(value) ?? 'AICREDIT').trim().toUpperCase();
    switch (normalized) {
      case 'AICREDIT':
      case 'AICREDIT':
        return 'AICREDIT';
      case 'AILOAN':
      case 'AILOAN':
        return 'AILOAN';
      default:
        throw new BadRequestException({
          code: 'INVALID_ARGUMENT',
          message: 'productCode 不支持',
        });
    }
  }


  private normalizeCreditOpenUserId(userId: unknown, idempotencyKey: unknown): string {
    if (isUnsafePositiveLongNumber(userId)) {
      const recovered = this.extractUserIdFromIdempotencyKey(idempotencyKey);
      if (recovered) {
        return recovered;
      }
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'userId 精度丢失，请传字符串',
      });
    }
    return this.normalizeRequiredIdentifier(userId, 'userId');
  }

  private buildCreateFundAccountPayload(
    payload: Record<string, unknown>,
    required: {
      userId: string;
      fundCode: string;
      currencyCode: string;
    },
  ): Record<string, unknown> {
    return {
      userId: required.userId,
      fundCode: required.fundCode,
      currencyCode: required.currencyCode,
      ...(this.normalizeOptionalText(payload.businessNo) ? { businessNo: this.normalizeOptionalText(payload.businessNo) } : {}),
    };
  }

  private buildFundSubscribePayload(
    payload: Record<string, unknown>,
    required: {
      orderNo: string;
      userId: string;
      fundCode: string;
      amount: string;
    },
  ): Record<string, unknown> {
    const requestPayload: Record<string, unknown> = {
      orderNo: required.orderNo,
      userId: required.userId,
      fundCode: required.fundCode,
      amount: required.amount,
    };
    const businessNo = this.normalizeOptionalText(payload.businessNo);
    if (businessNo) {
      requestPayload.businessNo = businessNo;
    }
    return requestPayload;
  }

  private buildFundFastRedeemPayload(
    payload: Record<string, unknown>,
    required: {
      orderNo: string;
      userId: string;
      fundCode: string;
      share: string;
    },
  ): Record<string, unknown> {
    const requestPayload: Record<string, unknown> = {
      orderNo: required.orderNo,
      userId: required.userId,
      fundCode: required.fundCode,
      share: required.share,
    };
    const businessNo = this.normalizeOptionalText(payload.businessNo);
    if (businessNo) {
      requestPayload.businessNo = businessNo;
    }
    const bankName = this.normalizeOptionalText(payload.bankName);
    if (bankName) {
      requestPayload.bankName = bankName;
    }
    const redeemDestination = this.normalizeRedeemDestination(payload.redeemDestination, bankName);
    if (redeemDestination) {
      requestPayload.redeemDestination = redeemDestination;
    }
    return requestPayload;
  }

  private normalizeRequiredAmountText(value: unknown, field: string): string {
    if (typeof value === 'number' && Number.isFinite(value) && !isUnsafePositiveLongNumber(value)) {
      return value.toString();
    }
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (trimmed.length > 0) {
        return trimmed;
      }
    }
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      const record = value as Record<string, unknown>;
      return this.normalizeRequiredAmountText(record.amount ?? record.value, field);
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private normalizeRedeemDestination(value: unknown, bankName?: string): string | undefined {
    const normalized = this.normalizeOptionalText(value);
    if (normalized) {
      const upper = normalized.toUpperCase();
      if (upper === 'BALANCE' || normalized.includes('余额')) {
        return 'BALANCE';
      }
      if (upper === 'BANK_CARD' || upper.includes('BANK') || normalized.includes('银行卡') || normalized.includes('银行')) {
        return 'BANK_CARD';
      }
    }
    if (bankName && this.hasBankDestinationHint(bankName)) {
      return 'BANK_CARD';
    }
    return undefined;
  }

  private hasBankDestinationHint(value: string): boolean {
    const normalized = value.trim();
    if (!normalized) {
      return false;
    }
    const upper = normalized.toUpperCase();
    return (
      upper.includes('BANK')
      || upper.includes('CARD')
      || normalized.includes('银行卡')
      || normalized.includes('银行')
      || normalized.includes('借记卡')
      || normalized.includes('储蓄卡')
      || normalized.includes('尾号')
    );
  }
}
