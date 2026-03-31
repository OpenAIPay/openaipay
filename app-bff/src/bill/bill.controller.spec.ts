import { BadRequestException } from '@nestjs/common';
import { BillService } from './bill.service';
import { BillController } from './bill.controller';

describe('BillController', () => {
  const createController = () => {
    const billService = {
      getUserBillEntries: jest.fn().mockResolvedValue({ items: [] }),
    } as unknown as jest.Mocked<BillService>;
    const controller = new BillController(billService);
    return { controller, billService };
  };

  it('passes normalized userId and paging arguments to bill service', async () => {
    const { controller, billService } = createController();

    await controller.getUserBillEntries(
      '880109000000000001',
      '20',
      '2',
      '50',
      ' 2026-03 ',
      ' AICASH ',
      ' 2026-03-22 18:20:30|100 ',
    );

    expect(billService.getUserBillEntries).toHaveBeenCalledWith(
      '880109000000000001',
      2,
      50,
      '2026-03',
      'AICASH',
      '2026-03-22 18:20:30|100',
    );
  });

  it('rejects malformed or precision-lost userId input', async () => {
    const { controller } = createController();

    await expect(
      controller.getUserBillEntries('abc', undefined, undefined, undefined, undefined, undefined, undefined),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(
      controller.getUserBillEntries(
        Number('880109000000000001'),
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
      ),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
