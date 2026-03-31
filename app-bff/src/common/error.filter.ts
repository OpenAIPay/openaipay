import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { AxiosError } from 'axios';
import { Request, Response } from 'express';
import {
  buildRequestPayload,
  isBizErrorLogged,
  markBizErrorLogged,
  resolveScene,
} from './scene-log.support';

@Catch()
export class GlobalErrorFilter implements ExceptionFilter {
  private readonly log = new Logger(GlobalErrorFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const req = ctx.getRequest<Request>();
    const res = ctx.getResponse<Response>();

    const requestId = req.requestId ?? 'n/a';
    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let code = 'INTERNAL_ERROR';
    let message = '服务器开小差，请稍后再试';
    let details: unknown;
    const exposeInternalDetails = (process.env.BFF_EXPOSE_ERROR_DETAILS ?? '').trim().toLowerCase() === 'true';

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const payload = exception.getResponse();
      if (typeof payload === 'string') {
        message = payload;
      } else if (payload && typeof payload === 'object') {
        const body = payload as Record<string, unknown>;
        code = typeof body.code === 'string' ? body.code : `HTTP_${status}`;
        if (typeof body.message === 'string') {
          message = body.message;
        }
        if (body.details !== undefined) {
          details = body.details;
        }
      }
    } else if (exception instanceof AxiosError) {
      status = HttpStatus.BAD_GATEWAY;
      code = 'UPSTREAM_ERROR';
      message = '上游服务调用失败';
      details = {
        upstreamStatus: exception.response?.status,
      };
      if (exposeInternalDetails) {
        details = {
          ...(details as Record<string, unknown>),
        };
      }
    } else if (exception instanceof Error) {
      if (exposeInternalDetails) {
        details = { name: exception.name };
      }
    }

    if (!isBizErrorLogged(req)) {
      const scene = resolveScene(req);
      const requestPayload = buildRequestPayload(req);
      if (exception instanceof Error) {
        this.log.error(`[${scene}]业务异常, request:${requestPayload}`, exception.stack);
      } else {
        this.log.error(`[${scene}]业务异常, request:${requestPayload}, exception:${String(exception)}`);
      }
      markBizErrorLogged(req);
    }

    res.status(status).json({
      success: false,
      requestId,
      error: {
        code,
        message,
        details,
      },
      path: req.originalUrl,
      timestamp: new Date().toISOString(),
    });
  }
}
