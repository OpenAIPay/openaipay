import { isAllowedProxyPath } from './api-proxy.policy';

describe('api proxy policy', () => {
  const originalAllowed = process.env.API_PROXY_ALLOWED_PREFIXES;
  const originalBlocked = process.env.API_PROXY_BLOCKED_PREFIXES;

  beforeEach(() => {
    delete process.env.API_PROXY_ALLOWED_PREFIXES;
    delete process.env.API_PROXY_BLOCKED_PREFIXES;
  });

  afterAll(() => {
    if (originalAllowed === undefined) {
      delete process.env.API_PROXY_ALLOWED_PREFIXES;
    } else {
      process.env.API_PROXY_ALLOWED_PREFIXES = originalAllowed;
    }
    if (originalBlocked === undefined) {
      delete process.env.API_PROXY_BLOCKED_PREFIXES;
    } else {
      process.env.API_PROXY_BLOCKED_PREFIXES = originalBlocked;
    }
  });

  it('blocks all mobile-facing business APIs by default', () => {
    expect(isAllowedProxyPath('/api/trade/pay')).toBe(false);
    expect(isAllowedProxyPath('/api/media/avatar.jpg')).toBe(false);
  });

  it('blocks admin and mock APIs from going through app-bff', () => {
    expect(isAllowedProxyPath('/api/auth/mobile-verify-login')).toBe(false);
    expect(isAllowedProxyPath('/api/apps/devices')).toBe(false);
    expect(isAllowedProxyPath('/api/assets/users/880100068483692100/overview')).toBe(false);
    expect(isAllowedProxyPath('/api/cashier/users/880100068483692100/payment-tools?sceneCode=TRANSFER')).toBe(false);
    expect(isAllowedProxyPath('/api/contacts/friends/880100068483692100?limit=20')).toBe(false);
    expect(isAllowedProxyPath('/api/conversations/users/880100068483692100?limit=20')).toBe(false);
    expect(isAllowedProxyPath('/api/messages/text')).toBe(false);
    expect(isAllowedProxyPath('/api/users/880100068483692100/profile')).toBe(false);
    expect(isAllowedProxyPath('/api/admin/users')).toBe(false);
    expect(isAllowedProxyPath('/api/mock/nucc/deposit/apply')).toBe(false);
  });

  it('allows explicitly configured proxy prefixes through environment override', () => {
    process.env.API_PROXY_ALLOWED_PREFIXES = '/api/trade,/api/media';

    expect(isAllowedProxyPath('/api/trade/pay')).toBe(true);
    expect(isAllowedProxyPath('/api/media/avatar.jpg')).toBe(true);
    expect(isAllowedProxyPath('/api/users/880100068483692100/profile')).toBe(false);
  });
});
