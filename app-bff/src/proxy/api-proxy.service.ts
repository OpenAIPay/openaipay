import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { Request, Response } from 'express';
import { firstValueFrom } from 'rxjs';
import type { AxiosResponse } from 'axios';
import { Agent as HttpAgent } from 'http';
import { Agent as HttpsAgent } from 'https';
import { isAllowedProxyPath } from './api-proxy.policy';
import { ATTR_REQUEST_PAYLOAD, ATTR_SCENE, buildRequestPayload, markBizErrorLogged, resolveScene } from '../common/scene-log.support';

@Injectable()
export class ApiProxyService {
  private readonly log = new Logger(ApiProxyService.name);
  private readonly backendBaseUrl = process.env.BACKEND_BASE_URL ?? 'http://127.0.0.1:8080';
  private readonly proxyTimeoutMs = this.resolveProxyTimeoutMs();
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

  constructor(private readonly httpService: HttpService) {}

  async forward(req: Request, res: Response): Promise<void> {
    this.ensureAllowedPath(req.path);
    const scene = resolveScene(req, '接口代理');
    const requestPayload = buildRequestPayload(req);
    req[ATTR_SCENE] = scene;
    req[ATTR_REQUEST_PAYLOAD] = requestPayload;
    this.log.log(`[${scene}]入参：${requestPayload}`);
    const targetUrl = `${this.backendBaseUrl}${req.originalUrl}`;

    const headers: Record<string, string> = {};
    for (const [key, value] of Object.entries(req.headers)) {
      const lower = key.toLowerCase();
      if (this.hopByHopHeaders.has(lower) || value === undefined) {
        continue;
      }
      headers[key] = Array.isArray(value) ? value.join(',') : value;
    }

    const requestData = this.resolveRequestData(req);

    let upstream: AxiosResponse<ArrayBuffer>;
    try {
      upstream = await this.forwardUpstream(req.method, targetUrl, headers, requestData);
    } catch (error) {
      this.log.error(`[${scene}]业务异常, request:${requestPayload}`, error instanceof Error ? error.stack : String(error));
      markBizErrorLogged(req);
      throw error;
    }

    Object.entries(upstream.headers).forEach(([key, value]) => {
      if (!this.hopByHopHeaders.has(key.toLowerCase()) && value !== undefined) {
        res.setHeader(key, value as string);
      }
    });

    res.status(upstream.status).send(Buffer.from(upstream.data));
  }

  private ensureAllowedPath(pathname: string): void {
    if (isAllowedProxyPath(pathname)) {
      return;
    }
    throw new NotFoundException({
      code: 'API_PROXY_ROUTE_NOT_ALLOWED',
      message: '该接口未纳入 app-bff 代理白名单，请改走 /bff/* 聚合接口或直连后端服务',
    });
  }

  private resolveRequestData(req: Request): unknown {
    const method = req.method.toUpperCase();
    if (method === 'GET' || method === 'HEAD') {
      return undefined;
    }

    const contentType = String(req.headers['content-type'] ?? '').toLowerCase();
    if (contentType.includes('multipart/form-data') || contentType.includes('application/octet-stream')) {
      return req;
    }

    return req.body;
  }

  private async forwardUpstream(
    method: string,
    url: string,
    headers: Record<string, string>,
    requestData: unknown,
  ): Promise<AxiosResponse<ArrayBuffer>> {
    return firstValueFrom(
      this.httpService.request<ArrayBuffer>({
        method,
        url,
        headers,
        timeout: this.proxyTimeoutMs,
        httpAgent: this.httpAgent,
        httpsAgent: this.httpsAgent,
        // Keep multipart body as stream to avoid dropping file parts.
        data: requestData,
        maxBodyLength: Infinity,
        maxContentLength: Infinity,
        responseType: 'arraybuffer',
        validateStatus: () => true,
      }),
    );
  }

  private resolveProxyTimeoutMs(): number {
    const fallback = 60000;
    const raw = process.env.API_PROXY_TIMEOUT_MS;
    if (!raw) {
      return fallback;
    }
    const parsed = Number(raw);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
  }
}
