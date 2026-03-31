import { BadRequestException } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';

describe('AuthController', () => {
  const createController = () => {
    const authService = {
      mobileVerifyLogin: jest.fn().mockResolvedValue({
        accessToken: 'token-mobile-001',
        tokenType: 'Bearer',
      }),
      demoAutoLogin: jest.fn().mockResolvedValue({
        accessToken: 'token-demo-001',
        tokenType: 'Bearer',
      }),
      listPresetLoginAccounts: jest.fn().mockResolvedValue([
        { loginId: '13920000001', nickname: '顾郡' },
      ]),
    } as unknown as jest.Mocked<AuthService>;
    const controller = new AuthController(authService);
    return { controller, authService };
  };

  it('delegates mobile verify login with normalized payload', async () => {
    const { controller, authService } = createController();

    await controller.mobileVerifyLogin(' 13920000002 ', ' ios-device-01 ', ' legacy-a , legacy-b ');

    expect(authService.mobileVerifyLogin).toHaveBeenCalledWith(
      '13920000002',
      'ios-device-01',
      undefined,
      'legacy-a , legacy-b',
    );
  });

  it('rejects blank loginId or deviceId for mobile verify login', async () => {
    const { controller } = createController();

    await expect(controller.mobileVerifyLogin('  ', 'ios-device-01')).rejects.toBeInstanceOf(BadRequestException);
    await expect(controller.mobileVerifyLogin('13920000002', '   ')).rejects.toBeInstanceOf(BadRequestException);
  });

  it('delegates demo auto login with normalized payload', async () => {
    const { controller, authService } = createController();

    await controller.demoAutoLogin(' ios-device-01 ', 13920000002, ' legacy-a ');

    expect(authService.demoAutoLogin).toHaveBeenCalledWith('ios-device-01', '13920000002', 'legacy-a');
  });

  it('rejects blank deviceId for demo auto login', async () => {
    const { controller } = createController();

    await expect(controller.demoAutoLogin('  ')).rejects.toBeInstanceOf(BadRequestException);
  });

  it('queries preset login accounts with normalized deviceId', async () => {
    const { controller, authService } = createController();

    await controller.listPresetLoginAccounts(' ios-device-01 ', ' legacy-a ');

    expect(authService.listPresetLoginAccounts).toHaveBeenCalledWith('ios-device-01', 'legacy-a');
  });

  it('rejects blank deviceId for preset login accounts', async () => {
    const { controller } = createController();

    await expect(controller.listPresetLoginAccounts('  ')).rejects.toBeInstanceOf(BadRequestException);
  });
});
