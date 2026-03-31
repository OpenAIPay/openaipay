import { BadRequestException } from '@nestjs/common';
import { CashierController } from './cashier.controller';
import { CashierService } from './cashier.service';

describe('CashierController', () => {
  const createController = () => {
    const cashierService = {
      getCashierView: jest.fn().mockResolvedValue({}),
      getPricingPreview: jest.fn().mockResolvedValue({}),
    } as unknown as jest.Mocked<CashierService>;
    const controller = new CashierController(cashierService);
    return { controller, cashierService };
  };

  it('normalizes pricing preview amount before delegating', async () => {
    const { controller, cashierService } = createController();

    await controller.getPricingPreview(
      '880109000000000001',
      'MOBILE_TOPUP',
      'BANK_CARD',
      ' 88.80 ',
      'CNY',
    );

    expect(cashierService.getPricingPreview).toHaveBeenCalledWith(
      '880109000000000001',
      'MOBILE_TOPUP',
      'BANK_CARD',
      '88.80',
      'CNY',
    );
  });

  it('rejects malformed amount', async () => {
    const { controller } = createController();

    await expect(
      controller.getPricingPreview('880109000000000001', 'MOBILE_TOPUP', 'BANK_CARD', '88.888', 'CNY'),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
