import { BadRequestException } from '@nestjs/common';
import { PageInitController } from './page-init.controller';
import { PageInitService } from './page-init.service';

describe('PageInitController', () => {
  const createController = () => {
    const pageInitService = {
      build: jest.fn().mockResolvedValue({ page: 'home' }),
    } as unknown as jest.Mocked<PageInitService>;
    const controller = new PageInitController(pageInitService);
    return { controller, pageInitService };
  };

  it('uses anonymous page init path when uid is absent', async () => {
    const { controller, pageInitService } = createController();

    await controller.pageInit('home', undefined);

    expect(pageInitService.build).toHaveBeenCalledWith('home');
  });

  it('passes validated uid through to service', async () => {
    const { controller, pageInitService } = createController();

    await controller.pageInit('balance', '880109000000000001');

    expect(pageInitService.build).toHaveBeenCalledWith('balance', '880109000000000001');
  });

  it('rejects blank page and malformed uid', async () => {
    const { controller } = createController();

    await expect(controller.pageInit(undefined, '880109000000000001')).rejects.toBeInstanceOf(
      BadRequestException,
    );
    await expect(controller.pageInit('home', 'abc')).rejects.toBeInstanceOf(BadRequestException);
    await expect(controller.pageInit('home', Number('880109000000000001'))).rejects.toBeInstanceOf(
      BadRequestException,
    );
  });
});
