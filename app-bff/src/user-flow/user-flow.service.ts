import { BadGatewayException, BadRequestException, Injectable } from '@nestjs/common';
import { AxiosError } from 'axios';
import { BackendHttpService } from '../common/backend-http.service';

type BackendEnvelope<T> = {
  success?: boolean;
  data?: T;
  error?: {
    code?: string;
    message?: string;
  };
};

type BackendRegisterCheck = {
  userExists?: boolean;
  realNameVerified?: boolean;
  kycLevel?: string;
};

type BackendRegistration = {
  userId?: number | string;
  aipayUid?: string;
  loginId?: string;
  kycSubmitted?: boolean;
};

@Injectable()
export class UserFlowService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async checkRegisterPhone(loginId: string, deviceId?: string): Promise<Record<string, unknown>> {
    try {
      const headers: Record<string, string> = {};
      if (typeof deviceId === 'string' && deviceId.trim().length > 0) {
        headers['X-Device-Id'] = deviceId.trim();
      }
      const response = await this.backendHttpService.get<BackendEnvelope<BackendRegisterCheck>>(
        `/api/user-flows/register/check?loginId=${encodeURIComponent(loginId)}`,
        Object.keys(headers).length > 0 ? { headers } : undefined,
      );
      const payload = response.data?.data;
      return {
        userExists: Boolean(payload?.userExists),
        realNameVerified: Boolean(payload?.realNameVerified),
        kycLevel: typeof payload?.kycLevel === 'string' ? payload.kycLevel.trim() : '',
      };
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '注册手机号校验失败');
    }
  }

  async register(
    payload: Record<string, unknown>,
  ): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendRegistration>, Record<string, unknown>>(
        '/api/user-flows/registrations',
        payload,
      );
      const registration = response.data?.data;
      if (!registration) {
        throw new BadGatewayException({
          code: 'UPSTREAM_EMPTY_RESPONSE',
          message: '上游服务未返回注册结果',
        });
      }
      return {
        userId: registration.userId ?? null,
        aipayUid: typeof registration.aipayUid === 'string' ? registration.aipayUid.trim() : '',
        loginId: typeof registration.loginId === 'string' ? registration.loginId.trim() : '',
        kycSubmitted: Boolean(registration.kycSubmitted),
      };
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '注册账号失败');
    }
  }

  private rethrowMappedUpstreamError(error: unknown, fallbackMessage: string): never {
    if (error instanceof AxiosError) {
      const status = error.response?.status ?? 502;
      const body = error.response?.data as BackendEnvelope<unknown> | undefined;
      const message = body?.error?.message ?? fallbackMessage;
      const code = body?.error?.code ?? 'UPSTREAM_ERROR';
      if (status === 400) {
        throw new BadRequestException({ code, message });
      }
      throw new BadGatewayException({ code, message });
    }
    if (error instanceof BadRequestException || error instanceof BadGatewayException) {
      throw error;
    }
    throw new BadGatewayException({
      code: 'UPSTREAM_ERROR',
      message: fallbackMessage,
    });
  }
}
