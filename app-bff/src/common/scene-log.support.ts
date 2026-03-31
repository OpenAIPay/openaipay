import type { Request } from 'express';

const MAX_LOG_TEXT_LENGTH = 4000;
const MAX_DEPTH = 4;
const MAX_ARRAY_SIZE = 20;
const MAX_OBJECT_KEYS = 30;
const SCENE_KEYWORDS: Array<[keyword: string, scene: string]> = [
  ['deposit', '充值'],
  ['withdraw', '提现'],
  ['transfer', '转账'],
  ['refund', '退款'],
  ['pay', '支付'],
  ['cashier', '收银台'],
  ['message', '消息'],
  ['conversation', '会话'],
  ['contact', '联系人'],
  ['feedback', '反馈与投诉'],
  ['bankcard', '银行卡'],
  ['credit', '爱花'],
  ['loan', '爱借'],
  ['fund', '爱存'],
  ['asset', '资产'],
  ['bill', '账单'],
  ['auth', '鉴权'],
  ['user', '用户'],
  ['search', '搜索'],
  ['deliver', '投放'],
  ['proxy', '接口代理'],
  ['health', '健康检查'],
  ['app', '应用'],
];
const SENSITIVE_KEYWORDS = ['password', 'pwd', 'token', 'authorization', 'idcard', 'id_card', 'credential', 'secret'];

export const ATTR_SCENE = 'aipayBffRequestLogScene';
export const ATTR_REQUEST_PAYLOAD = 'aipayBffRequestPayload';
export const ATTR_BIZ_ERROR_LOGGED = 'aipayBffBizErrorLogged';

export function resolveScene(req: Request, fallback = '通用接口'): string {
  const cachedScene = normalizeScene(req[ATTR_SCENE]);
  if (cachedScene) {
    return cachedScene;
  }
  const candidates = [req.originalUrl, req.path, req.route?.path]
    .filter((item): item is string => typeof item === 'string' && item.trim().length > 0)
    .map((item) => item.toLowerCase());
  for (const candidate of candidates) {
    for (const [keyword, scene] of SCENE_KEYWORDS) {
      if (candidate.includes(keyword)) {
        return scene;
      }
    }
  }
  return fallback;
}

export function buildRequestPayload(req: Request): string {
  const cachedPayload = req[ATTR_REQUEST_PAYLOAD];
  if (typeof cachedPayload === 'string' && cachedPayload.trim().length > 0) {
    return truncate(cachedPayload);
  }
  const payload: Record<string, unknown> = {
    method: req.method ?? 'UNKNOWN',
    path: req.originalUrl || req.url,
    params: sanitize(req.params ?? {}),
    query: sanitize(req.query ?? {}),
  };
  if (!isReadOnlyMethod(req.method)) {
    payload.body = sanitize(req.body);
  }
  return truncate(stringify(payload));
}

export function markBizErrorLogged(req: Request): void {
  req[ATTR_BIZ_ERROR_LOGGED] = true;
}

export function isBizErrorLogged(req: Request): boolean {
  return req[ATTR_BIZ_ERROR_LOGGED] === true;
}

export function resolveBackendScene(routeName: string, method: string): string {
  const normalizedRoute = (routeName ?? '').toLowerCase();
  for (const [keyword, scene] of SCENE_KEYWORDS) {
    if (normalizedRoute.includes(keyword)) {
      return scene;
    }
  }
  if (method === 'GET' || method === 'HEAD') {
    return '查询';
  }
  return '后端调用';
}

export function buildBackendRequestPayload(
  method: string,
  path: string,
  routeName: string,
  timeoutMs: number,
  data: unknown,
): string {
  const payload: Record<string, unknown> = {
    method,
    path,
    route: routeName,
    timeoutMs,
  };
  if (!isReadOnlyMethod(method) && data !== undefined) {
    payload.body = sanitize(data);
  }
  return truncate(stringify(payload));
}

function normalizeScene(input: unknown): string | null {
  if (typeof input !== 'string') {
    return null;
  }
  const trimmed = input.trim();
  if (!trimmed) {
    return null;
  }
  return trimmed.length > 40 ? trimmed.slice(0, 40) : trimmed;
}

function sanitize(value: unknown, depth = 0): unknown {
  if (value === null || value === undefined) {
    return value;
  }
  if (depth >= MAX_DEPTH) {
    return '[DepthLimit]';
  }
  if (typeof value === 'string') {
    return value.length > 600 ? `${value.slice(0, 600)}...` : value;
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return value;
  }
  if (Array.isArray(value)) {
    const size = Math.min(value.length, MAX_ARRAY_SIZE);
    const items = value.slice(0, size).map((item) => sanitize(item, depth + 1));
    if (value.length > size) {
      items.push(`[...${value.length - size} more]`);
    }
    return items;
  }
  if (typeof value === 'object') {
    const asRecord = value as Record<string, unknown>;
    const entries = Object.entries(asRecord);
    const result: Record<string, unknown> = {};
    const size = Math.min(entries.length, MAX_OBJECT_KEYS);
    for (const [key, item] of entries.slice(0, size)) {
      if (isSensitiveKey(key)) {
        result[key] = '[MASKED]';
        continue;
      }
      result[key] = sanitize(item, depth + 1);
    }
    if (entries.length > size) {
      result.__truncated__ = entries.length - size;
    }
    return result;
  }
  return String(value);
}

function isSensitiveKey(key: string): boolean {
  const lowered = key.toLowerCase();
  return SENSITIVE_KEYWORDS.some((keyword) => lowered.includes(keyword));
}

function isReadOnlyMethod(method: string | undefined): boolean {
  const upper = (method ?? 'GET').toUpperCase();
  return upper === 'GET' || upper === 'HEAD' || upper === 'OPTIONS';
}

function stringify(value: unknown): string {
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function truncate(raw: string): string {
  if (raw.length <= MAX_LOG_TEXT_LENGTH) {
    return raw;
  }
  return `${raw.slice(0, MAX_LOG_TEXT_LENGTH)}...`;
}
