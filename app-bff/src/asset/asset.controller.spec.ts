import { BadRequestException } from '@nestjs/common';
import { BillService } from '../bill/bill.service';
import { AssetController } from './asset.controller';
import { AssetService } from './asset.service';

describe('AssetController', () => {
  const createController = () => {
    const assetService = {
      getUserAssetOverview: jest.fn().mockResolvedValue({ userId: '880109000000000001' }),
      getUserAssetChanges: jest.fn().mockResolvedValue({ items: [] }),
    } as unknown as jest.Mocked<AssetService>;
    const billService = {
      getUserBillEntries: jest.fn().mockResolvedValue({ items: [] }),
    } as unknown as jest.Mocked<BillService>;
    const controller = new AssetController(assetService, billService);
    return { controller, assetService, billService };
  };

  it('uses default limit for asset changes', async () => {
    const { controller, assetService } = createController();

    await controller.getUserAssetChanges('880109000000000001', undefined);

    expect(assetService.getUserAssetChanges).toHaveBeenCalledWith('880109000000000001', 3);
  });

  it('prefers pageSize over limit for bill entry paging', async () => {
    const { controller, billService } = createController();

    await controller.getUserBillEntries(
      '880109000000000001',
      '20',
      '2',
      '50',
      ' 2026-03 ',
      ' AICASH ',
      ' 2026-03-22 18:30:00|9 ',
    );

    expect(billService.getUserBillEntries).toHaveBeenCalledWith(
      '880109000000000001',
      2,
      50,
      '2026-03',
      'AICASH',
      '2026-03-22 18:30:00|9',
    );
  });

  it('rejects invalid bill page arguments', async () => {
    const { controller } = createController();

    await expect(
      controller.getUserBillEntries('880109000000000001', undefined, '0', undefined, undefined, undefined, undefined),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(
      controller.getUserBillEntries(
        '880109000000000001',
        undefined,
        undefined,
        '101',
        undefined,
        undefined,
        undefined,
      ),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
