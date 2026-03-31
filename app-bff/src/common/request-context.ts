import { AsyncLocalStorage } from 'node:async_hooks';

type RequestContextState = {
  requestId: string;
  authorizationHeader?: string;
  currentUserId?: string;
};

const requestContextStorage = new AsyncLocalStorage<RequestContextState>();

export function runWithRequestContext<T>(state: RequestContextState, callback: () => T): T {
  return requestContextStorage.run(state, callback);
}

export function getRequestContext(): RequestContextState | undefined {
  return requestContextStorage.getStore();
}

export function getCurrentRequestId(): string | undefined {
  return getRequestContext()?.requestId;
}

export function getCurrentAuthorizationHeader(): string | undefined {
  return getRequestContext()?.authorizationHeader;
}

export function getCurrentUserId(): string | undefined {
  return getRequestContext()?.currentUserId;
}
