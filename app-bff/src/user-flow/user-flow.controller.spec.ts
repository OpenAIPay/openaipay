import { UserFlowController } from './user-flow.controller';
import { UserFlowService } from './user-flow.service';

describe('UserFlowController', () => {
  const createController = () => {
    const userFlowService = {
      checkRegisterPhone: jest.fn().mockResolvedValue({
        userExists: false,
        realNameVerified: false,
        kycLevel: '',
      }),
      register: jest.fn().mockResolvedValue({
        userId: 1,
        aipayUid: '2088000000000001',
        loginId: '13920000004',
        kycSubmitted: false,
      }),
    } as unknown as jest.Mocked<UserFlowService>;
    const controller = new UserFlowController(userFlowService);
    return { controller, userFlowService };
  };

  it('uses loginId for register check', async () => {
    const { controller, userFlowService } = createController();

    await controller.checkRegisterPhone('13920000004', ' ios-device-installation-01 ');

    expect(userFlowService.checkRegisterPhone).toHaveBeenCalledWith('13920000004', 'ios-device-installation-01');
  });

  it('uses loginId in registration payload', async () => {
    const { controller, userFlowService } = createController();

    await controller.register(
      {
        deviceId: ' ios-device-installation-01 ',
        legacyDeviceIds: [' legacy-a ', '', null, 'legacy-a', 'legacy-b'],
        loginId: '13920000004',
        nickname: '测试用户',
      },
    );

    expect(userFlowService.register).toHaveBeenCalledWith(
      expect.objectContaining({
        deviceId: 'ios-device-installation-01',
        legacyDeviceIds: ['legacy-a', 'legacy-b'],
        loginId: '13920000004',
        mobile: '13920000004',
      }),
    );
  });
});
