import { HttpService } from '@nestjs/axios';
import { Injectable, Logger } from '@nestjs/common';
import type { AxiosRequestConfig, AxiosResponse } from 'axios';
import { Agent as HttpAgent } from 'http';
import { Agent as HttpsAgent } from 'https';
import { firstValueFrom } from 'rxjs';
import { buildSignedAccessToken } from './auth-token';
import { getCurrentAuthorizationHeader, getCurrentRequestId, getCurrentUserId } from './request-context';
import { buildBackendRequestPayload, resolveBackendScene } from './scene-log.support';

type BackendRequestOptions<D = unknown> = Omit<AxiosRequestConfig<D>, 'baseURL' | 'timeout' | 'url'> & {
  path: string;
  timeoutMs?: number;
};

type BackendRoutePolicy = {
  name: string;
  methods?: string[];
  pattern: RegExp;
  timeoutMs?: number;
  cacheTtlMs?: number;
};

type BackendRouteMetric = {
  route: string;
  requestCount: number;
  successCount: number;
  errorCount: number;
  cacheHitCount: number;
  totalLatencyMs: number;
  lastLatencyMs: number;
  lastStatusCode: number | null;
  lastRequestAt: string | null;
};

type BackendRouteMetricSnapshot = BackendRouteMetric & {
  averageLatencyMs: number;
};

type CachedBackendResponse = {
  expiresAt: number;
  response: AxiosResponse<unknown>;
};

@Injectable()
export class BackendHttpService {
  private readonly log = new Logger(BackendHttpService.name);
  private readonly backendBaseUrl = process.env.BACKEND_BASE_URL ?? 'http://127.0.0.1:8080';
  private readonly defaultTimeoutMs = this.resolveDefaultTimeoutMs();
  private readonly queryMinimumTimeoutMs = 10_000;
  private readonly routePolicies = this.buildRoutePolicies();
  private readonly metrics = new Map<string, BackendRouteMetric>();
  private readonly responseCache = new Map<string, CachedBackendResponse>();
  private readonly httpAgent = new HttpAgent({
    keepAlive: true,
    keepAliveMsecs: 30000,
    maxSockets: 200,
    maxFreeSockets: 20,
  });
  private readonly httpsAgent = new HttpsAgent({
    keepAlive: true,
    keepAliveMsecs: 30000,
    maxSockets: 200,
    maxFreeSockets: 20,
  });
  private readonly retryHttpAgent = new HttpAgent({
    keepAlive: false,
  });
  private readonly retryHttpsAgent = new HttpsAgent({
    keepAlive: false,
  });

  constructor(private readonly httpService: HttpService) {}

  get backendOrigin(): string {
    return this.backendBaseUrl;
  }

