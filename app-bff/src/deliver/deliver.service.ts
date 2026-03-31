import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

@Injectable()
export class DeliverService {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  async queryDeliver(query: {
    positionCodeList: string;
    clientId: string;
    sceneCode: string;
    channel: string;
    userId?: string;
  }): Promise<unknown> {
    const queryItems = [
      `positionCodeList=${encodeURIComponent(query.positionCodeList.trim())}`,
      `clientId=${encodeURIComponent(query.clientId.trim())}`,
      `sceneCode=${encodeURIComponent(query.sceneCode.trim())}`,
      `channel=${encodeURIComponent(query.channel.trim())}`,
      query.userId?.trim() ? `userId=${encodeURIComponent(query.userId.trim())}` : '',
    ].filter((item) => item.length > 0);
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<unknown>>(`/api/deliver?${queryItems.join('&')}`);
      return unwrapBackendData(response, '上游服务未返回投放结果');
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询投放数据失败');
    }
  }

  async reportEvent(payload: Record<string, unknown>): Promise<unknown> {
    this.assertRequiredString(payload.clientId, 'clientId');
    this.assertRequiredString(payload.sceneCode, 'sceneCode');
    this.assertRequiredString(payload.channel, 'channel');
    this.assertRequiredString(payload.positionCode, 'positionCode');
    this.assertRequiredString(payload.eventType, 'eventType');
    try {
      const response = await this.backendHttpService.post<BackendEnvelope<unknown>, Record<string, unknown>>(
        '/api/deliver/events',
        payload,
      );
      return unwrapBackendData(response, '上游服务未返回投放事件结果');
    } catch (error) {
      rethrowMappedUpstreamError(error, '投放事件上报失败');
    }
  }

  private assertRequiredString(value: unknown, field: string): void {
    if (typeof value === 'string' && value.trim().length > 0) {
      return;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `${field} 不能为空`,
    });
  }
}
