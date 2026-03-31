import { Injectable, NestMiddleware } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { NextFunction, Request, Response } from 'express';
import { resolveSubjectIdFromAuthorizationHeader } from './auth-token';
import { runWithRequestContext } from './request-context';

@Injectable()
export class RequestContextMiddleware implements NestMiddleware {
  use(req: Request, res: Response, next: NextFunction): void {
    const incoming = req.header('x-request-id');
    const requestId = incoming && incoming.trim() ? incoming.trim() : randomUUID();
    const incomingAuthorization = req.header('authorization');
    const authorizationHeader = incomingAuthorization && incomingAuthorization.trim()
      ? incomingAuthorization.trim()
      : undefined;
    if (authorizationHeader) {
      req.headers['authorization'] = authorizationHeader;
    }
    const currentUserId = resolveSubjectIdFromAuthorizationHeader(authorizationHeader) ?? undefined;
    req.authenticatedUserId = currentUserId;
    req.requestId = requestId;
    req.headers['x-request-id'] = requestId;
    res.setHeader('x-request-id', requestId);
    runWithRequestContext({ requestId, authorizationHeader, currentUserId }, () => next());
  }
}
