import { BadGatewayException, BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import {
  firstNonEmpty,
  normalizeAvatarUrl,
  normalizeFirstAvailableUserId,
  normalizeOptionalUserId,
  normalizeUserId,
} from '../common/data-normalizer';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError } from '../common/upstream';

type BackendConversation = {
  conversationNo?: string;
  conversationType?: string;
  userId?: string | number;
  peerUserId?: string | number;
  peerAipayUid?: string;
  peerNickname?: string;
  peerAvatarUrl?: string;
  unreadCount?: string | number;
  lastMessageId?: string;
  lastMessagePreview?: string;
  lastMessageAt?: string;
  updatedAt?: string;
};

type BackendMessage = {
  messageId?: string;
  conversationNo?: string;
  senderUserId?: string | number;
  receiverUserId?: string | number;
  messageType?: string;
  contentText?: string;
  mediaId?: string;
  amount?: unknown;
  tradeNo?: string;
  extPayload?: string;
  messageStatus?: string;
  createdAt?: string;
};

type BackendRedPacketHistory = {
  userId?: unknown;
  direction?: unknown;
  year?: unknown;
  totalCount?: unknown;
  totalAmount?: unknown;
  items?: unknown;
};

type BackendRedPacketHistoryItem = {
  messageId?: unknown;
  conversationNo?: unknown;
  direction?: unknown;
  counterpartyUserId?: unknown;
  counterpartyNickname?: unknown;
  counterpartyAvatarUrl?: unknown;
  amount?: unknown;
  tradeNo?: unknown;
  messageStatus?: unknown;
  redPacketNo?: unknown;
  redPacketStatus?: unknown;
  createdAt?: unknown;
  senderUserId?: unknown;
  receiverUserId?: unknown;
  peerUserId?: unknown;
};

type BackendRedPacketDetail = {
  redPacketNo?: unknown;
  messageId?: unknown;
  conversationNo?: unknown;
  senderUserId?: unknown;
  senderNickname?: unknown;
  senderAvatarUrl?: unknown;
  receiverUserId?: unknown;
  receiverNickname?: unknown;
  receiverAvatarUrl?: unknown;
  holdingUserId?: unknown;
  amount?: unknown;
  paymentMethod?: unknown;
  coverId?: unknown;
  coverTitle?: unknown;
  blessingText?: unknown;
  status?: unknown;
  fundingTradeNo?: unknown;
  claimTradeNo?: unknown;
  claimableByViewer?: unknown;
  claimedByViewer?: unknown;
  claimedAt?: unknown;
  createdAt?: unknown;
};

type MessageAmountPayload = {
  amount: string;
  currencyCode: string;
  currencyUnit: {
    code: string;
  };
};

type MessageAmountRequestPayload = {
  amount: string;
  currencyCode: string;
};

