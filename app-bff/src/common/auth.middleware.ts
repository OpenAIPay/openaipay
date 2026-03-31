import { ForbiddenException, Injectable, NestMiddleware, UnauthorizedException } from '@nestjs/common';
import type { NextFunction, Request, Response } from 'express';
import { resolveSubjectIdFromAuthorizationHeader } from './auth-token';

const ASSERTED_USER_ID_KEYS = new Set([
  'userId',
  'uid',
  'ownerUserId',
  'targetUserId',
  'requesterUserId',
  'operatorUserId',
  'senderUserId',
  'payerUserId',
]);

@Injectable()
export class AuthMiddleware implements NestMiddleware {
  use(req: Request, _res: Response, next: NextFunction): void {
    const requestPath = this.resolveRequestPath(req);
    if (this.isPublicRequest(req, requestPath)) {
      next();
      return;
    }
    if (!this.requiresAuthentication(requestPath)) {
      next();
      return;
    }

    const userId = resolveSubjectIdFromAuthorizationHeader(req.header('authorization'));
    if (!userId) {
      throw new UnauthorizedException({
        code: 'UNAUTHORIZED',
        message: '用户鉴权失败：缺少有效 Bearer Token',
      });
    }
    req.authenticatedUserId = userId;

    if (!this.shouldSkipAssertedUserValidation(requestPath)) {
      this.validateAssertedUserIds(req, userId, requestPath);
    }
    next();
  }

  private resolveRequestPath(req: Request): string {
    const candidate = [req.path, req.url, req.originalUrl]
      .find((value) => typeof value === 'string' && value.trim().length > 0)
      ?.trim();
    if (!candidate) {
      return '/';
    }
    return this.normalizePath(candidate);
  }

  private normalizePath(path: string): string {
    const queryIndex = path.indexOf('?');
    let normalized = queryIndex >= 0 ? path.slice(0, queryIndex) : path;
    normalized = normalized.replace(/\/{2,}/g, '/');
    if (normalized.length > 1 && normalized.endsWith('/')) {
      normalized = normalized.slice(0, -1);
    }
    return normalized || '/';
  }

  private requiresAuthentication(path: string): boolean {
    return path.startsWith('/bff/') || path.startsWith('/api/');
  }

  private isPublicRequest(req: Request, path: string): boolean {
    if (req.method.toUpperCase() === 'OPTIONS') {
      return true;
    }
    if (path === '/' || path.startsWith('/health') || path.startsWith('/demo-avatar/')) {
      return true;
    }
    if (path === '/bff/auth/mobile-verify-login') {
      return true;
    }
    if (path === '/bff/auth/demo-auto-login') {
      return true;
    }
    if (path === '/bff/auth/preset-login-accounts') {
      return true;
    }
    if (path === '/bff/page-init') {
      return true;
    }
    if (path === '/bff/users/profile-by-login') {
      return true;
    }
    if (path === '/bff/user-flows/register/check' || path === '/bff/user-flows/registrations') {
      return true;
    }
    if (/^\/bff\/apps\/[^/]+\/versions\/check$/.test(path)) {
      return true;
    }
    if (path === '/bff/apps/devices' || path === '/bff/apps/visit-records' || path === '/bff/apps/behavior-events') {
      return true;
    }
    if (req.method.toUpperCase() === 'GET' && this.isPublicMediaContentPath(path)) {
      return true;
    }
    if (path === '/api/auth/mobile-verify-login') {
      return true;
    }
    if (path === '/api/auth/preset-login-accounts') {
      return true;
    }
    if (path === '/api/page-init') {
      return true;
    }
    if (path === '/api/users/profile-by-login') {
      return true;
    }
    if (path === '/api/user-flows/register/check' || path === '/api/user-flows/registrations') {
      return true;
    }
    if (req.method.toUpperCase() === 'GET' && /^\/api\/media\/[^/]+\/content$/.test(path)) {
      return true;
    }
    return false;
  }

  private isPublicMediaContentPath(path: string): boolean {
    return /^\/bff\/media\/[^/]+\/content$/.test(path);
  }

  private shouldSkipAssertedUserValidation(path: string): boolean {
    return path === '/bff/coupons/mobile-topup-reward/available'
      || path === '/bff/coupons/mobile-topup-reward/claim';
  }

  private validateAssertedUserIds(req: Request, currentUserId: string, requestPath: string): void {
    const ignoredAssertedUserIdKeys = this.resolveIgnoredAssertedUserIdKeys(requestPath, req.method);
    const assertedValues = [
      ...this.collectUserIdsFromPath(requestPath),
      ...this.collectUserIdsFromQuery(req.query, ignoredAssertedUserIdKeys),
      ...this.collectUserIdsFromBody(req.body, ignoredAssertedUserIdKeys),
    ];
    assertedValues.forEach((asserted) => {
      if (asserted !== currentUserId) {
        throw new ForbiddenException({
          code: 'FORBIDDEN',
          message: '用户鉴权失败：请求用户与令牌不匹配',
        });
      }
    });
  }

