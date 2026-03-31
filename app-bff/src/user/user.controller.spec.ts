import { BadRequestException } from '@nestjs/common';
import { UserController } from './user.controller';
import { UserService } from './user.service';

describe('UserController', () => {
  const createController = () => {
    const userService = {
      getProfileByLoginId: jest.fn().mockResolvedValue({ userId: '880109000000000001' }),
      getProfile: jest.fn().mockResolvedValue({ userId: '880109000000000001' }),
      updateProfile: jest.fn().mockResolvedValue(null),
      getRecentContacts: jest.fn().mockResolvedValue([]),
      getFriends: jest.fn().mockResolvedValue([]),
    } as unknown as jest.Mocked<UserService>;
    const controller = new UserController(userService);
    return { controller, userService };
  };

  it('normalizes mainland mobile query for profile lookup', async () => {
    const { controller, userService } = createController();

    await controller.getProfileByLoginId('0086 13920000002');

    expect(userService.getProfileByLoginId).toHaveBeenCalledWith('13920000002');
  });

  it('uses fallback and explicit limits for recent contacts and friends', async () => {
    const { controller, userService } = createController();

    await controller.getRecentContacts('880109000000000001', undefined);
    await controller.getFriends('880109000000000001', '50');

    expect(userService.getRecentContacts).toHaveBeenCalledWith('880109000000000001', 20);
    expect(userService.getFriends).toHaveBeenCalledWith('880109000000000001', 50);
  });

  it('rejects invalid update payload and invalid limit', async () => {
    const { controller } = createController();

    await expect(
      controller.updateProfile('880109000000000001', [] as unknown as Record<string, unknown>),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(controller.getFriends('880109000000000001', '0')).rejects.toBeInstanceOf(
      BadRequestException,
    );
  });
});
