import { ArgumentsHost, HttpException, HttpStatus } from '@nestjs/common';
import { AxiosError } from 'axios';
import { GlobalErrorFilter } from './error.filter';

describe('GlobalErrorFilter', () => {
  const filter = new GlobalErrorFilter();

  function createHost(response: { status: jest.Mock; json: jest.Mock }): ArgumentsHost {
    return {
      switchToHttp: () => ({
        getRequest: () => ({
          requestId: 'req-001',
          originalUrl: '/bff/page-init?page=home&uid=880100068483692100',
        }),
        getResponse: () => response,
      }),
    } as ArgumentsHost;
  }

  it('maps HttpException payload into unified error body', () => {
    const json = jest.fn();
    const status = jest.fn(() => ({ json }));
    const response = { status, json };
    const exception = new HttpException(
      {
        code: 'PAGE_PARAM_REQUIRED',
        message: 'page 参数不能为空',
        details: { field: 'page' },
      },
      HttpStatus.BAD_REQUEST,
    );

    filter.catch(exception, createHost(response));

    expect(status).toHaveBeenCalledWith(HttpStatus.BAD_REQUEST);
    expect(json).toHaveBeenCalledWith(
      expect.objectContaining({
        success: false,
        requestId: 'req-001',
        path: '/bff/page-init?page=home&uid=880100068483692100',
        error: {
          code: 'PAGE_PARAM_REQUIRED',
          message: 'page 参数不能为空',
          details: { field: 'page' },
        },
      }),
    );
  });

  it('maps AxiosError into upstream failure payload', () => {
    const json = jest.fn();
    const status = jest.fn(() => ({ json }));
    const response = { status, json };
    const exception = new AxiosError('upstream failed');
    exception.response = {
      status: 502,
      statusText: 'Bad Gateway',
      headers: {},
      config: {
        headers: {} as never,
      },
      data: {
        error: {
          code: 'UPSTREAM_TIMEOUT',
          message: 'timeout',
        },
      },
    };

    filter.catch(exception, createHost(response));

    expect(status).toHaveBeenCalledWith(HttpStatus.BAD_GATEWAY);
    expect(json).toHaveBeenCalledWith(
      expect.objectContaining({
        error: {
          code: 'UPSTREAM_ERROR',
          message: '上游服务调用失败',
          details: {
            upstreamStatus: 502,
          },
        },
      }),
    );
  });
});
