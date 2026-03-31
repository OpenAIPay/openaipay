import { ForbiddenException, UnauthorizedException } from '@nestjs/common';
import type { Request, Response } from 'express';
import { buildSignedAccessToken } from './auth-token';
import { AuthMiddleware } from './auth.middleware';

type MockRequestInit = {
  method: string;
  path: string;
  originalUrl?: string;
  url?: string;
  authorization?: string;
  query?: Record<string, unknown>;
  body?: Record<string, unknown>;
};

function createRequest(init: MockRequestInit): Request {
  const authorization = init.authorization;
  return {
    method: init.method,
    originalUrl: init.originalUrl ?? init.path,
    url: init.url ?? init.path,
    path: init.path,
    query: init.query ?? {},
    body: init.body ?? {},
    header: (name: string) => (name.toLowerCase() === 'authorization' ? authorization : undefined),
  } as unknown as Request;
}

describe('AuthMiddleware', () => {
  const middleware = new AuthMiddleware();
  const response = {} as Response;
  const currentUserId = '880100068483692100';

  function createAuthorization(userId: string): string {
    const token = buildSignedAccessToken(userId, { deviceId: 'auth-middleware-spec' });
    if (!token) {
      throw new Error('failed to build signed token');
    }
    return `Bearer ${token}`;
  }

  it('allows anonymous GET for bff media content', () => {
    const request = createRequest({
      method: 'GET',
      path: '/bff/media/MED1773921860354333/content',
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('allows anonymous GET for api media content', () => {
    const request = createRequest({
      method: 'GET',
      path: '/api/media/MED1773921860354333/content',
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('allows anonymous demo auto login', () => {
    const request = createRequest({
      method: 'POST',
      path: '/bff/auth/demo-auto-login',
      body: {
        deviceId: 'ios-device-demo',
      },
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('allows anonymous mobile verify login', () => {
    const request = createRequest({
      method: 'POST',
      path: '/bff/auth/mobile-verify-login',
      body: {
        loginId: '13920000001',
        deviceId: 'ios-device-demo',
      },
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('allows anonymous preset login account query', () => {
    const request = createRequest({
      method: 'GET',
      path: '/bff/auth/preset-login-accounts?deviceId=ios-device-demo',
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('allows anonymous behavior event reporting', () => {
    const request = createRequest({
      method: 'POST',
      path: '/bff/apps/behavior-events',
      body: {
        appCode: 'OPENAIPAY_IOS',
        deviceId: 'ios-device-demo',
        eventName: 'app_launch',
      },
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
  });

  it('still requires token for media upload', () => {
    const request = createRequest({
      method: 'POST',
      path: '/bff/media/images/upload?ownerUserId=880902068943900002',
      query: {
        ownerUserId: '880902068943900002',
      },
    });
    const next = jest.fn();

    expect(() => middleware.use(request, response, next)).toThrow(UnauthorizedException);
    expect(next).not.toHaveBeenCalled();
  });

  it('allows applying friend request when requesterUserId matches token but targetUserId differs', () => {
    const request = createRequest({
      method: 'POST',
      path: '/bff/contacts/requests',
      authorization: createAuthorization(currentUserId),
      body: {
        requesterUserId: currentUserId,
        targetUserId: '880902068943900002',
      },
    });
    const next = jest.fn();

    middleware.use(request, response, next);

    expect(next).toHaveBeenCalledTimes(1);
    expect(request.authenticatedUserId).toBe(currentUserId);
  });

  it('still forbids querying received requests with mismatched targetUserId', () => {
    const request = createRequest({
      method: 'GET',
      path: '/bff/contacts/requests/received',
      authorization: createAuthorization(currentUserId),
      query: {
        targetUserId: '880902068943900002',
      },
    });
    const next = jest.fn();

    expect(() => middleware.use(request, response, next)).toThrow(ForbiddenException);
    expect(next).not.toHaveBeenCalled();
  });
});
