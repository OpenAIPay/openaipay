import { BadRequestException, Body, Controller, ForbiddenException, Get, Post, Query, Req, UnauthorizedException } from '@nestjs/common';
import type { Request } from 'express';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { CouponService } from './coupon.service';

@Controller('bff/coupons')
export class CouponController {
  constructor(private readonly couponService: CouponService) {}

  @Post('mobile-topup-reward/claim')
  async claimMobileTopUpRewardCoupon(@Req() req: Request, @Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return this.couponService.claimMobileTopUpRewardCoupon(this.requireAuthenticatedUserId(req), payload);
  }

  @Get('mobile-topup-reward/available')
  async listMobileTopUpRewardCoupons(@Req() req: Request, @Query('userId') userId?: unknown) {
    const currentUserId = this.requireAuthenticatedUserId(req);
    const requestedUserId = this.normalizeOptionalUserId(userId);
    if (requestedUserId && requestedUserId !== currentUserId) {
      throw new ForbiddenException({
        code: 'FORBIDDEN',
        message: '用户鉴权失败：请求用户与令牌不匹配',
      });
    }
    return this.couponService.listMobileTopUpRewardCoupons(currentUserId);
  }

  private normalizeOptionalUserId(value: unknown): string | undefined {
    if (value === undefined || value === null || value === '') {
      return undefined;
    }
    const normalized = normalizePositiveLongId(value, { minDigits: 1, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'userId 参数格式不正确，请使用字符串传递18位ID',
    });
  }

  private requireAuthenticatedUserId(req: Request): string {
    const normalized = normalizePositiveLongId(req.authenticatedUserId, { minDigits: 1, maxDigits: 20 });
    if (!normalized) {
      throw new UnauthorizedException({
        code: 'UNAUTHORIZED',
        message: '用户鉴权失败：缺少有效 Bearer Token',
      });
    }
    return normalized;
  }
}
