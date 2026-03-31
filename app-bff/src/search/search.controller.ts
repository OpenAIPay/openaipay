import { BadRequestException, Controller, Get, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { SearchService } from './search.service';

@Controller('bff')
export class SearchController {
  constructor(private readonly searchService: SearchService) {}

  @Get('search')
  async search(@Query('uid') uid?: unknown, @Query('keyword') keyword?: unknown, @Query('limit') limit?: unknown) {
    const normalizedUid = this.normalizeUserId(uid);
    const normalizedKeyword = this.normalizeText(keyword, 'keyword 参数不能为空');
    return this.searchService.search(normalizedUid, normalizedKeyword, this.normalizeLimit(limit));
  }

  private normalizeUserId(value: unknown): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'uid 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private normalizeText(value: unknown, message: string): string {
    if (typeof value === 'string') {
      const normalized = value.trim();
      if (normalized.length > 0) {
        return normalized;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message,
    });
  }

  private normalizeLimit(value: unknown): number | undefined {
    if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
      return Math.trunc(value);
    }
    if (typeof value === 'string' && /^\d+$/.test(value.trim())) {
      return Number.parseInt(value.trim(), 10);
    }
    return undefined;
  }
}
