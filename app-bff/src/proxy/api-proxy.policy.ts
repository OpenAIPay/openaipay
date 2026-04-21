const DEFAULT_ALLOWED_PREFIXES = ['/api/short-video'] as const;

const DEFAULT_BLOCKED_PREFIXES = ['/api/admin', '/api/mock'] as const;

function parsePrefixes(raw: string | undefined): string[] {
  if (!raw) {
    return [];
  }
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
    .map((item) => (item.startsWith('/') ? item : `/${item}`));
}

function normalizePrefixes(raw: string | undefined, fallback: readonly string[]): string[] {
  const configured = parsePrefixes(raw);
  const merged = [...fallback, ...configured];
  const deduped: string[] = [];
  for (const prefix of merged) {
    if (!deduped.includes(prefix)) {
      deduped.push(prefix);
    }
  }
  return deduped;
}

function matchesPrefix(pathname: string, prefix: string): boolean {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

export function isAllowedProxyPath(pathname: string): boolean {
  const allowedPrefixes = normalizePrefixes(process.env.API_PROXY_ALLOWED_PREFIXES, DEFAULT_ALLOWED_PREFIXES);
  const blockedPrefixes = normalizePrefixes(process.env.API_PROXY_BLOCKED_PREFIXES, DEFAULT_BLOCKED_PREFIXES);
  if (!pathname.startsWith('/api/')) {
    return false;
  }
  if (blockedPrefixes.some((prefix) => matchesPrefix(pathname, prefix))) {
    return false;
  }
  return allowedPrefixes.some((prefix) => matchesPrefix(pathname, prefix));
}

export function currentProxyPolicy(): { allowedPrefixes: string[]; blockedPrefixes: string[] } {
  const allowedPrefixes = normalizePrefixes(process.env.API_PROXY_ALLOWED_PREFIXES, DEFAULT_ALLOWED_PREFIXES);
  const blockedPrefixes = normalizePrefixes(process.env.API_PROXY_BLOCKED_PREFIXES, DEFAULT_BLOCKED_PREFIXES);
  return {
    allowedPrefixes: [...allowedPrefixes],
    blockedPrefixes: [...blockedPrefixes],
  };
}
