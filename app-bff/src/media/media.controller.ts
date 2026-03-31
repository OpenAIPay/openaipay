import { BadRequestException, Controller, Get, Param, Post, Query, Req, Res } from '@nestjs/common';
import type { Request, Response } from 'express';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { RawResponse } from '../common/raw-response.decorator';
import { MediaService } from './media.service';

@Controller('bff/media')
export class MediaController {
  constructor(private readonly mediaService: MediaService) {}

  @Post('images/upload')
  async uploadImage(@Req() req: Request, @Query('ownerUserId') ownerUserId?: unknown) {
    return this.mediaService.uploadImage(req, this.assertUserId(ownerUserId));
  }

  @Get('owners/:ownerUserId')
  async listOwnerMedia(@Param('ownerUserId') ownerUserId?: unknown, @Query('limit') limit?: unknown) {
    return this.mediaService.listOwnerMedia(this.assertUserId(ownerUserId), this.normalizeLimit(limit, 200, 20));
  }

  @Get(':mediaId/content')
  @RawResponse()
  async loadContent(@Param('mediaId') mediaId?: string, @Res() res?: Response) {
    if (!res) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '响应对象不可用',
      });
    }
    await this.mediaService.loadContent(this.assertMediaId(mediaId), res);
  }

  @Get(':mediaId')
  async getMedia(@Param('mediaId') mediaId?: string) {
    return this.mediaService.getMedia(this.assertMediaId(mediaId));
  }

  private assertMediaId(value?: string): string {
    const normalized = value?.trim() ?? '';
    if (normalized.length > 0) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'mediaId 参数不能为空',
    });
  }

  private assertUserId(value: unknown): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'ownerUserId 参数格式不正确，请使用字符串传递18位ID',
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
}