  async request<T = unknown, D = unknown>(options: BackendRequestOptions<D>): Promise<AxiosResponse<T, D>> {
    const { path, timeoutMs, headers, ...rest } = options;
    const method = (rest.method ?? 'GET').toUpperCase();
    const routePolicy = this.resolveRoutePolicy(path, method);
    const resolvedTimeoutMs = this.resolveRequestTimeoutMs(timeoutMs, routePolicy.timeoutMs, method);
    const primaryHeaders = this.buildHeaders(headers, true, path, rest.data);
    const alternateHeaders = this.buildHeaders(headers, false, path, rest.data);
    const hasAlternateAuthorization =
      this.resolveAuthorizationHeaderValue(primaryHeaders) !== this.resolveAuthorizationHeaderValue(alternateHeaders);
    const cacheKey = this.resolveCacheKey(path, method, routePolicy);
    const metric = this.resolveMetric(routePolicy.name);
    const scene = resolveBackendScene(routePolicy.name, method);
    const requestPayload = buildBackendRequestPayload(method, path, routePolicy.name, resolvedTimeoutMs, rest.data);

    metric.requestCount += 1;
    metric.lastRequestAt = new Date().toISOString();
    this.log.log(`[${scene}]入参：${requestPayload}`);

    if (cacheKey) {
      const cached = this.getCachedResponse(cacheKey);
      if (cached) {
        metric.cacheHitCount += 1;
        metric.successCount += 1;
        metric.lastStatusCode = cached.status;
        metric.lastLatencyMs = 0;
        return cached as AxiosResponse<T, D>;
      }
    }

    const startedAt = Date.now();
    try {
      const response = await this.performRequestAttempt<T>({
        ...rest,
        method,
        url: this.resolveUrl(path),
        headers: primaryHeaders,
        timeout: resolvedTimeoutMs,
      });
      const latencyMs = Date.now() - startedAt;
      this.recordSuccess(metric, latencyMs, response.status);
      if (cacheKey && response.status >= 200 && response.status < 300) {
        this.responseCache.set(cacheKey, {
          expiresAt: Date.now() + (routePolicy.cacheTtlMs ?? 0),
          response: this.cloneResponse(response),
        });
      }
      return response as AxiosResponse<T, D>;
    } catch (error) {
      let resolvedError: unknown = error;
      if (hasAlternateAuthorization && this.isUnauthorizedStatus(resolvedError)) {
        try {
          const fallbackResponse = await this.performRequestAttempt<T>({
            ...rest,
            method,
            url: this.resolveUrl(path),
            headers: alternateHeaders,
            timeout: resolvedTimeoutMs,
          });
          const latencyMs = Date.now() - startedAt;
          this.recordSuccess(metric, latencyMs, fallbackResponse.status);
          if (cacheKey && fallbackResponse.status >= 200 && fallbackResponse.status < 300) {
            this.responseCache.set(cacheKey, {
              expiresAt: Date.now() + (routePolicy.cacheTtlMs ?? 0),
              response: this.cloneResponse(fallbackResponse),
            });
          }
          return fallbackResponse as AxiosResponse<T, D>;
        } catch (fallbackError) {
          resolvedError = fallbackError;
        }
      }
      if (this.shouldRetryRequest(resolvedError, method)) {
        try {
          const response = await this.performRequestAttempt<T>(
            {
              ...rest,
              method,
              url: this.resolveUrl(path),
              headers: primaryHeaders,
              timeout: resolvedTimeoutMs,
            },
            true,
          );
          const latencyMs = Date.now() - startedAt;
          this.recordSuccess(metric, latencyMs, response.status);
          if (cacheKey && response.status >= 200 && response.status < 300) {
            this.responseCache.set(cacheKey, {
              expiresAt: Date.now() + (routePolicy.cacheTtlMs ?? 0),
              response: this.cloneResponse(response),
            });
          }
          return response as AxiosResponse<T, D>;
        } catch (retryError) {
          const latencyMs = Date.now() - startedAt;
          const statusCode = this.resolveStatusCode(retryError);
          this.recordError(metric, latencyMs, statusCode);
          this.log.error(`[${scene}]业务异常, request:${requestPayload}`, this.resolveErrorStack(retryError));
          throw retryError;
        }
      }
      const latencyMs = Date.now() - startedAt;
      const statusCode = this.resolveStatusCode(resolvedError);
      this.recordError(metric, latencyMs, statusCode);
      this.log.error(`[${scene}]业务异常, request:${requestPayload}`, this.resolveErrorStack(resolvedError));
      throw resolvedError;
    }
  }

  async get<T = unknown>(path: string, options?: Omit<BackendRequestOptions<never>, 'method' | 'path' | 'data'>): Promise<AxiosResponse<T>> {
    return this.request<T>({
      ...options,
      method: 'GET',
      path,
    });
  }

  async post<T = unknown, D = unknown>(
    path: string,
    data?: D,
    options?: Omit<BackendRequestOptions<D>, 'method' | 'path' | 'data'>,
  ): Promise<AxiosResponse<T, D>> {
    return this.request<T, D>({
      ...options,
      method: 'POST',
      path,
      data,
    });
  }

  async put<T = unknown, D = unknown>(
    path: string,
    data?: D,
    options?: Omit<BackendRequestOptions<D>, 'method' | 'path' | 'data'>,
  ): Promise<AxiosResponse<T, D>> {
    return this.request<T, D>({
      ...options,
      method: 'PUT',
      path,
      data,
    });
  }

