import 'express';

declare module 'express-serve-static-core' {
  interface Request {
    requestId?: string;
    authenticatedUserId?: string;
    aipayBffRequestLogScene?: string;
    aipayBffRequestPayload?: string;
    aipayBffBizErrorLogged?: boolean;
  }
}
