import { BadRequestException } from '@nestjs/common';
import { ContactController } from './contact.controller';
import { ContactService } from './contact.service';

describe('ContactController', () => {
  const createController = () => {
    const contactService = {
      searchContacts: jest.fn().mockResolvedValue([]),
      applyFriendRequest: jest.fn().mockResolvedValue({ requestNo: 'CR202603220001' }),
      listReceivedFriendRequests: jest.fn().mockResolvedValue([]),
      listSentFriendRequests: jest.fn().mockResolvedValue([]),
      handleFriendRequest: jest.fn().mockResolvedValue({ requestNo: 'CR202603220001', status: 'ACCEPTED' }),
    } as unknown as jest.Mocked<ContactService>;
    const controller = new ContactController(contactService);
    return { controller, contactService };
  };

  it('searches contacts with canonical ownerUserId query', async () => {
    const { controller, contactService } = createController();

    await controller.searchContacts({
      ownerUserId: '880902068943900002',
      keyword: ' 177666 ',
      limit: '30',
    });

    expect(contactService.searchContacts).toHaveBeenCalledWith('880902068943900002', '177666', 30);
  });

  it('accepts normalized numeric keyword from global normalize pipe', async () => {
    const { controller, contactService } = createController();

    await controller.searchContacts({
      ownerUserId: '880902068943900002',
      keyword: 177666,
      limit: 30,
    });

    expect(contactService.searchContacts).toHaveBeenCalledWith('880902068943900002', '177666', 30);
  });

  it('rejects invalid user id and keyword for contact search', async () => {
    const { controller } = createController();

    await expect(
      controller.searchContacts({
        ownerUserId: 'not-a-number',
        keyword: '177666',
        limit: '20',
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(
      controller.searchContacts({
        ownerUserId: '880902068943900002',
        keyword: '   ',
        limit: '20',
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('queries received friend requests with normalized target user id and limit', async () => {
    const { controller, contactService } = createController();

    await controller.listReceivedFriendRequests({
      targetUserId: '880902068943900002',
      limit: '20',
    });

    expect(contactService.listReceivedFriendRequests).toHaveBeenCalledWith('880902068943900002', 20);
  });

  it('queries sent friend requests with normalized requester user id and limit', async () => {
    const { controller, contactService } = createController();

    await controller.listSentFriendRequests({
      requesterUserId: '880902068943900002',
      limit: '20',
    });

    expect(contactService.listSentFriendRequests).toHaveBeenCalledWith('880902068943900002', 20);
  });

  it('handles friend request with normalized operator user id and action', async () => {
    const { controller, contactService } = createController();

    await controller.handleFriendRequest('CR202603220001', {
      operatorUserId: '880902068943900002',
      action: 'accept',
    });

    expect(contactService.handleFriendRequest).toHaveBeenCalledWith('CR202603220001', {
      operatorUserId: '880902068943900002',
      action: 'ACCEPT',
    });
  });
});
