import { AuthService } from './auth.service';

describe('AuthService', () => {
  it('forwards legacy device ids during mobile verify login', async () => {
    const backendHttpService = {
      post: jest.fn().mockResolvedValue({
        data: {
          data: {
            accessToken: 'backend-token',
            tokenType: 'Bearer',
            expiresInSeconds: 7200,
            userId: '880109000000000001',
            aipayUid: '2088000000000001',
            nickname: '顾郡',
          },
        },
      }),
      get: jest.fn().mockResolvedValue({
        data: {
          data: {
            userId: '880109000000000001',
            aipayUid: '2088000000000001',
            loginId: '13920000001',
            accountSource: 'REGISTER',
            nickname: '顾郡',
          },
        },
      }),
    } as any;

    const service = new AuthService(backendHttpService);

    await service.mobileVerifyLogin(
      '13920000001',
      'ios-device-installation-01',
      undefined,
      'legacy-a, legacy-b',
    );

    expect(backendHttpService.post).toHaveBeenCalledWith(
      '/api/auth/mobile-verify-login',
      {
        loginId: '13920000001',
        deviceId: 'ios-device-installation-01',
      },
      expect.objectContaining({
        headers: {
          'X-Device-Id': 'ios-device-installation-01',
          'X-Legacy-Device-Ids': 'legacy-a,legacy-b',
        },
        timeoutMs: undefined,
      }),
    );
  });

  it('sends device fields in payload when registering demo user', async () => {
    const backendHttpService = {
      post: jest.fn().mockResolvedValue({
        data: {
          data: {
            userId: '880109000000000002',
            loginId: '13920000004',
          },
        },
      }),
      get: jest.fn(),
    } as any;

    const service = new AuthService(backendHttpService);

    await (service as any).registerDemoUser(
      {
        avatarUrl: '/api/media/demo.png',
        nickname: '顾郡',
        idCardNo: '440101199001010011',
      },
      'ios-device-installation-01',
      '123456',
      'legacy-a,legacy-b',
    );

    expect(backendHttpService.post).toHaveBeenCalledWith(
      '/api/user-flows/registrations',
      expect.objectContaining({
        deviceId: 'ios-device-installation-01',
        legacyDeviceIds: ['legacy-a', 'legacy-b'],
        accountSource: 'DEMO',
      }),
    );
  });
});
