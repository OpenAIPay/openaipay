import { BackendHttpService } from '../common/backend-http.service';
import { MessageService } from './message.service';

describe('MessageService', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
      post: jest.fn(),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new MessageService(backendHttpService);
    return { service, backendHttpService };
  };

  it('normalizes transfer amount to Money payload before proxying to backend', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.post.mockResolvedValue({
      data: {
        success: true,
        data: {
          messageId: 'MSG202603230001',
        },
      },
    });

    await service.sendTransfer({
      senderUserId: '880109000000000001',
      receiverUserId: '880109000000000002',
      amount: '88.66',
      paymentMethod: 'WALLET',
    });

    expect(backendHttpService.post).toHaveBeenCalledWith(
      '/api/messages/transfer',
      expect.objectContaining({
        senderUserId: '880109000000000001',
        receiverUserId: '880109000000000002',
        amount: {
          amount: '88.66',
          currencyCode: 'CNY',
        },
      }),
      expect.any(Object),
    );
  });
});

