import { BadRequestException } from '@nestjs/common';
import { MessageController } from './message.controller';
import { MessageService } from './message.service';

describe('MessageController', () => {
  const createController = () => {
    const messageService = {
      getMessageHome: jest.fn().mockResolvedValue({}),
      getConversationMessages: jest.fn().mockResolvedValue([]),
      getRedPacketHistory: jest.fn().mockResolvedValue({ items: [] }),
      sendText: jest.fn().mockResolvedValue({}),
      sendImage: jest.fn().mockResolvedValue({}),
      sendRedPacket: jest.fn().mockResolvedValue({}),
      sendTransfer: jest.fn().mockResolvedValue({}),
      getRedPacketDetail: jest.fn().mockResolvedValue({}),
      claimRedPacket: jest.fn().mockResolvedValue({}),
      markConversationRead: jest.fn().mockResolvedValue(null),
    } as unknown as jest.Mocked<MessageService>;
    const controller = new MessageController(messageService);
    return { controller, messageService };
  };

  it('normalizes conversation query and default paging', async () => {
    const { controller, messageService } = createController();

    await controller.getConversationMessages(' CONV202603210001 ', '880109000000000001', ' MSG0001 ', undefined);

    expect(messageService.getConversationMessages).toHaveBeenCalledWith(
      '880109000000000001',
      'CONV202603210001',
      'MSG0001',
      100,
    );
  });

  it('parses red packet history filters', async () => {
    const { controller, messageService } = createController();

    await controller.getRedPacketHistory('880109000000000001', 'RECEIVED', '2026', '20');

    expect(messageService.getRedPacketHistory).toHaveBeenCalledWith(
      '880109000000000001',
      'RECEIVED',
      2026,
      20,
    );
  });

  it('rejects blank red packet number and invalid payload', async () => {
    const { controller } = createController();

    await expect(controller.getRedPacketDetail('   ', '880109000000000001')).rejects.toBeInstanceOf(
      BadRequestException,
    );
    await expect(
      controller.claimRedPacket('RPK202603210001', undefined),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
