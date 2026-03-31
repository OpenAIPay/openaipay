import { BadRequestException, Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { MessageService } from './message.service';

@Controller('bff/messages')
export class MessageController {
  constructor(private readonly messageService: MessageService) {}

  @Get('users/:userId/home')
  async getMessageHome(@Param('userId') userId?: unknown, @Query('limit') limit?: unknown) {
    return this.messageService.getMessageHome(this.assertUserId(userId), this.normalizeLimit(limit, 100, 30));
  }

  @Get('conversations/:conversationNo/messages')
  async getConversationMessages(
    @Param('conversationNo') conversationNo?: string,
    @Query('userId') userId?: unknown,
    @Query('beforeMessageId') beforeMessageId?: string,
    @Query('limit') limit?: unknown,
  ) {
    const normalizedConversationNo = conversationNo?.trim() ?? '';
    if (!normalizedConversationNo) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'conversationNo 参数不能为空',
      });
    }
    return this.messageService.getConversationMessages(
      this.assertUserId(userId),
      normalizedConversationNo,
      beforeMessageId?.trim(),
      this.normalizeLimit(limit, 200, 100),
    );
  }

  @Get('red-packets/history')
  async getRedPacketHistory(
    @Query('userId') userId?: unknown,
    @Query('direction') direction?: string,
    @Query('year') year?: unknown,
    @Query('limit') limit?: unknown,
  ) {
    const normalizedYear = this.normalizeOptionalInteger(year, 2000, 2100, 'year');
    return this.messageService.getRedPacketHistory(
      this.assertUserId(userId),
      direction,
      normalizedYear,
      this.normalizeLimit(limit, 200, 100),
    );
  }

  @Post('text')
  async sendText(@Body() payload?: Record<string, unknown>) {
    return this.messageService.sendText(this.assertPayload(payload));
  }

  @Post('image')
  async sendImage(@Body() payload?: Record<string, unknown>) {
    return this.messageService.sendImage(this.assertPayload(payload));
  }

  @Post('red-packet')
  async sendRedPacket(@Body() payload?: Record<string, unknown>) {
    return this.messageService.sendRedPacket(this.assertPayload(payload));
  }

  @Post('transfer')
  async sendTransfer(@Body() payload?: Record<string, unknown>) {
    return this.messageService.sendTransfer(this.assertPayload(payload));
  }

  @Get('red-packets/:redPacketNo')
  async getRedPacketDetail(@Param('redPacketNo') redPacketNo?: string, @Query('userId') userId?: unknown) {
    const normalizedRedPacketNo = redPacketNo?.trim() ?? '';
    if (!normalizedRedPacketNo) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'redPacketNo 参数不能为空',
      });
    }
    return this.messageService.getRedPacketDetail(normalizedRedPacketNo, this.assertUserId(userId));
  }

  @Post('red-packets/:redPacketNo/claim')
  async claimRedPacket(@Param('redPacketNo') redPacketNo?: string, @Body() payload?: Record<string, unknown>) {
    const normalizedRedPacketNo = redPacketNo?.trim() ?? '';
    if (!normalizedRedPacketNo) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'redPacketNo 参数不能为空',
      });
    }
    return this.messageService.claimRedPacket(normalizedRedPacketNo, this.assertPayload(payload));
  }

  @Post('conversations/read')
  async markConversationRead(@Body() payload?: Record<string, unknown>) {
    return this.messageService.markConversationRead(this.assertPayload(payload));
  }

  private assertUserId(value: unknown): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'userId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private normalizeLimit(value: unknown, max: number, fallback: number): number {
    if (value === undefined || value === null || value === '') {
      return fallback;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= max) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0 && parsed <= max) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `limit 参数必须是 1-${max} 的整数`,
    });
  }

  private normalizeOptionalInteger(value: unknown, min: number, max: number, field: string): number | undefined {
    if (value === undefined || value === null || value === '') {
      return undefined;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value >= min && value <= max) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed >= min && parsed <= max) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 参数必须是 ${min}-${max} 的整数`,
    });
  }

  private assertPayload(payload?: Record<string, unknown>): Record<string, unknown> {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return payload;
  }
}
