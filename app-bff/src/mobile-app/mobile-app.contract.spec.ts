import { BackendHttpService } from '../common/backend-http.service';
import { MobileAppService } from './mobile-app.service';

describe('MobileAppServiceContract', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
      post: jest.fn(),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new MobileAppService(backendHttpService);
    return { service, backendHttpService };
  };

  it('sanitizes record visit payload before forwarding', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.post.mockResolvedValue({
      data: {
        success: true,
        data: {
          deviceId: 'ios-debug-device',
          appCode: 'openaipay',
          clientType: 'IOS_APP',
          apiName: 'api',
        },
      },
    });

    const extraLongApiName = 'a'.repeat(1200);
    const extraLongSummary = 'b'.repeat(1500);

    await service.recordVisit({
      deviceId: ' ios-debug-device ',
      appCode: ' openaipay ',
      apiName: extraLongApiName,
      resultSummary: extraLongSummary,
      durationMs: -100,
      unknownField: 'should-not-pass-through',
    });

    const [, requestPayload] = backendHttpService.post.mock.calls[0];
    expect((requestPayload as Record<string, unknown>).deviceId).toBe('ios-debug-device');
    expect((requestPayload as Record<string, unknown>).appCode).toBe('openaipay');
    expect((requestPayload as Record<string, unknown>).apiName).toBe('a'.repeat(1024));
    expect((requestPayload as Record<string, unknown>).resultSummary).toBe('b'.repeat(1000));
    expect((requestPayload as Record<string, unknown>).durationMs).toBe(0);
    expect((requestPayload as Record<string, unknown>).unknownField).toBeUndefined();
  });

  it('sanitizes behavior event payload before forwarding', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.post.mockResolvedValue({
      data: {
        success: true,
        data: {
          deviceId: 'ios-debug-device',
          appCode: 'openaipay',
          eventName: 'button_click',
        },
      },
    });

    await service.recordBehaviorEvent({
      deviceId: ' ios-debug-device ',
      appCode: ' openaipay ',
      eventName: ' button_click ',
      userId: -1,
      durationMs: -10,
      screenWidth: 1179.8,
      payloadJson: 'x'.repeat(22000),
      unknownField: 'ignore-me',
    });

    const [, requestPayload] = backendHttpService.post.mock.calls[0];
    expect((requestPayload as Record<string, unknown>).deviceId).toBe('ios-debug-device');
    expect((requestPayload as Record<string, unknown>).appCode).toBe('openaipay');
    expect((requestPayload as Record<string, unknown>).eventName).toBe('button_click');
    expect((requestPayload as Record<string, unknown>).durationMs).toBeUndefined();
    expect((requestPayload as Record<string, unknown>).userId).toBeUndefined();
    expect((requestPayload as Record<string, unknown>).screenWidth).toBe(1179);
    expect(((requestPayload as Record<string, unknown>).payloadJson as string).length).toBe(20000);
    expect((requestPayload as Record<string, unknown>).unknownField).toBeUndefined();
  });
});
