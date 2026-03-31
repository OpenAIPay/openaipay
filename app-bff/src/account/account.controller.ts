import { BadRequestException, Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { AccountService } from './account.service';

@Controller('bff/accounts')
export class AccountController {
  constructor(private readonly accountService: AccountService) {}

  @Get('credit/users/:userId')
  async getCreditAccount(@Param('userId') userId?: unknown) {
    return this.accountService.getCreditAccount(this.assertUserId(userId));
  }

  @Get('credit/users/:userId/current-bill-detail')
  async getCreditCurrentBillDetail(@Param('userId') userId?: unknown) {
    return this.accountService.getCreditCurrentBillDetail(this.assertUserId(userId));
  }

  @Get('credit/users/:userId/next-bill-detail')
  async getCreditNextBillDetail(@Param('userId') userId?: unknown) {
    return this.accountService.getCreditNextBillDetail(this.assertUserId(userId));
  }

  @Get('loan/users/:userId')
  async getLoanAccount(@Param('userId') userId?: unknown) {
    return this.accountService.getLoanAccount(this.assertUserId(userId));
  }

  @Get('fund/users/:userId')
  async getFundAccount(@Param('userId') userId?: unknown) {
    return this.accountService.getFundAccount(this.assertUserId(userId));
  }

  @Get('fund/open/agreement-pack/:userId')
  async getFundOpenAgreementPack(
    @Param('userId') userId?: unknown,
    @Query('fundCode') fundCode?: unknown,
    @Query('currencyCode') currencyCode?: unknown,
  ) {
    return this.accountService.getFundOpenAgreementPack(
      this.assertUserId(userId),
      this.normalizeOptionalString(fundCode),
      this.normalizeOptionalString(currencyCode),
    );
  }

  @Get('credit/open/agreement-pack/:userId')
  async getCreditOpenAgreementPack(
    @Param('userId') userId?: unknown,
    @Query('productCode') productCode?: unknown,
  ) {
    return this.accountService.getCreditOpenAgreementPack(
      this.assertUserId(userId),
      this.normalizeOptionalString(productCode),
    );
  }

  @Post('fund/open')
  async openFundAccountWithAgreement(@Body() payload?: Record<string, unknown>) {
    return this.accountService.openFundAccountWithAgreement(this.assertPayload(payload));
  }

  @Post('credit/open')
  async openCreditProductWithAgreement(@Body() payload?: Record<string, unknown>) {
    return this.accountService.openCreditProductWithAgreement(this.assertPayload(payload));
  }

  @Post('fund')
  async createFundAccount(@Body() payload?: Record<string, unknown>) {
    return this.accountService.createFundAccount(this.assertPayload(payload));
  }

  @Post('fund/subscribe')
  async createFundSubscribe(@Body() payload?: Record<string, unknown>) {
    return this.accountService.createFundSubscribe(this.assertPayload(payload));
  }

  @Post('fund/subscribe/confirm')
  async confirmFundSubscribe(@Body() payload?: Record<string, unknown>) {
    return this.accountService.confirmFundSubscribe(this.assertPayload(payload));
  }

  @Post('fund/fast-redeem')
  async createFundFastRedeem(@Body() payload?: Record<string, unknown>) {
    return this.accountService.createFundFastRedeem(this.assertPayload(payload));
  }

  @Post('fund/redeem/confirm')
  async confirmFundRedeem(@Body() payload?: Record<string, unknown>) {
    return this.accountService.confirmFundRedeem(this.assertPayload(payload));
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

  private assertPayload(payload?: Record<string, unknown>): Record<string, unknown> {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return payload;
  }

  private normalizeOptionalString(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
  }
}
