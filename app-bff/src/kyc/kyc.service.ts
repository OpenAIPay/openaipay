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

type BackendKycStatus = {
  userId?: string | number;
  kycLevel?: string;
  realNameVerified?: boolean;
  maskedRealName?: string;
  idCardNoMasked?: string;
};

@Injectable()
export class KycService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async getStatus(userId: string): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendKycStatus>>(
        `/api/kyc/users/${userId}/status`,
      );
      return this.normalizeStatus(response.data?.data);
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '查询实名状态失败');
    }
  }

  async submit(userId: string, payload: Record<string, unknown>): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<BackendKycStatus>, Record<string, unknown>>(
        `/api/kyc/users/${userId}/submissions`,
        payload,
      );
      return this.normalizeStatus(response.data?.data);
    } catch (error) {
      this.rethrowMappedUpstreamError(error, '提交实名认证失败');
    }
  }

  private normalizeStatus(raw?: BackendKycStatus): Record<string, unknown> {
    return {
      userId: raw?.userId ?? null,
      kycLevel: typeof raw?.kycLevel === 'string' ? raw.kycLevel.trim() : '',
      realNameVerified: Boolean(raw?.realNameVerified),
      maskedRealName: typeof raw?.maskedRealName === 'string' ? raw.maskedRealName.trim() : '',
      idCardNoMasked: typeof raw?.idCardNoMasked === 'string' ? raw.idCardNoMasked.trim() : '',
    };
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
