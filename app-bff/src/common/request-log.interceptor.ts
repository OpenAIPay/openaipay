import { CallHandler, ExecutionContext, Injectable, Logger, NestInterceptor } from '@nestjs/common';
import type { Request } from 'express';
import type { Observable } from 'rxjs';
import { ATTR_REQUEST_PAYLOAD, ATTR_SCENE, buildRequestPayload, resolveScene } from './scene-log.support';

@Injectable()
export class RequestLogInterceptor implements NestInterceptor {
  private readonly log = new Logger(RequestLogInterceptor.name);

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    if (context.getType() !== 'http') {
      return next.handle();
    }

    const request = context.switchToHttp().getRequest<Request>();
    const scene = resolveScene(request);
    const requestPayload = buildRequestPayload(request);
    request[ATTR_SCENE] = scene;
    request[ATTR_REQUEST_PAYLOAD] = requestPayload;

    this.log.log(`[${scene}]入参：${requestPayload}`);
    return next.handle();
  }
}
