import { BadGatewayException, BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import type { AxiosResponseHeaders } from 'axios';
import type { Request, Response } from 'express';
import { BackendHttpService } from '../common/backend-http.service';
import { BackendEnvelope, rethrowMappedUpstreamError, unwrapBackendData } from '../common/upstream';

type BackendMediaAsset = {
  mediaId?: string;
  ownerUserId?: string | number;
  mediaType?: string;
  originalName?: string;
  mimeType?: string;
  sizeBytes?: string | number;
  compressedSizeBytes?: string | number;
  width?: string | number;
  height?: string | number;
  contentUrl?: string;
  createdAt?: string;
};

@Injectable()
export class MediaService {
  private readonly hopByHopHeaders = new Set([
    'connection',
    'keep-alive',
    'proxy-authenticate',
    'proxy-authorization',
    'te',
    'trailer',
    'transfer-encoding',
    'upgrade',
    'content-encoding',
    'content-length',
    'host',
  ]);

  constructor(private readonly backendHttpService: BackendHttpService) {}

  async uploadImage(req: Request, ownerUserId: string): Promise<Record<string, unknown>> {
    const upstream = await this.backendHttpService.request<BackendEnvelope<BackendMediaAsset>, Request>({
      method: 'POST',
      path: `/api/media/images/upload?ownerUserId=${encodeURIComponent(ownerUserId)}`,
      headers: this.forwardHeaders(req),
      data: req,
      timeoutMs: 15_000,
      maxBodyLength: Infinity,
      maxContentLength: Infinity,
      validateStatus: () => true,
    });

    if (upstream.status >= 200 && upstream.status < 300) {
      return this.normalizeMediaAsset(unwrapBackendData(upstream, '上游服务未返回媒体上传结果'));
    }

    this.throwMappedResponseError(upstream.status, upstream.data, '上传图片失败');
  }

  async getMedia(mediaId: string): Promise<Record<string, unknown>> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendMediaAsset>>(
        `/api/media/${encodeURIComponent(mediaId)}`,
      );
      return this.normalizeMediaAsset(unwrapBackendData(response, '上游服务未返回媒体资源'));
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询媒体资源失败');
    }
  }

  async listOwnerMedia(ownerUserId: string, limit: number): Promise<Record<string, unknown>[]> {
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendMediaAsset[]>>(
        `/api/media/owners/${encodeURIComponent(ownerUserId)}?limit=${limit}`,
      );
      const items = unwrapBackendData(response, '上游服务未返回媒体列表');
      return Array.isArray(items) ? items.map((item) => this.normalizeMediaAsset(item)) : [];
    } catch (error) {
      rethrowMappedUpstreamError(error, '查询媒体列表失败');
    }
  }

  async loadContent(mediaId: string, res: Response): Promise<void> {
    const upstream = await this.backendHttpService.request<ArrayBuffer>({
      method: 'GET',
      path: `/api/media/${encodeURIComponent(mediaId)}/content`,
      timeoutMs: 15_000,
      responseType: 'arraybuffer',
      validateStatus: () => true,
    });

    if (upstream.status < 200 || upstream.status >= 300) {
      const envelope = this.parseEnvelopeFromBinary(upstream.data);
      this.throwMappedResponseError(upstream.status, envelope, '加载媒体内容失败');
    }

    this.copyHeaders(res, upstream.headers);
    res.status(upstream.status).send(Buffer.from(upstream.data));
  }

  private normalizeMediaAsset(raw: BackendMediaAsset): Record<string, unknown> {
    const mediaId = typeof raw.mediaId === 'string' ? raw.mediaId.trim() : '';
    const contentUrl = mediaId.length > 0 ? `/bff/media/${encodeURIComponent(mediaId)}/content` : '';
    return {
      mediaId,
      ownerUserId: this.normalizeOptionalScalar(raw.ownerUserId),
      mediaType: this.normalizeOptionalText(raw.mediaType),
      originalName: this.normalizeOptionalText(raw.originalName),
      mimeType: this.normalizeOptionalText(raw.mimeType),
      sizeBytes: this.normalizeOptionalScalar(raw.sizeBytes),
      compressedSizeBytes: this.normalizeOptionalScalar(raw.compressedSizeBytes),
      width: this.normalizeOptionalScalar(raw.width),
      height: this.normalizeOptionalScalar(raw.height),
      contentUrl,
      createdAt: this.normalizeOptionalText(raw.createdAt),
    };
  }

  private normalizeOptionalText(value: unknown): string {
    if (typeof value === 'string') {
      return value.trim();
    }
    return '';
  }

  private normalizeOptionalScalar(value: unknown): string | number | null {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value;
    }
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
    return null;
  }

  private forwardHeaders(req: Request): Record<string, string> {
    const headers: Record<string, string> = {};
    Object.entries(req.headers).forEach(([key, value]) => {
      const normalizedKey = key.toLowerCase();
      if (this.hopByHopHeaders.has(normalizedKey) || value === undefined) {
        return;
      }
      headers[key] = Array.isArray(value) ? value.join(',') : value;
    });
    return headers;
  }

  private copyHeaders(res: Response, headers: AxiosResponseHeaders | Record<string, unknown>): void {
    Object.entries(headers).forEach(([key, value]) => {
      const normalizedKey = key.toLowerCase();
      if (this.hopByHopHeaders.has(normalizedKey) || value === undefined) {
        return;
      }
      res.setHeader(key, Array.isArray(value) ? value.join(',') : String(value));
    });
  }

  private parseEnvelopeFromBinary(data: unknown): BackendEnvelope<unknown> | undefined {
    const text = Buffer.from(data as ArrayBuffer).toString('utf8').trim();
    if (!text) {
      return undefined;
    }
    try {
      return JSON.parse(text) as BackendEnvelope<unknown>;
    } catch {
      return undefined;
    }
  }

  private throwMappedResponseError(status: number, body: BackendEnvelope<unknown> | undefined, fallbackMessage: string): never {
    const message = body?.error?.message ?? fallbackMessage;
    const code = body?.error?.code ?? 'UPSTREAM_ERROR';
    if (status === 400) {
      throw new BadRequestException({ code, message });
    }
    if (status === 404) {
      throw new NotFoundException({ code, message });
    }
    throw new BadGatewayException({ code, message });
  }
}
