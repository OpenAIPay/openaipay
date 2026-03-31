import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendFeedbackTicket = {
  feedbackNo?: unknown;
  userId?: unknown;
  feedbackType?: unknown;
  sourceChannel?: unknown;
  sourcePageCode?: unknown;
  title?: unknown;
  content?: unknown;
  contactMobile?: unknown;
  attachmentUrls?: unknown;
  status?: unknown;
  handledBy?: unknown;
  handleNote?: unknown;
  handledAt?: unknown;
  closedAt?: unknown;
  createdAt?: unknown;
  updatedAt?: unknown;
};

@Injectable()
export class FeedbackService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async submitTicket(payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    const userId = this.assertRequiredUserId(payload.userId, 'userId');
    const upstreamPayload = this.buildFeedbackPayload(payload, {
      userId,
    });
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendFeedbackTicket>, Record<string, unknown>>(
        '/api/feedback/tickets',
        upstreamPayload,
      );
      const ticket = unwrapBackendData(response, '上游服务未返回反馈单结果');
      return this.normalizeFeedbackTicket(ticket, {
        userId,
        feedbackType: upstreamPayload.feedbackType as string,
        content: upstreamPayload.content as string,
      });
    } catch (error) {
      rethrowMappedUpstreamError(error, '提交反馈失败');
    }
  }

  private buildFeedbackPayload(
    payload: Record<string, unknown>,
    required: {
      userId: string;
    },
  ): Record<string, unknown> {
    const normalizedFeedbackType = this.normalizeOptionalUppercaseText(payload.feedbackType, 32) ?? 'PRODUCT_SUGGESTION';
    const normalizedSourceChannel = this.normalizeOptionalUppercaseText(payload.sourceChannel, 32) ?? 'IOS_APP';
    const normalizedSourcePageCode = this.normalizeOptionalUppercaseText(payload.sourcePageCode, 64) ?? 'SETTINGS_PRODUCT_SUGGESTION';
    const normalizedContent = this.assertRequiredString(payload.content, 'content', 200);
    if (normalizedContent.length < 10) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'content 长度必须在10到200之间',
      });
    }

    const upstreamPayload: Record<string, unknown> = {
      userId: required.userId,
      feedbackType: normalizedFeedbackType,
      sourceChannel: normalizedSourceChannel,
      sourcePageCode: normalizedSourcePageCode,
      content: normalizedContent,
    };

    this.assignOptionalText(upstreamPayload, 'title', payload.title, 128);
    this.assignOptionalText(upstreamPayload, 'contactMobile', payload.contactMobile, 32);
    const attachments = this.normalizeAttachmentUrls(payload.attachmentUrls);
    if (attachments.length > 0) {
      upstreamPayload.attachmentUrls = attachments;
    }
    return upstreamPayload;
  }

  private normalizeFeedbackTicket(
    payload: BackendFeedbackTicket,
    fallback: {
      userId: string;
      feedbackType: string;
      content: string;
    },
  ): Record<string, unknown> {
    return {
      feedbackNo: this.normalizeOptionalText(payload?.feedbackNo, 64) ?? '',
      userId: this.normalizeUserId(payload?.userId) ?? fallback.userId,
      feedbackType: this.normalizeOptionalUppercaseText(payload?.feedbackType, 32) ?? fallback.feedbackType,
      sourceChannel: this.normalizeOptionalUppercaseText(payload?.sourceChannel, 32),
      sourcePageCode: this.normalizeOptionalUppercaseText(payload?.sourcePageCode, 64),
      title: this.normalizeOptionalText(payload?.title, 128),
      content: this.normalizeOptionalText(payload?.content, 200) ?? fallback.content,
      contactMobile: this.normalizeOptionalText(payload?.contactMobile, 32),
      attachmentUrls: this.normalizeAttachmentUrls(payload?.attachmentUrls),
      status: this.normalizeOptionalUppercaseText(payload?.status, 32) ?? 'PENDING',
      handledBy: this.normalizeOptionalText(payload?.handledBy, 64),
      handleNote: this.normalizeOptionalText(payload?.handleNote, 255),
      handledAt: this.normalizeOptionalText(payload?.handledAt, 64),
      closedAt: this.normalizeOptionalText(payload?.closedAt, 64),
      createdAt: this.normalizeOptionalText(payload?.createdAt, 64),
      updatedAt: this.normalizeOptionalText(payload?.updatedAt, 64),
    };
  }

  private assertRequiredString(value: unknown, field: string, maxLength: number): string {
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim().slice(0, maxLength);
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

  private normalizeUserId(value: unknown): string | undefined {
    return normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 }) || undefined;
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

  private normalizeOptionalUppercaseText(value: unknown, maxLength: number): string | undefined {
    const normalized = this.normalizeOptionalText(value, maxLength);
    return normalized ? normalized.toUpperCase() : undefined;
  }

  private normalizeAttachmentUrls(value: unknown): string[] {
    if (!Array.isArray(value)) {
      return [];
    }
    return value
      .filter((item): item is string => typeof item === 'string')
      .map((item) => item.trim())
      .filter((item) => item.length > 0)
      .slice(0, 4)
      .map((item) => item.slice(0, 512));
  }

  private assignOptionalText(target: Record<string, unknown>, field: string, value: unknown, maxLength: number): void {
    const normalized = this.normalizeOptionalText(value, maxLength);
    if (normalized !== undefined) {
      target[field] = normalized;
    }
  }
}
