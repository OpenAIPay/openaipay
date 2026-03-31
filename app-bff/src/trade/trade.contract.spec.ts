import { BackendHttpService } from '../common/backend-http.service';
import { TradeService } from './trade.service';

describe('TradeServiceContract', () => {
  const createService = () => {
    const backendHttpService = {
      post: jest.fn(),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new TradeService(backendHttpService);
    return { service, backendHttpService };
  };

  it('builds transfer payload with whitelist fields only', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.post.mockResolvedValue({
      data: {
        success: true,
        data: {
          tradeNo: 'TRD202603230001',
          requestNo: 'REQ202603230001',
          status: 'ACCEPTED',
          payerUserId: '880109000000000001',
          payeeUserId: '880109000000000002',
        },
      },
    });

    await service.createTransferTrade({
      requestNo: 'REQ202603230001',
      payerUserId: '880109000000000001',
      payeeUserId: '880109000000000002',
      amount: '88.66',
      paymentMethod: 'wallet',
      metadata: 'from=chat',
      unexpectedField: 'should-not-pass-through',
    });

    const [, requestPayload] = backendHttpService.post.mock.calls[0];
    expect(requestPayload).toEqual({
      requestNo: 'REQ202603230001',
      payerUserId: '880109000000000001',
      payeeUserId: '880109000000000002',
      amount: '88.66',
      currencyCode: 'CNY',
      paymentMethod: 'WALLET',
      metadata: 'from=chat',
    });
    expect((requestPayload as Record<string, unknown>).unexpectedField).toBeUndefined();
  });
});
