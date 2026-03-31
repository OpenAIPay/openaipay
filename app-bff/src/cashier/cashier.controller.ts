import { BadRequestException, Controller, Get, Param, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { CashierService } from './cashier.service';

@Controller('bff/cashier')
export class CashierController {
  constructor(private readonly cashierService: CashierService) {}

  @Get('users/:userId/view')
  async getCashierView(@Param('userId') userId?: unknown, @Query('sceneCode') sceneCode?: string) {
    return this.cashierService.getCashierView(this.assertUserId(userId), sceneCode);
  }

  @Get('users/:userId/pricing-preview')
  async getPricingPreview(
    @Param('userId') userId?: unknown,
    @Query('sceneCode') sceneCode?: string,
    @Query('paymentMethod') paymentMethod?: string,
    @Query('amount') amount?: string,
    @Query('currencyCode') currencyCode?: string,
  ) {
    const normalizedAmount = typeof amount === 'string' ? amount.trim() : '';
    if (!/^\d+(?:\.\d{1,2})?$/.test(normalizedAmount)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'amount 参数格式不正确',
      });
    }
    return this.cashierService.getPricingPreview(
      this.assertUserId(userId),
      sceneCode,
      paymentMethod,
      normalizedAmount,
      currencyCode,
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
}
