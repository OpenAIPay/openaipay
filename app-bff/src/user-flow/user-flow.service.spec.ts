import { UserFlowService } from './user-flow.service';

describe('UserFlowService', () => {
  it('passes device header when checking register phone', async () => {
    const backendHttpService = {
      get: jest.fn().mockResolvedValue({
        data: {
          data: {
            userExists: false,
            realNameVerified: false,
            kycLevel: '',
          },
        },
      }),
    } as any;

    const service = new UserFlowService(backendHttpService);
    await service.checkRegisterPhone('13920000004', 'ios-device-installation-01');

    expect(backendHttpService.get).toHaveBeenCalledWith(
      '/api/user-flows/register/check?loginId=13920000004',
      {
        headers: {
          'X-Device-Id': 'ios-device-installation-01',
        },
      },
    );
  });

  it('registers user by payload body fields', async () => {
    const backendHttpService = {
      post: jest.fn().mockResolvedValue({
        data: {
          data: {
            userId: 1,
            aipayUid: '2088000000000001',
            loginId: '13920000004',
            kycSubmitted: true,
          },
        },
      }),
    } as any;

    const service = new UserFlowService(backendHttpService);

    await service.register(
      {
        deviceId: 'ios-device-installation-01',
        legacyDeviceIds: ['legacy-a', 'legacy-b'],
        loginId: '13920000004',
        mobile: '13920000004',
      },
    );

    expect(backendHttpService.post).toHaveBeenCalledWith(
      '/api/user-flows/registrations',
      expect.objectContaining({
        deviceId: 'ios-device-installation-01',
        legacyDeviceIds: ['legacy-a', 'legacy-b'],
        loginId: '13920000004',
      }),
    );
  });
});
