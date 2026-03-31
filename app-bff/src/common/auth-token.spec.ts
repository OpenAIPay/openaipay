import { createHmac } from 'node:crypto';
import { resolveSubjectIdFromAccessToken, resolveSubjectIdFromAuthorizationHeader } from './auth-token';

const DEFAULT_SECRET = 'openaipay-local-signing-secret-please-change';
const LEGACY_SECRET = 'openaipay-local-signing-secret-20260323-please-change';

type EnvSnapshot = {
  primary: string | undefined;
};

function buildSignedToken(subjectId: string, secret: string): string {
  const issuedAt = Math.floor(Date.now() / 1000);
  const expiresAt = issuedAt + 3600;
  const payload = `${subjectId}:${issuedAt}:${expiresAt}:auth-token-spec:${Date.now()}`;
  const payloadBuffer = Buffer.from(payload, 'utf8');
  const signatureBuffer = createHmac('sha256', secret).update(payloadBuffer).digest();
  return `${payloadBuffer.toString('base64url')}.${signatureBuffer.toString('base64url')}`;
}

function buildUnsignedLegacyToken(subjectId: string): string {
  const payload = `${subjectId}:${Math.floor(Date.now() / 1000)}`;
  return Buffer.from(payload, 'utf8').toString('base64url');
}

describe('auth-token final signing behavior', () => {
  let envSnapshot: EnvSnapshot;

  beforeEach(() => {
    envSnapshot = {
      primary: process.env.OPENAIPAY_TOKEN_SIGNING_SECRET,
    };
    delete process.env.OPENAIPAY_TOKEN_SIGNING_SECRET;
  });

  afterEach(() => {
    if (envSnapshot.primary === undefined) {
      delete process.env.OPENAIPAY_TOKEN_SIGNING_SECRET;
    } else {
      process.env.OPENAIPAY_TOKEN_SIGNING_SECRET = envSnapshot.primary;
    }
  });

  it('accepts token signed with current default secret', () => {
    const userId = '880100068483692100';
    const token = buildSignedToken(userId, DEFAULT_SECRET);

    expect(resolveSubjectIdFromAccessToken(token)).toBe(userId);
    expect(resolveSubjectIdFromAuthorizationHeader(`Bearer ${token}`)).toBe(userId);
  });

  it('accepts token signed with configured primary secret', () => {
    const userId = '880100070648333000';
    process.env.OPENAIPAY_TOKEN_SIGNING_SECRET = 'custom-primary-secret';
    const token = buildSignedToken(userId, 'custom-primary-secret');

    expect(resolveSubjectIdFromAccessToken(token)).toBe(userId);
    expect(resolveSubjectIdFromAuthorizationHeader(`Bearer ${token}`)).toBe(userId);
  });

  it('rejects token signed with legacy secret', () => {
    const userId = '880100068483692100';
    const token = buildSignedToken(userId, LEGACY_SECRET);

    expect(resolveSubjectIdFromAccessToken(token)).toBeNull();
    expect(resolveSubjectIdFromAuthorizationHeader(`Bearer ${token}`)).toBeNull();
  });

  it('rejects unsigned legacy token payload', () => {
    const token = buildUnsignedLegacyToken('880100068483692100');
    expect(resolveSubjectIdFromAccessToken(token)).toBeNull();
  });
});
