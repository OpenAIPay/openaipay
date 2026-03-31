import { BadGatewayException, BadRequestException, NotFoundException, ServiceUnavailableException } from '@nestjs/common';
import { AxiosError } from 'axios';

export type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: {
    code?: string;
    message?: string;
  };
};

const databaseErrorPatterns = [
  /cannotgetjdbcconnectionexception/i,
  /failed to obtain jdbc connection/i,
  /error querying database/i,
  /communications link failure/i,
  /the last packet successfully received from the server/i,
  /jdbc connection/i,
  /sqlnontransientconnectionexception/i,
  /hikaripool/i,
];

const sensitiveErrorPatterns = [
  /###\s+error querying database/i,
  /org\.springframework\./i,
  /cn\/openaipay\/.*mapper/i,
  /exception/i,
  /stack trace/i,
];

function normalizeUpstreamMessage(message: unknown): string {
  return typeof message === 'string' ? message.trim() : '';
}

function isDatabaseUnavailableMessage(message: string): boolean {
  return databaseErrorPatterns.some((pattern) => pattern.test(message));
}

function isSensitiveInternalMessage(message: string): boolean {
  return sensitiveErrorPatterns.some((pattern) => pattern.test(message));
}

function resolvePublicErrorCode(rawCode: unknown, message: string): string {
  if (typeof rawCode === 'string' && rawCode.trim().length > 0) {
    return rawCode.trim();
  }
  if (isDatabaseUnavailableMessage(message)) {
    return 'DATABASE_UNAVAILABLE';
  }
  return 'UPSTREAM_ERROR';
}

function resolvePublicErrorMessage(rawMessage: unknown, fallbackMessage: string): string {
  const normalized = normalizeUpstreamMessage(rawMessage);
  if (!normalized) {
    return fallbackMessage;
  }
  if (isDatabaseUnavailableMessage(normalized)) {
    return '服务暂不可用，请稍后重试';
  }
  if (isSensitiveInternalMessage(normalized)) {
    return fallbackMessage;
  }
  return normalized;
}

export function unwrapBackendData<T>(
  response: { data?: BackendEnvelope<T> | undefined },
  emptyMessage: string,
): T {
  const payload = response.data?.data;
  if (payload === undefined || payload === null) {
    throw new BadGatewayException({
      code: 'UPSTREAM_EMPTY_RESPONSE',
      message: emptyMessage,
    });
  }
  return payload;
}

export function rethrowMappedUpstreamError(error: unknown, fallbackMessage: string): never {
  if (error instanceof AxiosError) {
    const status = error.response?.status ?? 502;
    const body = error.response?.data as BackendEnvelope<unknown> | undefined;
    const code = resolvePublicErrorCode(body?.error?.code, normalizeUpstreamMessage(body?.error?.message));
    const message = resolvePublicErrorMessage(body?.error?.message, fallbackMessage);
    const shouldUseServiceUnavailable = code === 'DATABASE_UNAVAILABLE' || status === 503;

    if (status === 400) {
      throw new BadRequestException({ code, message });
    }
    if (status === 404) {
      throw new NotFoundException({ code, message });
    }
    if (shouldUseServiceUnavailable) {
      throw new ServiceUnavailableException({ code, message });
    }
    throw new BadGatewayException({ code, message });
  }
  if (
    error instanceof BadRequestException ||
    error instanceof NotFoundException ||
    error instanceof BadGatewayException ||
    error instanceof ServiceUnavailableException
  ) {
    throw error;
  }
  throw new BadGatewayException({
    code: 'UPSTREAM_ERROR',
    message: fallbackMessage,
  });
}