  private resolveIgnoredAssertedUserIdKeys(path: string, method: string): Set<string> {
    const normalizedMethod = method.toUpperCase();
    if (
      normalizedMethod === 'POST'
      && (path === '/bff/contacts/requests' || path === '/api/contacts/requests')
    ) {
      return new Set(['targetUserId']);
    }
    return new Set<string>();
  }

  private collectUserIdsFromPath(path: string): string[] {
    const patterns = [
      /\/bff\/users\/(\d+)\b/,
      /\/bff\/accounts\/(?:credit|loan|fund)\/users\/(\d+)\b/,
      /\/bff\/assets\/(\d+)\b/,
      /\/bff\/bills\/users\/(\d+)\b/,
      /\/bff\/cashier\/users\/(\d+)\b/,
      /\/bff\/kyc\/users\/(\d+)\b/,
      /\/bff\/messages\/users\/(\d+)\b/,
      /\/bff\/media\/owners\/(\d+)\b/,
      /\/api\/users\/(\d+)\b/,
      /\/api\/trade\/users\/(\d+)\b/,
      /\/api\/assets\/users\/(\d+)\b/,
      /\/api\/cashier\/users\/(\d+)\b/,
      /\/api\/account-query\/credit\/users\/(\d+)\b/,
      /\/api\/account-query\/loan\/users\/(\d+)\b/,
      /\/api\/account-query\/fund\/users\/(\d+)\b/,
      /\/api\/account-query\/wallet\/users\/(\d+)\b/,
      /\/api\/credit-accounts\/users\/(\d+)\b/,
      /\/api\/loan-accounts\/users\/(\d+)\b/,
      /\/api\/fund-accounts\/(\d+)\b/,
      /\/api\/bankcards\/users\/(\d+)\b/,
      /\/api\/contacts\/friends\/(\d+)\b/,
      /\/api\/conversations\/users\/(\d+)\b/,
      /\/api\/kyc\/users\/(\d+)\b/,
      /\/api\/feedback\/users\/(\d+)\b/,
      /\/api\/media\/owners\/(\d+)\b/,
    ];
    const values: string[] = [];
    patterns.forEach((pattern) => {
      const matched = pattern.exec(path);
      if (matched && matched[1]) {
        values.push(matched[1]);
      }
    });
    return values;
  }

  private collectUserIdsFromQuery(query: Request['query'], ignoredKeys: Set<string>): string[] {
    const values: string[] = [];
    Object.entries(query ?? {}).forEach(([key, rawValue]) => {
      if (!ASSERTED_USER_ID_KEYS.has(key) || ignoredKeys.has(key)) {
        return;
      }
      this.flattenValues(rawValue).forEach((item) => {
        const normalized = this.normalizePositiveLong(item);
        if (!normalized) {
          throw new UnauthorizedException({
            code: 'UNAUTHORIZED',
            message: '用户鉴权失败：用户标识格式错误',
          });
        }
        values.push(normalized);
      });
    });
    return values;
  }

  private collectUserIdsFromBody(body: unknown, ignoredKeys: Set<string>): string[] {
    const values: string[] = [];
    if (!body || typeof body !== 'object' || Array.isArray(body)) {
      return values;
    }
    Object.entries(body as Record<string, unknown>).forEach(([key, rawValue]) => {
      if (!ASSERTED_USER_ID_KEYS.has(key) || ignoredKeys.has(key)) {
        return;
      }
      const normalized = this.normalizePositiveLong(rawValue);
      if (!normalized) {
        throw new UnauthorizedException({
          code: 'UNAUTHORIZED',
          message: '用户鉴权失败：用户标识格式错误',
        });
      }
      values.push(normalized);
    });
    return values;
  }

  private flattenValues(rawValue: unknown): string[] {
    if (rawValue === undefined || rawValue === null) {
      return [];
    }
    if (Array.isArray(rawValue)) {
      return rawValue.flatMap((item) => this.flattenValues(item));
    }
    if (typeof rawValue === 'object') {
      return [];
    }
    return [String(rawValue)];
  }

  private normalizePositiveLong(rawValue: unknown): string | null {
    if (typeof rawValue === 'number') {
      if (!Number.isSafeInteger(rawValue) || rawValue <= 0) {
        return null;
      }
      return String(Math.trunc(rawValue));
    }
    if (typeof rawValue !== 'string') {
      return null;
    }
    const normalized = rawValue.trim();
    if (!/^\d{1,20}$/.test(normalized)) {
      return null;
    }
    return normalized;
  }
}
