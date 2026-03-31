import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication } from '@nestjs/common';
import { createHmac } from 'node:crypto';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { AccountService } from './../src/account/account.service';
import { AuthService } from './../src/auth/auth.service';
import { AuthMiddleware } from './../src/common/auth.middleware';
import { BackendHttpService } from './../src/common/backend-http.service';
import { RequestContextMiddleware } from './../src/common/request-context.middleware';
import { PageInitService } from './../src/page-init/page-init.service';
import { SearchService } from './../src/search/search.service';
import { UserService } from './../src/user/user.service';

describe('AppController (e2e)', () => {
  let app: INestApplication<App>;
  let pageInitService: { build: jest.Mock };
  let authService: { mobileVerifyLogin: jest.Mock };
  let accountService: { createFundFastRedeem: jest.Mock };
  let backendHttpService: { getMetricsSnapshot: jest.Mock };
  let searchService: { search: jest.Mock };
  let userService: { getProfile: jest.Mock };

  beforeEach(async () => {
    pageInitService = {
      build: jest.fn().mockResolvedValue({
        page: 'home',
        user: { userId: '880100068483692100', nickname: '顾郡' },
      }),
    };
    authService = {
      mobileVerifyLogin: jest.fn().mockResolvedValue({
        accessToken: 'token-1',
        tokenType: 'Bearer',
        expiresInSeconds: 7200,
      }),
    };
    accountService = {
      createFundFastRedeem: jest.fn().mockResolvedValue({
        orderNo: '302100000000000001',
        status: 'PROCESSING',
      }),
    };
    backendHttpService = {
      getMetricsSnapshot: jest.fn().mockReturnValue({
        totalRequests: 4,
        failedRequests: 1,
      }),
    };
    searchService = {
      search: jest.fn().mockResolvedValue({
        keyword: '红包',
        sections: [
          {
            type: 'features',
            title: '功能',
            count: 1,
            items: [{ featureKey: 'red-packet', title: '红包', route: { type: 'page', page: 'redPacket' } }],
          },
        ],
        empty: false,
      }),
    };
    userService = {
      getProfile: jest.fn().mockResolvedValue({
        userId: '880100068483692100',
        nickname: '顾郡',
        accountStatus: 'ACTIVE',
      }),
    };

    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    })
      .overrideProvider(PageInitService)
      .useValue(pageInitService)
      .overrideProvider(AuthService)
      .useValue(authService)
      .overrideProvider(AccountService)
      .useValue(accountService)
      .overrideProvider(BackendHttpService)
      .useValue(backendHttpService)
      .overrideProvider(SearchService)
      .useValue(searchService)
      .overrideProvider(UserService)
      .useValue(userService)
      .compile();

    app = moduleFixture.createNestApplication();
    const requestContextMiddleware = new RequestContextMiddleware();
    const authMiddleware = new AuthMiddleware();
    app.use((req, res, next) => requestContextMiddleware.use(req, res, next));
    app.use((req, res, next) => authMiddleware.use(req, res, next));
    await app.init();
  });

  afterEach(async () => {
    await app.close();
  });

  it('/ (GET)', () => {
    return request(app.getHttpServer())
      .get('/')
      .expect(200)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: true,
          data: {
            service: 'app-bff',
            status: 'running',
            endpoints: {
              health: '/health',
              pageInit: '/bff/page-init?page=home&uid=880100068483692100',
              login: '/bff/auth/mobile-verify-login',
              profileExample: '/bff/users/880100068483692100/profile',
            },
          },
        });
        expect(typeof response.body.requestId).toBe('string');
        expect(typeof response.body.data.now).toBe('string');
      });
  });

  it('/health (GET) should return wrapped payload and preserve incoming request id', () => {
    return request(app.getHttpServer())
      .get('/health')
      .set('x-request-id', 'req-health-001')
      .expect(200)
      .expect('x-request-id', 'req-health-001')
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: true,
          requestId: 'req-health-001',
          data: {
            status: 'ok',
            service: 'app-bff',
          },
        });
        expect(typeof response.body.data.now).toBe('string');
      });
  });

  it('/health/backend-http (GET) should expose backend http metrics snapshot', () => {
    return request(app.getHttpServer())
      .get('/health/backend-http')
      .expect(200)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: true,
          data: {
            totalRequests: 4,
            failedRequests: 1,
          },
        });
      });
  });

  it('/bff/page-init (GET) should delegate to service with normalized uid', () => {
    return request(app.getHttpServer())
      .get('/bff/page-init?page=home&uid=880100068483692100')
      .set('x-request-id', 'req-page-init-001')
      .expect(200)
      .expect((response) => {
        expect(pageInitService.build).toHaveBeenCalledWith('home', '880100068483692100');
        expect(response.body).toMatchObject({
          success: true,
          requestId: 'req-page-init-001',
          data: {
            page: 'home',
            user: {
              userId: '880100068483692100',
              nickname: '顾郡',
            },
          },
        });
      });
  });

  it('/bff/page-init (GET) should support anonymous request without uid', () => {
    return request(app.getHttpServer())
      .get('/bff/page-init?page=home')
      .expect(200)
      .expect((response) => {
        expect(pageInitService.build).toHaveBeenCalledWith('home');
        expect(response.body).toMatchObject({
          success: true,
          data: {
            page: 'home',
          },
        });
      });
  });

  it('/bff/page-init (GET) should return standardized validation error for invalid uid', () => {
    return request(app.getHttpServer())
      .get('/bff/page-init?page=home&uid=abc')
      .set('x-request-id', 'req-page-init-bad')
      .expect(400)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: false,
          requestId: 'req-page-init-bad',
          error: {
            code: 'INVALID_ARGUMENT',
            message: 'uid 参数格式不正确，请使用字符串传递18位ID',
          },
          path: '/bff/page-init?page=home&uid=abc',
        });
      });
  });

  it('/bff/auth/mobile-verify-login (POST) should delegate to auth service and return wrapped result', () => {
    return request(app.getHttpServer())
      .post('/bff/auth/mobile-verify-login')
      .send({
        loginId: '13920000001',
        deviceId: 'ios-device-001',
      })
      .expect(200)
      .expect((response) => {
        expect(authService.mobileVerifyLogin).toHaveBeenCalledWith(
          '13920000001',
          'ios-device-001',
          undefined,
          undefined,
        );
        expect(response.body).toMatchObject({
          success: true,
          data: {
            accessToken: 'token-1',
            tokenType: 'Bearer',
            expiresInSeconds: 7200,
          },
        });
      });
  });

  it('/bff/auth/mobile-verify-login (POST) should return standardized validation error for blank loginId or deviceId', () => {
    return request(app.getHttpServer())
      .post('/bff/auth/mobile-verify-login')
      .send({
        loginId: '  ',
        deviceId: 'ios-device-001',
      })
      .expect(400)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: false,
          error: {
            code: 'INVALID_ARGUMENT',
            message: 'loginId 和 deviceId 不能为空',
          },
        });
      });
  });

  it('/bff/users/:userId/profile (GET) should reject missing bearer token', () => {
    return request(app.getHttpServer())
      .get('/bff/users/880100068483692100/profile')
      .expect(401)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: false,
          error: {
            code: 'UNAUTHORIZED',
            message: '用户鉴权失败：缺少有效 Bearer Token',
          },
        });
      });
  });

  it('/bff/users/:userId/profile (GET) should reject mismatched asserted user id', () => {
    return request(app.getHttpServer())
      .get('/bff/users/880100068483692100/profile')
      .set('authorization', bearerToken('880100068483692101'))
      .expect(403)
      .expect((response) => {
        expect(response.body).toMatchObject({
          success: false,
          error: {
            code: 'FORBIDDEN',
            message: '用户鉴权失败：请求用户与令牌不匹配',
          },
        });
      });
  });

  it('/bff/users/:userId/profile (GET) should accept matching signed bearer token', () => {
    return request(app.getHttpServer())
      .get('/bff/users/880100068483692100/profile')
      .set('authorization', bearerToken('880100068483692100'))
      .set('x-request-id', 'req-user-profile-001')
      .expect(200)
      .expect('x-request-id', 'req-user-profile-001')
      .expect((response) => {
        expect(userService.getProfile).toHaveBeenCalledWith('880100068483692100');
        expect(response.body).toMatchObject({
          success: true,
          requestId: 'req-user-profile-001',
          data: {
            userId: '880100068483692100',
            nickname: '顾郡',
            accountStatus: 'ACTIVE',
          },
        });
      });
  });

  it('/bff/search (GET) should delegate to search service and return wrapped result', () => {
    return request(app.getHttpServer())
      .get('/bff/search?uid=880100068483692100&keyword=红包')
      .set('authorization', bearerToken('880100068483692100'))
      .expect(200)
      .expect((response) => {
        expect(searchService.search).toHaveBeenCalledWith('880100068483692100', '红包', undefined);
        expect(response.body).toMatchObject({
          success: true,
          data: {
            keyword: '红包',
            empty: false,
            sections: [
              {
                type: 'features',
                items: [{ featureKey: 'red-packet', title: '红包' }],
              },
            ],
          },
        });
      });
  });

  it('/bff/accounts/fund/fast-redeem (POST) should normalize decimal share and still delegate to account service', () => {
    return request(app.getHttpServer())
      .post('/bff/accounts/fund/fast-redeem')
      .set('authorization', bearerToken('880100068483692100'))
      .send({
        orderNo: '302100000000000001',
        userId: '880100068483692100',
        fundCode: 'AICASH',
        share: '1.0000',
      })
      .expect(201)
      .expect((response) => {
        expect(accountService.createFundFastRedeem).toHaveBeenCalledWith({
          orderNo: '302100000000000001',
          userId: '880100068483692100',
          fundCode: 'AICASH',
          share: 1,
        });
        expect(response.body).toMatchObject({
          success: true,
          data: {
            orderNo: '302100000000000001',
            status: 'PROCESSING',
          },
        });
      });
  });
});

function bearerToken(userId: string): string {
  const payload = `${userId}:1710000000:1710003600:test-device:e2e`;
  const payloadBase64Url = Buffer.from(payload, 'utf8').toString('base64url');
  const signature = createHmac('sha256', 'openaipay-local-signing-secret-please-change')
    .update(Buffer.from(payload, 'utf8'))
    .digest('base64url');
  return `Bearer ${payloadBase64Url}.${signature}`;
}