  resolveAbsoluteUrl(raw: string | undefined): string {
    if (!raw) {
      return '';
    }
    const normalized = raw.trim();
    if (!normalized) {
      return '';
    }
    if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
      return normalized;
    }
    if (normalized.startsWith('/')) {
      return `${this.backendBaseUrl}${normalized}`;
    }
    return normalized;
  }

  getMetricsSnapshot(): {
    backendOrigin: string;
    defaultTimeoutMs: number;
    policies: Array<{ name: string; methods: string[]; pattern: string; timeoutMs?: number; cacheTtlMs?: number }>;
    routes: BackendRouteMetricSnapshot[];
    cache: { size: number };
  } {
    this.pruneExpiredCache();
    return {
      backendOrigin: this.backendBaseUrl,
      defaultTimeoutMs: this.defaultTimeoutMs,
      policies: this.routePolicies.map((policy) => ({
        name: policy.name,
        methods: policy.methods ?? ['ALL'],
        pattern: policy.pattern.source,
        timeoutMs: policy.timeoutMs,
        cacheTtlMs: policy.cacheTtlMs,
      })),
      routes: Array.from(this.metrics.values())
        .map((metric) => ({
          ...metric,
          averageLatencyMs:
            metric.successCount + metric.errorCount > 0
              ? Math.round(metric.totalLatencyMs / (metric.successCount + metric.errorCount))
              : 0,
        }))
        .sort((left, right) => left.route.localeCompare(right.route)),
      cache: {
        size: this.responseCache.size,
      },
    };
  }

  private resolveUrl(path: string): string {
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path;
    }
    return path.startsWith('/') ? `${this.backendBaseUrl}${path}` : `${this.backendBaseUrl}/${path}`;
  }

  private buildHeaders(
    headers: AxiosRequestConfig['headers'],
    preferRotatedAuthorization = true,
    path?: string,
    body?: unknown,
  ): Record<string, string> {
    const requestHeaders: Record<string, string> = {
      'x-bff-source': 'app-bff',
    };
    const requestId = getCurrentRequestId();
    if (requestId) {
      requestHeaders['x-request-id'] = requestId;
    }
    const resolvedAuthorization = this.resolveAuthorizationHeader(preferRotatedAuthorization);
    if (resolvedAuthorization) {
      requestHeaders.Authorization = resolvedAuthorization;
    }
    if (!headers) {
      return requestHeaders;
    }
    Object.entries(headers).forEach(([key, value]) => {
      if (value === undefined) {
        return;
      }
      const normalizedValue = Array.isArray(value) ? value.join(',') : String(value);
      if (key.toLowerCase() === 'authorization') {
        requestHeaders.Authorization = normalizedValue;
        return;
      }
      requestHeaders[key] = normalizedValue;
    });
    if (!this.resolveAuthorizationHeaderValue(requestHeaders)) {
      const inferredAuthorization = this.resolveInferredAuthorizationHeader(path, body);
      if (inferredAuthorization) {
        requestHeaders.Authorization = inferredAuthorization;
      }
    }
    return requestHeaders;
  }

  private resolveAuthorizationHeader(preferRotatedAuthorization: boolean): string | undefined {
    const currentUserId = getCurrentUserId();
    const rotatedAuthorization = currentUserId
      ? buildSignedAccessToken(currentUserId, {
          deviceId: 'app-bff',
          expiresInSeconds: 7 * 24 * 3600,
        })
      : null;
    const rotatedAuthorizationHeader = rotatedAuthorization ? `Bearer ${rotatedAuthorization}` : undefined;
    const originalAuthorizationHeader = getCurrentAuthorizationHeader();
    if (preferRotatedAuthorization) {
      return rotatedAuthorizationHeader ?? originalAuthorizationHeader;
    }
    return originalAuthorizationHeader ?? rotatedAuthorizationHeader;
  }

  private resolveAuthorizationHeaderValue(headers: Record<string, string>): string {
    const matched = Object.entries(headers).find(([key]) => key.toLowerCase() === 'authorization');
    if (!matched) {
      return '';
    }
    return matched[1];
  }

  private resolveInferredAuthorizationHeader(path?: string, body?: unknown): string | undefined {
    const inferredUserId = this.inferUserIdFromRequest(path, body);
    if (!inferredUserId) {
      return undefined;
    }
    const inferredToken = buildSignedAccessToken(inferredUserId, {
      deviceId: 'app-bff-inferred',
      expiresInSeconds: 7 * 24 * 3600,
    });
    return inferredToken ? `Bearer ${inferredToken}` : undefined;
  }

  private inferUserIdFromRequest(path?: string, body?: unknown): string | null {
    const userIdFromPath = this.extractUserIdFromPath(path);
    if (userIdFromPath) {
      return userIdFromPath;
    }
    const userIdFromBody = this.extractUserIdFromBody(body);
    if (userIdFromBody) {
      return userIdFromBody;
    }
    return null;
  }

  private extractUserIdFromPath(path?: string): string | null {
    if (!path || typeof path !== 'string') {
      return null;
    }
    const normalized = path.trim();
    if (!normalized) {
      return null;
    }
    const queryIndex = normalized.indexOf('?');
    const pathPart = queryIndex >= 0 ? normalized.slice(0, queryIndex) : normalized;
    const queryPart = queryIndex >= 0 ? normalized.slice(queryIndex + 1) : '';

    const pathPatterns = [
      /\/users\/(\d{1,20})(?:\/|$)/,
      /\/owners\/(\d{1,20})(?:\/|$)/,
      /\/friends\/(\d{1,20})(?:\/|$)/,
      /\/bankcards\/users\/(\d{1,20})(?:\/|$)/,
    ];
    for (const pattern of pathPatterns) {
      const matched = pattern.exec(pathPart);
      if (matched && matched[1]) {
        return matched[1];
      }
    }

    if (queryPart) {
      const query = new URLSearchParams(queryPart);
      const userQueryKeys = ['userId', 'uid', 'ownerUserId', 'requesterUserId', 'operatorUserId', 'senderUserId', 'payerUserId'];
      for (const key of userQueryKeys) {
        const candidate = (query.get(key) ?? '').trim();
        if (/^\d{1,20}$/.test(candidate)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private extractUserIdFromBody(body: unknown): string | null {
    if (!body || typeof body !== 'object' || Array.isArray(body)) {
      return null;
    }
    const payload = body as Record<string, unknown>;
    const userKeys = ['userId', 'uid', 'ownerUserId', 'requesterUserId', 'operatorUserId', 'senderUserId', 'payerUserId'];
    for (const key of userKeys) {
      const value = payload[key];
      const normalized = this.normalizePositiveLongId(value);
      if (normalized) {
        return normalized;
      }
    }
    return null;
  }

  private normalizePositiveLongId(value: unknown): string | null {
    if (typeof value === 'number') {
      if (!Number.isSafeInteger(value) || value <= 0) {
        return null;
      }
      return String(Math.trunc(value));
    }
    if (typeof value !== 'string') {
      return null;
    }
    const normalized = value.trim();
    if (!/^\d{1,20}$/.test(normalized)) {
      return null;
    }
    return normalized;
  }

  private buildRoutePolicies(): BackendRoutePolicy[] {
    return [
      { name: 'user.init', methods: ['GET'], pattern: /^\/api\/users\/\d+\/init(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 10_000 },
      { name: 'user.profile', methods: ['GET'], pattern: /^\/api\/users\/\d+\/profile(?:\?.*)?$/, timeoutMs: 8000, cacheTtlMs: 15_000 },
      { name: 'user.profile.batch', methods: ['GET'], pattern: /^\/api\/users\/profiles(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 10_000 },
      { name: 'user.recentContacts', methods: ['GET'], pattern: /^\/api\/users\/\d+\/recent-contacts(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 5_000 },
      { name: 'contact.friends', methods: ['GET'], pattern: /^\/api\/contacts\/friends\/\d+(?:\?.*)?$/, timeoutMs: 8000, cacheTtlMs: 8_000 },
      { name: 'contact.search', methods: ['GET'], pattern: /^\/api\/contacts\/search(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 3_000 },
      { name: 'conversation.list', methods: ['GET'], pattern: /^\/api\/conversations\/users\/\d+(?:\?.*)?$/, timeoutMs: 8000, cacheTtlMs: 2_000 },
      { name: 'conversation.markRead', methods: ['POST'], pattern: /^\/api\/conversations\/read$/, timeoutMs: 2000 },
      { name: 'message.list', methods: ['GET'], pattern: /^\/api\/messages\/conversations\/[^?]+(?:\?.*)?$/, timeoutMs: 8000 },
      { name: 'cashier.view', methods: ['GET'], pattern: /^\/api\/cashier\/users\/\d+\/payment-tools(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 3_000 },
      { name: 'cashier.pricingPreview', methods: ['GET'], pattern: /^\/api\/cashier\/users\/\d+\/pricing-preview(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 2_000 },
      { name: 'credit.account', methods: ['GET'], pattern: /^\/api\/account-query\/credit\/users\/\d+(?:\?.*)?$/, timeoutMs: 2500 },
      {
        name: 'credit.currentBill',
        methods: ['GET'],
        pattern: /^\/api\/account-query\/credit\/users\/\d+\/current-bill-detail(?:\?.*)?$/,
        timeoutMs: 3000,
      },
      {
        name: 'credit.nextBill',
        methods: ['GET'],
        pattern: /^\/api\/account-query\/credit\/users\/\d+\/next-bill-detail(?:\?.*)?$/,
        timeoutMs: 3000,
        cacheTtlMs: 5_000,
      },
      { name: 'loan.account', methods: ['GET'], pattern: /^\/api\/account-query\/loan\/users\/\d+(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 5_000 },
      { name: 'fund.account', methods: ['GET'], pattern: /^\/api\/account-query\/fund\/users\/\d+(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 5_000 },
      { name: 'fund.create', methods: ['POST'], pattern: /^\/api\/fund-accounts$/, timeoutMs: 3000 },
      { name: 'fund.subscribe', methods: ['POST'], pattern: /^\/api\/fund-accounts\/subscribe$/, timeoutMs: 4000 },
      { name: 'fund.subscribeConfirm', methods: ['POST'], pattern: /^\/api\/fund-accounts\/subscribe\/confirm$/, timeoutMs: 3500 },
      { name: 'fund.fastRedeem', methods: ['POST'], pattern: /^\/api\/fund-accounts\/fast-redeem$/, timeoutMs: 4000 },
      { name: 'contact.applyRequest', methods: ['POST'], pattern: /^\/api\/contacts\/requests$/, timeoutMs: 3000 },
      { name: 'message.text', methods: ['POST'], pattern: /^\/api\/messages\/text$/, timeoutMs: 3000 },
      { name: 'message.image', methods: ['POST'], pattern: /^\/api\/messages\/image$/, timeoutMs: 3500 },
      { name: 'message.transfer', methods: ['POST'], pattern: /^\/api\/messages\/transfer$/, timeoutMs: 5000 },
      { name: 'message.redPacket', methods: ['POST'], pattern: /^\/api\/messages\/red-packet$/, timeoutMs: 5000 },
      { name: 'message.redPacketHistory', methods: ['GET'], pattern: /^\/api\/messages\/red-packets\/history(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 3_000 },
      { name: 'message.redPacketDetail', methods: ['GET'], pattern: /^\/api\/messages\/red-packets\/[^/]+(?:\?.*)?$/, timeoutMs: 3000 },
      { name: 'message.redPacketClaim', methods: ['POST'], pattern: /^\/api\/messages\/red-packets\/[^/]+\/claim$/, timeoutMs: 5000 },
      { name: 'trade.transfer', methods: ['POST'], pattern: /^\/api\/trade\/transfer$/, timeoutMs: 6000 },
      { name: 'trade.deposit', methods: ['POST'], pattern: /^\/api\/trade\/deposit$/, timeoutMs: 8000 },
      { name: 'trade.withdraw', methods: ['POST'], pattern: /^\/api\/trade\/withdraw$/, timeoutMs: 8000 },
      { name: 'trade.pay', methods: ['POST'], pattern: /^\/api\/trade\/pay$/, timeoutMs: 8000 },
      { name: 'media.upload', methods: ['POST'], pattern: /^\/api\/media\/images\/upload(?:\?.*)?$/, timeoutMs: 15000 },
      { name: 'media.metadata', methods: ['GET'], pattern: /^\/api\/media\/[^/]+$/, timeoutMs: 3000, cacheTtlMs: 15_000 },
      { name: 'media.ownerList', methods: ['GET'], pattern: /^\/api\/media\/owners\/\d+(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 10_000 },
      { name: 'media.content', methods: ['GET'], pattern: /^\/api\/media\/[^/]+\/content$/, timeoutMs: 15000 },
      { name: 'deliver.query', methods: ['GET'], pattern: /^\/api\/deliver(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 2_000 },
      { name: 'deliver.event', methods: ['POST'], pattern: /^\/api\/deliver\/events$/, timeoutMs: 2500 },
      { name: 'feedback.submit', methods: ['POST'], pattern: /^\/api\/feedback\/tickets$/, timeoutMs: 3000 },
      { name: 'app.versionCheck', methods: ['GET'], pattern: /^\/api\/apps\/[^/]+\/versions\/check(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 10_000 },
      { name: 'app.deviceUpsert', methods: ['POST'], pattern: /^\/api\/apps\/devices$/, timeoutMs: 3000 },
      { name: 'app.visitRecord', methods: ['POST'], pattern: /^\/api\/apps\/visit-records$/, timeoutMs: 3000 },
      { name: 'app.behaviorEvent', methods: ['POST'], pattern: /^\/api\/apps\/behavior-events$/, timeoutMs: 3000 },
      { name: 'asset.overview', methods: ['GET'], pattern: /^\/api\/assets\/users\/\d+\/overview(?:\?.*)?$/, timeoutMs: 2500, cacheTtlMs: 2_000 },
      { name: 'asset.changes', methods: ['GET'], pattern: /^\/api\/trade\/users\/\d+\/recent-wallet-flows(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 0 },
      { name: 'asset.bills', methods: ['GET'], pattern: /^\/api\/bill\/users\/\d+\/entries(?:\/page)?(?:\?.*)?$/, timeoutMs: 3000, cacheTtlMs: 0 },
      { name: 'default', pattern: /^\/api\/.*/, timeoutMs: this.defaultTimeoutMs },
    ];
  }

  private resolveRoutePolicy(path: string, method: string): BackendRoutePolicy {
    const normalizedPath = path.trim();
    return (
      this.routePolicies.find((policy) => {
        const matchesMethod = !policy.methods || policy.methods.includes(method);
        return matchesMethod && policy.pattern.test(normalizedPath);
      }) ?? {
        name: 'default',
        pattern: /^.*$/,
        timeoutMs: this.defaultTimeoutMs,
      }
    );
  }

  private resolveMetric(routeName: string): BackendRouteMetric {
    const existing = this.metrics.get(routeName);
    if (existing) {
      return existing;
    }
    const created: BackendRouteMetric = {
      route: routeName,
      requestCount: 0,
      successCount: 0,
      errorCount: 0,
      cacheHitCount: 0,
      totalLatencyMs: 0,
      lastLatencyMs: 0,
      lastStatusCode: null,
      lastRequestAt: null,
    };
    this.metrics.set(routeName, created);
    return created;
  }

  private resolveCacheKey(path: string, method: string, policy: BackendRoutePolicy): string | null {
    if (method !== 'GET' || !policy.cacheTtlMs || policy.cacheTtlMs <= 0) {
      return null;
    }
    return `${method}:${path}`;
  }

  private getCachedResponse(cacheKey: string): AxiosResponse<unknown> | null {
    const cached = this.responseCache.get(cacheKey);
    if (!cached) {
      return null;
    }
    if (cached.expiresAt <= Date.now()) {
      this.responseCache.delete(cacheKey);
      return null;
    }
    return this.cloneResponse(cached.response);
  }

  private pruneExpiredCache(): void {
    const now = Date.now();
    Array.from(this.responseCache.entries()).forEach(([cacheKey, cached]) => {
      if (cached.expiresAt <= now) {
        this.responseCache.delete(cacheKey);
      }
    });
  }

  private cloneResponse<T = unknown>(response: AxiosResponse<T>): AxiosResponse<T> {
    return {
      ...response,
      data: this.cloneValue(response.data),
      headers: this.cloneValue(response.headers),
    };
  }

  private cloneValue<T>(value: T): T {
    if (value === null || value === undefined) {
      return value;
    }
    if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
      return value;
    }
    try {
      return JSON.parse(JSON.stringify(value)) as T;
    } catch {
      return value;
    }
  }

  private async performRequestAttempt<T>(
    config: AxiosRequestConfig,
    useRetryTransport = false,
  ): Promise<AxiosResponse<T>> {
    const retryHeaders = useRetryTransport
      ? {
          ...config.headers,
          Connection: 'close',
        }
      : config.headers;
    return firstValueFrom(
      this.httpService.request<T>({
        ...config,
        headers: retryHeaders,
        httpAgent: useRetryTransport ? this.retryHttpAgent : this.httpAgent,
        httpsAgent: useRetryTransport ? this.retryHttpsAgent : this.httpsAgent,
      }),
    );
  }

  private recordSuccess(metric: BackendRouteMetric, latencyMs: number, statusCode: number): void {
    metric.successCount += 1;
    metric.totalLatencyMs += latencyMs;
    metric.lastLatencyMs = latencyMs;
    metric.lastStatusCode = statusCode;
  }

  private recordError(metric: BackendRouteMetric, latencyMs: number, statusCode: number | null): void {
    metric.errorCount += 1;
    metric.totalLatencyMs += latencyMs;
    metric.lastLatencyMs = latencyMs;
    metric.lastStatusCode = statusCode;
  }

  private resolveStatusCode(error: unknown): number | null {
    if (error && typeof error === 'object' && 'response' in error) {
      const response = (error as { response?: { status?: number } }).response;
      return typeof response?.status === 'number' ? response.status : null;
    }
    return null;
  }

  private isUnauthorizedStatus(error: unknown): boolean {
    return this.resolveStatusCode(error) === 401;
  }

  private shouldRetryRequest(error: unknown, method: string): boolean {
    if (method !== 'GET' && method !== 'HEAD') {
      return false;
    }
    if (!error || typeof error !== 'object') {
      return false;
    }
    const networkCode = 'code' in error ? (error as { code?: string }).code : undefined;
    if (!networkCode) {
      return false;
    }
    return ['ECONNRESET', 'ECONNREFUSED', 'ECONNABORTED', 'EPIPE', 'ETIMEDOUT'].includes(networkCode);
  }

  private resolveErrorStack(error: unknown): string | undefined {
    if (error instanceof Error) {
      return error.stack;
    }
    return String(error);
  }

  private resolveDefaultTimeoutMs(): number {
    const fallback = 10_000;
    const raw = process.env.BACKEND_HTTP_TIMEOUT_MS;
    if (!raw) {
      return fallback;
    }
    const parsed = Number(raw);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
  }

  private resolveRequestTimeoutMs(requestTimeoutMs: number | undefined, routeTimeoutMs: number | undefined, method: string): number {
    const configuredTimeoutMs = requestTimeoutMs ?? routeTimeoutMs ?? this.defaultTimeoutMs;
    if (method === 'GET' || method === 'HEAD') {
      return Math.max(this.queryMinimumTimeoutMs, configuredTimeoutMs);
    }
    return configuredTimeoutMs;
  }
}
