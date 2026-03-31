import { AssetService } from './asset.service';
import { BackendHttpService } from '../common/backend-http.service';

describe('AssetService', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
      resolveAbsoluteUrl: jest.fn((raw?: string) => raw ?? ''),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new AssetService(backendHttpService);
    return { service, backendHttpService };
  };

  it('uses tradeOrderNo as fallback tradeNo to keep multiple changes', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: [
          {
            tradeOrderNo: '30922026032312000100000000000001',
            tradeType: 'TRANSFER',
            businessSceneCode: 'TRANSFER',
            direction: 'DEBIT',
            signedWalletAmount: '-66.00',
            currencyCode: 'CNY',
            counterpartyUserId: '880109000000000002',
            counterpartyNickname: '张三',
            occurredAt: '2026-03-23 12:00:01',
          },
          {
            tradeOrderNo: '30922026032312000200000000000002',
            tradeType: 'TRANSFER',
            businessSceneCode: 'TRANSFER',
            direction: 'CREDIT',
            signedWalletAmount: '88.00',
            currencyCode: 'CNY',
            counterpartyUserId: '880109000000000003',
            counterpartyNickname: '李四',
            occurredAt: '2026-03-23 12:00:02',
          },
        ],
      },
    });

    const result = await service.getUserAssetChanges('880109000000000001', 10);
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(2);
    expect(items[0]?.tradeNo).toBe('30922026032312000100000000000001');
    expect(items[1]?.tradeNo).toBe('30922026032312000200000000000002');
  });

  it('maps aicash fast redeem card-like subtitle to bank-card title', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [
            {
              tradeOrderNo: '30922026032314141400110000000004',
              tradeType: 'WITHDRAW',
              businessSceneCode: 'FUND_FAST_REDEEM',
              direction: 'CREDIT',
              signedWalletAmount: '260.00',
              currencyCode: 'CNY',
              displayTitle: '储蓄卡(尾号6666)',
              occurredAt: '2026-03-23 14:14:14',
            },
          ],
        },
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [],
        },
      });

    const result = await service.getUserAssetChanges('880109000000000001', 10);
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0]?.displayTitle).toBe('爱存转出至银行卡');
  });

  it('normalizes repeated redeem prefix to avoid duplicated display title', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [
            {
              tradeOrderNo: '30922026032611215600210033523600',
              tradeType: 'WITHDRAW',
              businessSceneCode: 'FUND_FAST_REDEEM',
              direction: 'CREDIT',
              signedWalletAmount: '520.00',
              currencyCode: 'CNY',
              displayTitle: '爱存转出至爱存转出至中国农业银行',
              occurredAt: '2026-03-26 11:21:56',
            },
          ],
        },
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [],
        },
      });

    const result = await service.getUserAssetChanges('880109000000000001', 10);
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0]?.displayTitle).toBe('爱存转出至中国农业银行');
  });

  it('returns generic query failed message when overview upstream is unavailable', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockRejectedValue(new Error('connect ECONNREFUSED 127.0.0.1:8080'));

    await expect(service.getUserAssetOverview('880109000000000001')).rejects.toMatchObject({
      response: expect.objectContaining({
        code: 'UPSTREAM_ERROR',
        message: '查询失败',
      }),
    });
  });

  it('returns generic query failed message when asset change query upstream is unavailable', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockRejectedValue(new Error('socket hang up'));

    await expect(service.getUserAssetChanges('880109000000000001', 20)).rejects.toMatchObject({
      response: expect.objectContaining({
        code: 'UPSTREAM_ERROR',
        message: '查询失败',
      }),
    });
  });
});
