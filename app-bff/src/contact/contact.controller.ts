import { BadRequestException, Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { ContactService } from './contact.service';

@Controller('bff/contacts')
export class ContactController {
  constructor(private readonly contactService: ContactService) {}

  @Get('search')
  async searchContacts(@Query() query?: Record<string, unknown>) {
    const safeQuery = this.normalizeQuery(query);
    const normalizedOwnerUserId = this.assertOwnerUserId(safeQuery.ownerUserId);
    const normalizedKeyword = this.assertKeyword(safeQuery.keyword);
    const normalizedLimit = this.normalizeLimit(safeQuery.limit, 100, 30);
    return this.contactService.searchContacts(normalizedOwnerUserId, normalizedKeyword, normalizedLimit);
  }

  @Post('requests')
  async applyFriendRequest(@Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return this.contactService.applyFriendRequest(payload);
  }

  @Get('requests/received')
  async listReceivedFriendRequests(@Query() query?: Record<string, unknown>) {
    const safeQuery = this.normalizeQuery(query);
    const normalizedTargetUserId = this.assertTargetUserId(safeQuery.targetUserId);
    const normalizedLimit = this.normalizeLimit(safeQuery.limit, 100, 20);
    return this.contactService.listReceivedFriendRequests(normalizedTargetUserId, normalizedLimit);
  }

  @Get('requests/sent')
  async listSentFriendRequests(@Query() query?: Record<string, unknown>) {
    const safeQuery = this.normalizeQuery(query);
    const normalizedRequesterUserId = this.assertRequesterUserId(safeQuery.requesterUserId);
    const normalizedLimit = this.normalizeLimit(safeQuery.limit, 100, 20);
    return this.contactService.listSentFriendRequests(normalizedRequesterUserId, normalizedLimit);
  }

  @Post('requests/:requestNo/handle')
  async handleFriendRequest(@Param('requestNo') requestNo?: unknown, @Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    const normalizedRequestNo = this.assertRequestNo(requestNo);
    const normalizedOperatorUserId = this.assertOperatorUserId(payload.operatorUserId);
    const normalizedAction = this.assertHandleAction(payload.action);
    return this.contactService.handleFriendRequest(normalizedRequestNo, {
      operatorUserId: normalizedOperatorUserId,
      action: normalizedAction,
    });
  }

  private assertOwnerUserId(ownerUserId: unknown): string {
    const normalizedOwnerUserId = normalizePositiveLongId(ownerUserId, { minDigits: 1, maxDigits: 20 });
    if (normalizedOwnerUserId) {
      return normalizedOwnerUserId;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'ownerUserId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private assertTargetUserId(targetUserId: unknown): string {
    const normalizedTargetUserId = normalizePositiveLongId(targetUserId, { minDigits: 1, maxDigits: 20 });
    if (normalizedTargetUserId) {
      return normalizedTargetUserId;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'targetUserId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private assertRequesterUserId(requesterUserId: unknown): string {
    const normalizedRequesterUserId = normalizePositiveLongId(requesterUserId, { minDigits: 1, maxDigits: 20 });
    if (normalizedRequesterUserId) {
      return normalizedRequesterUserId;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'requesterUserId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private assertOperatorUserId(operatorUserId: unknown): string {
    const normalizedOperatorUserId = normalizePositiveLongId(operatorUserId, { minDigits: 1, maxDigits: 20 });
    if (normalizedOperatorUserId) {
      return normalizedOperatorUserId;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'operatorUserId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private assertRequestNo(value: unknown): string {
    if (typeof value === 'string') {
      const normalized = value.trim();
      if (normalized.length > 0) {
        return normalized;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'requestNo 参数不能为空',
    });
  }

  private assertHandleAction(value: unknown): 'ACCEPT' | 'REJECT' {
    if (typeof value === 'string') {
      const normalized = value.trim().toUpperCase();
      if (normalized === 'ACCEPT' || normalized === 'REJECT') {
        return normalized;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'action 参数必须为 ACCEPT 或 REJECT',
    });
  }

  private normalizeQuery(query: Record<string, unknown> | undefined): Record<string, unknown> {
    if (query && typeof query === 'object' && !Array.isArray(query)) {
      return query;
    }
    return {};
  }

  private assertKeyword(value: unknown): string {
    if (typeof value === 'number' && Number.isFinite(value)) {
      const normalizedNumberText = String(Math.trunc(value));
      if (normalizedNumberText.length > 0) {
        return normalizedNumberText;
      }
    }
    if (typeof value !== 'string') {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'keyword 参数不能为空',
      });
    }
    const normalized = value.trim();
    if (normalized.length > 0) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'keyword 参数不能为空',
    });
  }

  private normalizeLimit(value: unknown, max: number, fallback: number): number {
    if (value === undefined || value === null || value === '') {
      return fallback;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= max) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value.trim())) {
      const parsed = Number.parseInt(value.trim(), 10);
      if (parsed > 0 && parsed <= max) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `limit 参数必须是 1-${max} 的整数`,
    });
  }
}