@Injectable()
export class MessageService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getMessageHome(userId: string, limit: number): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendConversation[]> | string>(
        `/api/conversations/users/${userId}?limit=${limit}`,
        this.losslessJsonRequestOptions(),
      );
      const envelope = this.parseBackendEnvelope<BackendConversation[]>(response.data);
      this.assertEnvelopeSucceeded(envelope, '查询消息首页失败');

      const conversations = (Array.isArray(envelope.data) ? envelope.data : [])
        .map((item) => this.normalizeConversation(item, userId))
        .filter((item): item is Record<string, unknown> => item !== null);

      const unreadTotal = conversations.reduce((sum, item) => {
        if (typeof item.unreadCount === 'number') {
          return sum + item.unreadCount;
        }
        return sum;
      }, 0);

      return {
        userId,
        unreadTotal,
        conversations,
        generatedAt: new Date().toISOString(),
      };
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询消息首页失败');
    }
  }

  async getConversationMessages(
    userId: string,
    conversationNo: string,
    beforeMessageId: string | undefined,
    limit: number,
  ): Promise<Record<string, unknown>> {
    const queryItems = [
      `userId=${encodeURIComponent(userId)}`,
      `limit=${limit}`,
      beforeMessageId ? `beforeMessageId=${encodeURIComponent(beforeMessageId)}` : '',
    ].filter((item) => item.length > 0);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendMessage[]> | string>(
        `/api/messages/conversations/${encodeURIComponent(conversationNo)}?${queryItems.join('&')}`,
        this.losslessJsonRequestOptions(),
      );
      const envelope = this.parseBackendEnvelope<BackendMessage[]>(response.data);
      this.assertEnvelopeSucceeded(envelope, '查询会话消息失败');

      const items = (Array.isArray(envelope.data) ? envelope.data : [])
        .map((item) => this.normalizeMessage(item, { conversationNo, userId }))
        .filter((item): item is Record<string, unknown> => item !== null);

      return {
        userId,
        conversationNo,
        items,
        generatedAt: new Date().toISOString(),
      };
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询会话消息失败');
    }
  }

  async markConversationRead(payload: Record<string, unknown>): Promise<null> {
    const userId = this.normalizeUserIdInput(payload.userId);
    const conversationNo = typeof payload.conversationNo === 'string' ? payload.conversationNo.trim() : '';
    if (!conversationNo) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'conversationNo 不能为空',
      });
    }

    const requestBody: Record<string, string> = {
      userId,
      conversationNo,
    };
    if (typeof payload.lastReadMessageId === 'string' && payload.lastReadMessageId.trim().length > 0) {
      requestBody.lastReadMessageId = payload.lastReadMessageId.trim();
    }

    try {
      await this.backendHttpService.post<BackendEnvelope<unknown>, Record<string, string>>('/api/conversations/read', requestBody);
      return null;
    } catch (error) {
      rethrowMappedUpstreamError(error, '标记已读失败');
    }
  }

  async getRedPacketHistory(
    userId: string,
    direction: string | undefined,
    year: number | undefined,
    limit: number,
  ): Promise<Record<string, unknown>> {
    const normalizedDirection = this.normalizeDirection(direction);
    const queryItems = [
      `userId=${encodeURIComponent(userId)}`,
      `direction=${encodeURIComponent(normalizedDirection)}`,
      typeof year === 'number' ? `year=${year}` : '',
      `limit=${limit}`,
    ].filter((item) => item.length > 0);

    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendRedPacketHistory> | string>(
        `/api/messages/red-packets/history?${queryItems.join('&')}`,
        this.losslessJsonRequestOptions(),
      );
      const envelope = this.parseBackendEnvelope<BackendRedPacketHistory>(response.data);
      this.assertEnvelopeSucceeded(envelope, '查询红包记录失败');
      if (envelope.data === undefined || envelope.data === null) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回红包记录数据',
        });
      }
      return this.normalizeRedPacketHistory(envelope.data, {
        userId,
        direction: normalizedDirection,
        year,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询红包记录失败');
    }
  }

  async sendText(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const senderUserId = this.assertRequiredUserId(payload.senderUserId, 'senderUserId');
    const receiverUserId = this.assertRequiredUserId(payload.receiverUserId, 'receiverUserId');
    const contentText = this.assertRequiredString(payload.contentText, 'contentText');
    const requestBody: Record<string, unknown> = {
      senderUserId,
      receiverUserId,
      contentText,
    };
    this.assignOptionalText(requestBody, 'extPayload', payload.extPayload);
    const result = await this.postMessageAction('/api/messages/text', requestBody, '发送文本消息失败', '上游服务未返回文本消息结果');
    return this.normalizeMessage(result, {
      conversationNo: '',
      userId: senderUserId,
      senderUserId,
      receiverUserId,
    });
  }

  async sendImage(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const senderUserId = this.assertRequiredUserId(payload.senderUserId, 'senderUserId');
    const receiverUserId = this.assertRequiredUserId(payload.receiverUserId, 'receiverUserId');
    const mediaId = this.assertRequiredString(payload.mediaId, 'mediaId');
    const requestBody: Record<string, unknown> = {
      senderUserId,
      receiverUserId,
      mediaId,
    };
    this.assignOptionalText(requestBody, 'extPayload', payload.extPayload);
    const result = await this.postMessageAction('/api/messages/image', requestBody, '发送图片消息失败', '上游服务未返回图片消息结果');
    return this.normalizeMessage(result, {
      conversationNo: '',
      userId: senderUserId,
      senderUserId,
      receiverUserId,
    });
  }

  async sendRedPacket(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const senderUserId = this.assertRequiredUserId(payload.senderUserId, 'senderUserId');
    const receiverUserId = this.assertRequiredUserId(payload.receiverUserId, 'receiverUserId');
    const paymentMethod = this.normalizeUppercaseCode(payload.paymentMethod, 'WALLET');
    const requestBody: Record<string, unknown> = {
      senderUserId,
      receiverUserId,
      amount: this.normalizeRequiredMessageAmount(payload.amount, 'amount'),
      paymentMethod,
    };
    this.assignOptionalText(requestBody, 'extPayload', payload.extPayload);
    const result = await this.postMessageAction('/api/messages/red-packet', requestBody, '发送红包失败', '上游服务未返回红包发送结果');
    return this.normalizeMessage(result, {
      conversationNo: '',
      userId: senderUserId,
      senderUserId,
      receiverUserId,
    });
  }

  async sendTransfer(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const senderUserId = this.assertRequiredUserId(payload.senderUserId, 'senderUserId');
    const receiverUserId = this.assertRequiredUserId(payload.receiverUserId, 'receiverUserId');
    const requestBody: Record<string, unknown> = {
      senderUserId,
      receiverUserId,
      amount: this.normalizeRequiredMessageAmount(payload.amount, 'amount'),
      paymentMethod: this.normalizeUppercaseCode(payload.paymentMethod, 'WALLET'),
    };
    this.assignOptionalText(requestBody, 'paymentToolCode', payload.paymentToolCode);
    this.assignOptionalText(requestBody, 'remark', payload.remark);
    this.assignOptionalText(requestBody, 'extPayload', payload.extPayload);
    const result = await this.postMessageAction('/api/messages/transfer', requestBody, '发送转账失败', '上游服务未返回转账消息结果');
    return this.normalizeMessage(result, {
      conversationNo: '',
      userId: senderUserId,
      senderUserId,
      receiverUserId,
    });
  }

  async getRedPacketDetail(redPacketNo: string, userId: string): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendRedPacketDetail> | string>(
        `/api/messages/red-packets/${encodeURIComponent(redPacketNo)}?userId=${encodeURIComponent(userId)}`,
        this.losslessJsonRequestOptions(),
      );
      const envelope = this.parseBackendEnvelope<BackendRedPacketDetail>(response.data);
      this.assertEnvelopeSucceeded(envelope, '查询红包详情失败');
      if (envelope.data === undefined || envelope.data === null) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回红包详情',
        });
      }
      return this.normalizeRedPacketDetail(envelope.data, {
        redPacketNo,
        userId,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询红包详情失败');
    }
  }

  async claimRedPacket(redPacketNo: string, payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const userId = this.assertRequiredUserId(payload.userId, 'userId');
    const result = await this.postMessageAction(
      `/api/messages/red-packets/${encodeURIComponent(redPacketNo)}/claim`,
      { userId },
      '领取红包失败',
      '上游服务未返回红包领取结果',
    );
    return this.normalizeRedPacketDetail(result, {
      redPacketNo,
      userId,
    });
  }

  private normalizeConversation(raw: BackendConversation, fallbackUserId: string): Record<string, unknown> | null {
    const userId = normalizeUserId(raw.userId, fallbackUserId);
    const peerUserId = normalizeFirstAvailableUserId(raw.peerUserId, raw.peerAipayUid) || fallbackUserId;
    return {
      conversationNo: firstNonEmpty(raw.conversationNo),
      conversationType: firstNonEmpty(raw.conversationType, 'PRIVATE'),
      userId,
      peerUserId,
      peerAipayUid: firstNonEmpty(raw.peerAipayUid, peerUserId),
      peerNickname: firstNonEmpty(raw.peerNickname, raw.peerAipayUid, peerUserId),
      peerAvatarUrl: normalizeAvatarUrl(this.backendHttpService, raw.peerAvatarUrl),
      unreadCount: this.normalizeCount(raw.unreadCount),
      lastMessageId: firstNonEmpty(raw.lastMessageId),
      lastMessagePreview: firstNonEmpty(raw.lastMessagePreview),
      lastMessageAt: firstNonEmpty(raw.lastMessageAt),
      updatedAt: firstNonEmpty(raw.updatedAt),
    };
  }

  private normalizeMessage(
    value: unknown,
    fallback: {
      conversationNo: string;
      userId: string;
      senderUserId?: string;
      receiverUserId?: string;
    },
  ): Record<string, unknown> {
    const raw = this.asRecord(value);
    const senderUserId = normalizeOptionalUserId(raw.senderUserId as string | number | undefined)
      || fallback.senderUserId
      || fallback.userId;
    const receiverUserId = normalizeOptionalUserId(raw.receiverUserId as string | number | undefined)
      || fallback.receiverUserId
      || fallback.userId;

    return {
      messageId: firstNonEmpty(raw.messageId as string | undefined),
      conversationNo: firstNonEmpty(raw.conversationNo as string | undefined, fallback.conversationNo),
      senderUserId,
      receiverUserId,
      messageType: firstNonEmpty(raw.messageType as string | undefined, 'TEXT'),
      contentText: firstNonEmpty(raw.contentText as string | undefined),
      mediaId: firstNonEmpty(raw.mediaId as string | undefined),
      amount: this.normalizeMessageAmount(raw.amount),
      tradeNo: firstNonEmpty(raw.tradeNo as string | undefined),
      extPayload: firstNonEmpty(raw.extPayload as string | undefined),
      messageStatus: firstNonEmpty(raw.messageStatus as string | undefined, 'SENT'),
      createdAt: firstNonEmpty(raw.createdAt as string | undefined),
    };
  }

  private normalizeRedPacketHistory(
    value: BackendRedPacketHistory,
    fallback: {
      userId: string;
      direction: string;
      year: number | undefined;
    },
  ): Record<string, unknown> {
    const raw = this.asRecord(value);
    const userId = normalizeOptionalUserId(raw.userId as string | number | undefined) || fallback.userId;
    const direction = this.normalizeDirection(this.normalizeOptionalText(raw.direction) ?? fallback.direction);
    const itemsRaw = Array.isArray(raw.items) ? raw.items : [];
    const items = itemsRaw.map((item) => this.normalizeRedPacketHistoryItem(item, userId, direction));
    return {
      userId,
      direction,
      year: this.normalizeOptionalInteger(raw.year) ?? fallback.year,
      totalCount: this.normalizeOptionalInteger(raw.totalCount) ?? items.length,
      totalAmount: this.normalizeMessageAmount(raw.totalAmount),
      items,
    };
  }

  private normalizeRedPacketHistoryItem(value: unknown, fallbackUserId: string, direction: string): Record<string, unknown> {
    const raw = this.asRecord(value) as BackendRedPacketHistoryItem;
    const counterpartyUserId = normalizePositiveLongId(
      raw.counterpartyUserId ?? raw.peerUserId ?? raw.senderUserId ?? raw.receiverUserId,
      { minDigits: 1, maxDigits: 20 },
    ) || fallbackUserId;
    return {
      messageId: this.normalizeOptionalText(raw.messageId) ?? '',
      conversationNo: this.normalizeOptionalText(raw.conversationNo) ?? '',
      direction: this.normalizeDirection(this.normalizeOptionalText(raw.direction) ?? direction),
      counterpartyUserId,
      counterpartyNickname: this.normalizeOptionalText(raw.counterpartyNickname) ?? '',
      counterpartyAvatarUrl: normalizeAvatarUrl(this.backendHttpService, this.normalizeOptionalText(raw.counterpartyAvatarUrl)),
      amount: this.normalizeMessageAmount(raw.amount),
      tradeNo: this.normalizeOptionalText(raw.tradeNo),
      messageStatus: this.normalizeOptionalText(raw.messageStatus) ?? 'SENT',
      redPacketNo: this.normalizeOptionalText(raw.redPacketNo),
      redPacketStatus: this.normalizeOptionalText(raw.redPacketStatus),
      createdAt: this.normalizeOptionalText(raw.createdAt),
    };
  }

  private normalizeRedPacketDetail(
    value: unknown,
    fallback: {
      redPacketNo: string;
      userId: string;
    },
  ): Record<string, unknown> {
    const raw = this.asRecord(value) as BackendRedPacketDetail;
    const senderUserId = normalizePositiveLongId(raw.senderUserId, { minDigits: 1, maxDigits: 20 }) || fallback.userId;
    const receiverUserId = normalizePositiveLongId(raw.receiverUserId, { minDigits: 1, maxDigits: 20 }) || fallback.userId;
    const holdingUserId = normalizePositiveLongId(raw.holdingUserId, { minDigits: 1, maxDigits: 20 }) || receiverUserId;
    return {
      redPacketNo: this.normalizeOptionalText(raw.redPacketNo) ?? fallback.redPacketNo,
      messageId: this.normalizeOptionalText(raw.messageId) ?? '',
      conversationNo: this.normalizeOptionalText(raw.conversationNo) ?? '',
      senderUserId,
      senderNickname: this.normalizeOptionalText(raw.senderNickname) ?? '',
      senderAvatarUrl: normalizeAvatarUrl(this.backendHttpService, this.normalizeOptionalText(raw.senderAvatarUrl)),
      receiverUserId,
      receiverNickname: this.normalizeOptionalText(raw.receiverNickname) ?? '',
      receiverAvatarUrl: normalizeAvatarUrl(this.backendHttpService, this.normalizeOptionalText(raw.receiverAvatarUrl)),
      holdingUserId,
      amount: this.normalizeMessageAmount(raw.amount),
      paymentMethod: this.normalizeUppercaseCode(raw.paymentMethod, 'WALLET'),
      coverId: this.normalizeOptionalText(raw.coverId),
      coverTitle: this.normalizeOptionalText(raw.coverTitle),
      blessingText: this.normalizeOptionalText(raw.blessingText),
      status: this.normalizeOptionalText(raw.status) ?? 'PENDING_CLAIM',
      fundingTradeNo: this.normalizeOptionalText(raw.fundingTradeNo),
      claimTradeNo: this.normalizeOptionalText(raw.claimTradeNo),
      claimableByViewer: this.normalizeBoolean(raw.claimableByViewer),
      claimedByViewer: this.normalizeBoolean(raw.claimedByViewer),
      claimedAt: this.normalizeOptionalText(raw.claimedAt),
      createdAt: this.normalizeOptionalText(raw.createdAt),
    };
  }

  private normalizeMessageAmount(value: unknown): MessageAmountPayload | undefined {
    if (value === undefined || value === null || value === '') {
      return undefined;
    }
    const raw = this.asRecord(value);
    const amount = this.normalizeDecimalText(raw.amount ?? raw.value ?? value) ?? '0';
    const nestedCurrencyUnit = raw.currencyUnit;
    const nestedCurrencyCode = nestedCurrencyUnit && typeof nestedCurrencyUnit === 'object' && !Array.isArray(nestedCurrencyUnit)
      ? this.normalizeOptionalText((nestedCurrencyUnit as Record<string, unknown>).code)
      : undefined;
    const currencyCode = this.normalizeUppercaseCode(
      raw.currencyCode ?? raw.currency ?? nestedCurrencyCode,
      'CNY',
    );
    return {
      amount,
      currencyCode,
      currencyUnit: {
        code: currencyCode,
      },
    };
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

  private normalizeUserIdInput(value: unknown): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'userId 参数格式不正确，请使用字符串传递18位ID',
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

  private assertRequiredString(value: unknown, field: string): string {
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }

  private normalizeRequiredMessageAmount(value: unknown, field: string): MessageAmountRequestPayload {
    const amount = this.normalizeDecimalText(value)
      ?? this.normalizeDecimalText(this.asRecord(value).amount ?? this.asRecord(value).value);
    if (!amount) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: `${field} 不能为空`,
      });
    }
    const raw = this.asRecord(value);
    const nestedCurrencyUnit = raw.currencyUnit;
    const nestedCurrencyCode = nestedCurrencyUnit && typeof nestedCurrencyUnit === 'object' && !Array.isArray(nestedCurrencyUnit)
      ? this.normalizeOptionalText((nestedCurrencyUnit as Record<string, unknown>).code)
      : undefined;
    const currencyCode = this.normalizeUppercaseCode(
      raw.currencyCode ?? raw.currency ?? nestedCurrencyCode,
      'CNY',
    );
    return {
      amount,
      currencyCode,
    };
  }

  private normalizeDirection(value: string | undefined): 'SENT' | 'RECEIVED' {
    const normalized = (value ?? '').trim().toUpperCase();
    return normalized === 'RECEIVED' ? 'RECEIVED' : 'SENT';
  }

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private normalizeUppercaseCode(value: unknown, fallback: string): string {
    const normalized = this.normalizeOptionalText(value)?.toUpperCase();
    return normalized && normalized.length > 0 ? normalized : fallback;
  }

  private normalizeDecimalText(value: unknown): string | undefined {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value.toString();
    }
    if (typeof value === 'string') {
      const normalized = value.trim();
      if (normalized.length > 0) {
        return normalized;
      }
    }
    return undefined;
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

  private asRecord(value: unknown): Record<string, unknown> {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return value as Record<string, unknown>;
    }
    return {};
  }

  private assignOptionalText(target: Record<string, unknown>, field: string, value: unknown): void {
    const normalized = this.normalizeOptionalText(value);
    if (normalized !== undefined) {
      target[field] = normalized;
    }
  }

  private async postMessageAction(
    path: string,
    payload: Record<string, unknown>,
    fallbackMessage: string,
    emptyMessage: string,
  ): Promise<unknown> {
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<unknown> | string, Record<string, unknown>>(
        path,
        payload,
        this.losslessJsonRequestOptions(),
      );
      const envelope = this.parseBackendEnvelope<unknown>(response.data);
      this.assertEnvelopeSucceeded(envelope, fallbackMessage);
      if (envelope.data === undefined || envelope.data === null) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: emptyMessage,
        });
      }
      return envelope.data;
    } catch (error) {
      rethrowMappedUpstreamError(error, fallbackMessage);
    }
  }

  private losslessJsonRequestOptions() {
    return {
      responseType: 'text' as const,
      transformResponse: [(value: string) => value],
    };
  }

  private parseBackendEnvelope<T>(rawData: unknown): BackendEnvelope<T> {
    if (typeof rawData === 'string') {
      const normalized = this.quoteLargeIntegersOutsideStrings(rawData);
      try {
        return JSON.parse(normalized) as BackendEnvelope<T>;
      } catch {
        return {};
      }
    }
    if (rawData && typeof rawData === 'object') {
      return rawData as BackendEnvelope<T>;
    }
    return {};
  }

  private assertEnvelopeSucceeded<T>(envelope: BackendEnvelope<T>, fallbackMessage: string): void {
    if (envelope.success === false) {
      throw new BadGatewayException({
        code: envelope.error?.code ?? 'UPSTREAM_ERROR',
        message: envelope.error?.message ?? fallbackMessage,
      });
    }
  }

  private quoteLargeIntegersOutsideStrings(raw: string): string {
    let index = 0;
    let inString = false;
    let escaped = false;
    let output = '';

    while (index < raw.length) {
      const current = raw[index];

      if (inString) {
        output += current;
        if (escaped) {
          escaped = false;
        } else if (current === '\\') {
          escaped = true;
        } else if (current === '"') {
          inString = false;
        }
        index += 1;
        continue;
      }

      if (current === '"') {
        inString = true;
        output += current;
        index += 1;
        continue;
      }

      if (current !== ':') {
        output += current;
        index += 1;
        continue;
      }

      output += current;
      index += 1;

      while (index < raw.length && /\s/.test(raw[index])) {
        output += raw[index];
        index += 1;
      }

      const numberStart = index;
      let signed = false;
      if (raw[index] === '-') {
        signed = true;
        index += 1;
      }

      const digitsStart = index;
      while (index < raw.length && raw[index] >= '0' && raw[index] <= '9') {
        index += 1;
      }
      const digits = raw.slice(digitsStart, index);

      if (digits.length >= 16) {
        let suffixIndex = index;
        while (suffixIndex < raw.length && /\s/.test(raw[suffixIndex])) {
          suffixIndex += 1;
        }
        const nextToken = raw[suffixIndex];
        if (nextToken === ',' || nextToken === '}' || nextToken === ']') {
          output += `"${signed ? '-' : ''}${digits}"`;
          continue;
        }
      }

      output += raw.slice(numberStart, index);
    }

    return output;
  }
}
