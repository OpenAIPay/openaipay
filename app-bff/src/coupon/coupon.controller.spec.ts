import { BadRequestException, ForbiddenException, UnauthorizedException } from '@nestjs/common';
import type { Request } from 'express';
import { CouponController } from './coupon.controller';
import { CouponService } from './coupon.service';

describe('CouponController', () => {
  const createController = () => {
    const couponService = {
      claimMobileTopUpRewardCoupon: jest.fn().mockResolvedValue({ couponNo: 'CPN001' }),
      listMobileTopUpRewardCoupons: jest.fn().mockResolvedValue([]),
    } as unknown as jest.Mocked<CouponService>;
    const controller = new CouponController(couponService);
    return { controller, couponService };
  };

  it('passes valid claim payload and user query through controller boundary', async () => {
    const { controller, couponService } = createController();
    const payload = { userId: '880109000000000001', mobile: '13920000002' };
    const req = { authenticatedUserId: '880109000000000001' } as unknown as Request;

    await controller.claimMobileTopUpRewardCoupon(req, payload);
    await controller.listMobileTopUpRewardCoupons(req, '880109000000000001');

    expect(couponService.claimMobileTopUpRewardCoupon).toHaveBeenCalledWith('880109000000000001', payload);
    expect(couponService.listMobileTopUpRewardCoupons).toHaveBeenCalledWith('880109000000000001');
  });

  it('rejects invalid payload or malformed user id', async () => {
    const { controller } = createController();
    const req = { authenticatedUserId: '880109000000000001' } as unknown as Request;

    await expect(
      controller.claimMobileTopUpRewardCoupon(req, undefined),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(controller.listMobileTopUpRewardCoupons(req, 'abc')).rejects.toBeInstanceOf(
      BadRequestException,
    );
    await expect(
      controller.listMobileTopUpRewardCoupons(req, Number('880109000000000001')),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('rejects missing or malformed authenticated user id', async () => {
    const { controller } = createController();

    await expect(
      controller.listMobileTopUpRewardCoupons({ authenticatedUserId: '' } as unknown as Request, undefined),
    ).rejects.toBeInstanceOf(UnauthorizedException);
    await expect(
      controller.listMobileTopUpRewardCoupons(
        { authenticatedUserId: 'not-a-number' } as unknown as Request,
        undefined,
      ),
    ).rejects.toBeInstanceOf(UnauthorizedException);
  });

  it('rejects mismatched userId query when token user differs', async () => {
    const { controller } = createController();
    const req = { authenticatedUserId: '880109000000000001' } as unknown as Request;

    await expect(
      controller.listMobileTopUpRewardCoupons(req, '880109000000000002'),
    ).rejects.toBeInstanceOf(ForbiddenException);
  });
});
