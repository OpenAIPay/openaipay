import { BadRequestException, Controller, Get, Param, Query } from '@nestjs/common';
import { BillService } from '../bill/bill.service';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { AssetService } from './asset.service';

@Controller('bff/assets')
export class AssetController {
  constructor(
    private readonly assetService: AssetService,
    private readonly billService: BillService,
  ) {}

  @Get(':userId/overview')
  async getUserAssetOverview(@Param('userId') userId?: unknown) {
    return this.assetService.getUserAssetOverview(this.assertUserId(userId));
  }

  @Get(':userId/changes')
  async getUserAssetChanges(@Param('userId') userId?: unknown, @Query('limit') limit?: unknown) {
    return this.assetService.getUserAssetChanges(this.assertUserId(userId), this.normalizeLimit(limit));
  }

  @Get(':userId/bills')
  async getUserBillEntries(
    @Param('userId') userId?: unknown,
    @Query('limit') limit?: unknown,
    @Query('pageNo') pageNo?: unknown,
    @Query('pageSize') pageSize?: unknown,
    @Query('billMonth') billMonth?: unknown,
    @Query('businessDomainCode') businessDomainCode?: unknown,
    @Query('cursor') cursor?: unknown,
  ) {
    const normalizedUserId = this.assertUserId(userId);
    const normalizedBillMonth =
      typeof billMonth === 'string' && billMonth.trim().length > 0 ? billMonth.trim() : undefined;
    const normalizedBusinessDomainCode =
      typeof businessDomainCode === 'string' && businessDomainCode.trim().length > 0
        ? businessDomainCode.trim()
        : undefined;
    const normalizedCursor = typeof cursor === 'string' && cursor.trim().length > 0 ? cursor.trim() : undefined;
    return this.billService.getUserBillEntries(
      normalizedUserId,
      this.normalizeBillPageNo(pageNo),
      this.resolveBillPageSize(limit, pageSize),
      normalizedBillMonth,
      normalizedBusinessDomainCode,
      normalizedCursor,
    );
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

  private normalizeLimit(value: unknown): number {
    if (value === undefined || value === null || value === '') {
      return 3;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= 100) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0 && parsed <= 100) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'limit 参数必须是 1-100 的整数',
    });
  }

  private normalizeBillLimit(value: unknown): number {
    if (value === undefined || value === null || value === '') {
      return 20;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= 100) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0 && parsed <= 100) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'limit 参数必须是 1-100 的整数',
    });
  }

  private normalizeBillPageNo(value: unknown): number {
    if (value === undefined || value === null || value === '') {
      return 1;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'pageNo 参数必须是大于 0 的整数',
    });
  }

  private resolveBillPageSize(limitValue: unknown, pageSizeValue: unknown): number {
    if (pageSizeValue !== undefined && pageSizeValue !== null && pageSizeValue !== '') {
      return this.normalizeBillPageSize(pageSizeValue);
    }
    return this.normalizeBillLimit(limitValue);
  }

  private normalizeBillPageSize(value: unknown): number {
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= 100) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0 && parsed <= 100) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'pageSize 参数必须是 1-100 的整数',
    });
  }
}
