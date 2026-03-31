import { BackendHttpService } from '../common/backend-http.service';
import { ContactService } from './contact.service';

describe('ContactServiceContract', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
      post: jest.fn(),
      resolveAbsoluteUrl: jest.fn((raw?: unknown) => (typeof raw === 'string' ? raw : '')),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new ContactService(backendHttpService);
    return { service, backendHttpService };
  };

  it('whitelists apply friend request payload fields', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.post.mockResolvedValue({
      data: {
        success: true,
        data: {
          requestNo: 'CR202603230001',
          requesterUserId: '880109000000000001',
          targetUserId: '880109000000000002',
          status: 'PENDING',
        },
      },
    });

    await service.applyFriendRequest({
      requesterUserId: '880109000000000001',
      targetUserId: '880109000000000002',
      applyMessage: '请通过',
      action: 'should-not-pass-through',
    });

    const [, requestPayload] = backendHttpService.post.mock.calls[0];
    expect(requestPayload).toEqual({
      requesterUserId: '880109000000000001',
      targetUserId: '880109000000000002',
      applyMessage: '请通过',
    });
  });

  it('queries sent friend requests from backend with normalized requesterUserId', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: [
          {
            requestNo: 'CR202603230010',
            requesterUserId: '880109000000000001',
            targetUserId: '880109000000000002',
            status: 'REJECTED',
          },
        ],
      },
    });

    const requests = await service.listSentFriendRequests('880109000000000001', 20);

    expect(backendHttpService.get).toHaveBeenCalledWith(
      '/api/contacts/requests/sent?requesterUserId=880109000000000001&limit=20',
    );
    expect(requests).toHaveLength(1);
    expect(requests[0]).toMatchObject({
      requestNo: 'CR202603230010',
      requesterUserId: '880109000000000001',
      targetUserId: '880109000000000002',
      status: 'REJECTED',
    });
  });
});
