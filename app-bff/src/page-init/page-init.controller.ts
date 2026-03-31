import { BadRequestException, Controller, Get, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { PageInitService } from './page-init.service';

@Controller('bff')
export class PageInitController {
  constructor(private readonly pageInitService: PageInitService) {}

  @Get('page-init')
  async pageInit(@Query('page') page?: string, @Query('uid') uid?: unknown) {
    if (!page) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'page 参数不能为空',
      });
    }
    const normalizedUid = this.normalizeOptionalUid(uid);
    if (!normalizedUid) {
      return this.pageInitService.build(page);
    }
    return this.pageInitService.build(page, normalizedUid);
  }

  private normalizeOptionalUid(value: unknown): string | undefined {
    if (value === undefined || value === null || value === '') {
      return undefined;
    }
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'uid 参数格式不正确，请使用字符串传递18位ID',
    });
  }
}
