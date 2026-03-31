import { createHmac, timingSafeEqual } from 'node:crypto';

const BEARER_PREFIX = 'Bearer ';
const DEFAULT_TOKEN_SIGNING_SECRET = 'openaipay-local-signing-secret-please-change';
const DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 7 * 24 * 3600;
const LEGACY_TOKEN_SIGNING_SECRETS = [DEFAULT_TOKEN_SIGNING_SECRET];

export function extractBearerToken(rawAuthorizationHeader: unknown): string | null {
  if (typeof rawAuthorizationHeader !== 'string') {
    return null;
  }
  const normalized = rawAuthorizationHeader.trim();
  if (!normalized || normalized.length <= BEARER_PREFIX.length) {
    return null;
  }
  if (!normalized.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
    return null;
  }
  const token = normalized.slice(BEARER_PREFIX.length).trim();
  return token || null;
}

export function resolveSubjectIdFromAuthorizationHeader(
  rawAuthorizationHeader: unknown,
): string | null {
  const token = extractBearerToken(rawAuthorizationHeader);
  if (!token) {
    return null;
  }
  return resolveSubjectIdFromAccessToken(token);
}

export function resolveSubjectIdFromAccessToken(rawAccessToken: unknown): string | null {
  if (typeof rawAccessToken !== 'string') {
    return null;
  }
  const normalized = rawAccessToken.trim();
  if (!normalized) {
    return null;
  }

  const separator = normalized.indexOf('.');
  if (separator > 0 && separator < normalized.length - 1) {
    const payloadPart = normalized.slice(0, separator);
    const signaturePart = normalized.slice(separator + 1);
    const payloadBuffer = decodeBase64Url(payloadPart);
    const signatureBuffer = decodeBase64Url(signaturePart);
    if (!payloadBuffer || !signatureBuffer) {
      return null;
    }
    if (!isValidSignature(payloadBuffer, signatureBuffer)) {
      return null;
    }
    return resolveSubjectIdFromPayload(payloadBuffer.toString('utf8'));
  }
  return null;
}

export function buildSignedAccessToken(
  subjectId: string,
  options?: { deviceId?: string; expiresInSeconds?: number },
): string | null {
  const normalizedSubjectId = typeof subjectId === 'string' ? subjectId.trim() : '';
  if (!/^\d{1,20}$/.test(normalizedSubjectId)) {
    return null;
  }
  const now = Math.floor(Date.now() / 1000);
  const requestedExpires = Math.trunc(options?.expiresInSeconds ?? DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS);
  const expiresInSeconds = Number.isFinite(requestedExpires) && requestedExpires > 0
    ? requestedExpires
    : DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS;
  const expireAt = now + expiresInSeconds;
  const deviceId = typeof options?.deviceId === 'string' && options.deviceId.trim()
    ? options.deviceId.trim()
    : 'app-bff';
  const nonce = `${Date.now()}${Math.floor(Math.random() * 1_000_000)}`;
  const payload = `${normalizedSubjectId}:${now}:${expireAt}:${deviceId}:${nonce}`;
  const payloadBuffer = Buffer.from(payload, 'utf8');
  const signatureBuffer = createHmac('sha256', resolveSigningSecret()).update(payloadBuffer).digest();
  return `${payloadBuffer.toString('base64url')}.${signatureBuffer.toString('base64url')}`;
}

function resolveSubjectIdFromPayload(payload: string): string | null {
  const separator = payload.indexOf(':');
  const candidate = (separator >= 0 ? payload.slice(0, separator) : payload).trim();
  if (!/^\d{1,20}$/.test(candidate)) {
    return null;
  }
  return candidate;
}

function decodeBase64Url(encoded: string): Buffer | null {
  const normalized = encoded.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
  try {
    return Buffer.from(padded, 'base64');
  } catch {
    return null;
  }
}

function isValidSignature(payload: Buffer, signature: Buffer): boolean {
  const secrets = resolveSigningSecrets();
  for (const secret of secrets) {
    const expected = createHmac('sha256', secret).update(payload).digest();
    if (expected.length !== signature.length) {
      continue;
    }
    if (timingSafeEqual(expected, signature)) {
      return true;
    }
  }
  return false;
}

function resolveSigningSecret(): string {
  return resolveSigningSecrets()[0];
}

function resolveSigningSecrets(): string[] {
  const fromEnv = process.env.OPENAIPAY_TOKEN_SIGNING_SECRET;
  const normalizedPrimary = typeof fromEnv === 'string' ? fromEnv.trim() : '';
  const legacyRaw = process.env.OPENAIPAY_TOKEN_SIGNING_SECRET_LEGACY;
  const envLegacyList = typeof legacyRaw === 'string'
    ? legacyRaw
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0)
    : [];
  const allSecrets = [
    normalizedPrimary || DEFAULT_TOKEN_SIGNING_SECRET,
    ...envLegacyList,
    ...LEGACY_TOKEN_SIGNING_SECRETS,
  ];
  return Array.from(new Set(allSecrets.filter((item) => item.length > 0)));
}
