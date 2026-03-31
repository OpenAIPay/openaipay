import { BackendHttpService } from '../common/backend-http.service';
import { CashierService } from './cashier.service';

describe('CashierServiceContract', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new CashierService(backendHttpService);
    return { service, backendHttpService };
  };

  it('normalizes cashier view to stable iOS contract fields', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          userId: 880109000000000001,
          sceneCode: 'transfer',
          payTools: [
            {
              toolType: 'wallet',
              toolCode: 'WALLET',
              toolName: '余额',
              defaultSelected: 1,
              ignored: 'ignored',
            },
          ],
        },
      },
    });

    const result = await service.getCashierView('880109000000000001', 'transfer');
    expect(result).toEqual({
      userId: '880109000000000001',
      sceneCode: 'TRANSFER',
      sceneConfig: undefined,
      payTools: [
        {
          toolType: 'WALLET',
          toolCode: 'WALLET',
          toolName: '余额',
          toolDescription: undefined,
          defaultSelected: true,
          bankCode: undefined,
          cardType: undefined,
          phoneTailNo: undefined,
        },
      ],
      generatedAt: undefined,
    });
  });
});
