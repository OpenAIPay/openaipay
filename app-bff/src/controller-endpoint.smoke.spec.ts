import 'reflect-metadata';
import * as fs from 'node:fs';
import * as path from 'node:path';

const VALID_USER_ID = '880100068483692100';
const VALID_LOGIN_ID = '13800138000';
const VALID_PASSWORD = '777444';
const VALID_DEVICE_ID = 'ios-debug-device';

type ControllerClass = new (...args: unknown[]) => Record<string, unknown>;
type MockFn = jest.Mock<unknown, unknown[], unknown>;

describe('BffControllerEndpointSmokeTest', () => {
  const originalNodeEnv = process.env.NODE_ENV;

  beforeAll(() => {
    process.env.NODE_ENV = 'test';
  });

  afterAll(() => {
    process.env.NODE_ENV = originalNodeEnv;
  });

  it('invokes all mapped controller endpoints', async () => {
    const controllers = discoverControllers(path.resolve(__dirname));
    expect(controllers.length).toBeGreaterThan(0);

    const failures: string[] = [];
    let endpointCount = 0;

    for (const controllerClass of controllers) {
      const controller = instantiateController(controllerClass);
      const endpointMethods = discoverEndpointMethods(controllerClass);

      for (const methodName of endpointMethods) {
        endpointCount += 1;
        const method = controller[methodName] as (...args: unknown[]) => unknown;
        const args = buildMethodArguments(controllerClass.name, methodName, method);

        try {
          await Promise.resolve(method.apply(controller, args));
        } catch (error) {
          const message = error instanceof Error ? error.message : String(error);
          failures.push(`${controllerClass.name}#${methodName} -> ${message}`);
        }
      }
    }

    expect(endpointCount).toBeGreaterThanOrEqual(50);
    expect(failures).toEqual([]);
  });
});

function discoverControllers(rootDir: string): ControllerClass[] {
  const controllerFiles = listFiles(rootDir).filter(
    (filePath) => filePath.endsWith('.controller.ts') && !filePath.endsWith('.spec.ts'),
  );
  const controllers: ControllerClass[] = [];

  for (const filePath of controllerFiles) {
    const exportsRecord = require(filePath) as Record<string, unknown>;
    for (const exportedValue of Object.values(exportsRecord)) {
      if (
        typeof exportedValue === 'function' &&
        exportedValue.name.endsWith('Controller') &&
        Reflect.hasMetadata('path', exportedValue)
      ) {
        controllers.push(exportedValue as ControllerClass);
      }
    }
  }

  return controllers.sort((left, right) => left.name.localeCompare(right.name));
}

function listFiles(directory: string): string[] {
  const entries = fs.readdirSync(directory, { withFileTypes: true });
  const files: string[] = [];
  for (const entry of entries) {
    const absolutePath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...listFiles(absolutePath));
      continue;
    }
    files.push(absolutePath);
  }
  return files;
}

function instantiateController(controllerClass: ControllerClass): Record<string, unknown> {
  const parameterTypes = (Reflect.getMetadata('design:paramtypes', controllerClass) as unknown[]) ?? [];
  const dependencies = parameterTypes.map(() => createMockDependency());
  return new controllerClass(...dependencies);
}

function createMockDependency(): Record<string | symbol, unknown> {
  const cache = new Map<string | symbol, MockFn>();
  return new Proxy<Record<string | symbol, unknown>>(
    {},
    {
      get(_target, property) {
        if (!cache.has(property)) {
          const propertyName = String(property);
          const mock = jest.fn(async () => {
            if (propertyName === 'getMetricsSnapshot') {
              return {
                totalRequests: 0,
                failedRequests: 0,
                p95LatencyMs: 0,
              };
            }
            return {
              ok: true,
              method: propertyName,
            };
          }) as MockFn;
          cache.set(property, mock);
        }
        return cache.get(property);
      },
    },
  );
}

function discoverEndpointMethods(controllerClass: ControllerClass): string[] {
  const methods = Object.getOwnPropertyNames(controllerClass.prototype)
    .filter((methodName) => methodName !== 'constructor')
    .filter((methodName) => {
      const method = controllerClass.prototype[methodName] as unknown;
      return typeof method === 'function'
        && Reflect.hasMetadata('path', method)
        && Reflect.hasMetadata('method', method);
    });
  return methods.sort((left, right) => left.localeCompare(right));
}

function buildMethodArguments(
  controllerName: string,
  methodName: string,
  method: (...args: unknown[]) => unknown,
): unknown[] {
  return extractParameterNames(method).map((parameterName, index) =>
    createArgument(parameterName, controllerName, methodName, index),
  );
}

function extractParameterNames(method: (...args: unknown[]) => unknown): string[] {
  const source = method.toString();
  const matched = source.match(/^[^(]*\(([\s\S]*?)\)/);
  if (!matched || !matched[1].trim()) {
    return [];
  }
  return matched[1]
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0)
    .map((segment) => segment.replace(/\/\*.*?\*\//g, '').trim())
    .map((segment) => segment.replace(/=.*$/, '').trim())
    .map((segment) => segment.replace(/^\.\.\./, '').trim());
}

function createArgument(
  parameterName: string,
  _controllerName: string,
  _methodName: string,
  _index: number,
): unknown {
  const lower = parameterName.toLowerCase();
  if (lower === 'req') {
    return createRequestMock();
  }
  if (lower === 'res') {
    return createResponseMock();
  }
  if (lower === 'payload') {
    return createPayload();
  }
  if (lower === 'query') {
    return {
      ownerUserId: VALID_USER_ID,
      targetUserId: VALID_USER_ID,
      requesterUserId: VALID_USER_ID,
      keyword: '红包',
      limit: '20',
      userId: VALID_USER_ID,
    };
  }
  if (lower === 'userid' || lower === 'uid' || lower === 'owneruserid' || lower === 'requesteruserid') {
    return VALID_USER_ID;
  }
  if (lower === 'loginid' || lower === 'mobile') {
    return VALID_LOGIN_ID;
  }
  if (lower === 'password') {
    return VALID_PASSWORD;
  }
  if (lower === 'deviceid') {
    return VALID_DEVICE_ID;
  }
  if (lower === 'limit') {
    return '20';
  }
  if (lower === 'pageno') {
    return '1';
  }
  if (lower === 'pagesize') {
    return '20';
  }
  if (lower === 'year') {
    return '2026';
  }
  if (lower === 'amount') {
    return '100.00';
  }
  if (lower === 'billmonth') {
    return '2026-03';
  }
  if (lower === 'businessdomaincode') {
    return 'TRADE';
  }
  if (lower === 'productcode') {
    return 'AICREDIT';
  }
  if (lower === 'fundcode') {
    return 'AICASH';
  }
  if (lower === 'currencycode') {
    return 'CNY';
  }
  if (lower === 'paymentmethod') {
    return 'BALANCE';
  }
  if (lower === 'keyword') {
    return '红包';
  }
  if (lower === 'page') {
    return 'home';
  }
  if (lower === 'appcode') {
    return 'openaipay';
  }
  if (lower === 'currentversionno') {
    return '1.0.0';
  }
  if (lower === 'scenecode') {
    return 'TOP_UP';
  }
  if (lower === 'channel') {
    return 'APP';
  }
  if (lower === 'clientid') {
    return 'ios-client';
  }
  if (lower === 'positioncodelist') {
    return 'HOME_BANNER';
  }
  if (lower === 'conversationno') {
    return 'CONV202603210001';
  }
  if (lower === 'beforemessageid') {
    return '10001';
  }
  if (lower === 'redpacketno') {
    return 'RP202603210001';
  }
  if (lower === 'mediaid') {
    return 'MEDIA202603210001';
  }
  if (lower === 'realname') {
    return '顾郡';
  }
  if (lower === 'idcardno') {
    return '440101199001011234';
  }
  if (lower.endsWith('id')) {
    return `${parameterName}-001`;
  }
  return 'mock-value';
}

function createPayload(): Record<string, unknown> {
  return {
    userId: VALID_USER_ID,
    ownerUserId: VALID_USER_ID,
    loginId: VALID_LOGIN_ID,
    mobile: VALID_LOGIN_ID,
    password: VALID_PASSWORD,
    deviceId: VALID_DEVICE_ID,
    nickname: '测试用户',
    realName: '顾郡',
    idCardNo: '440101199001011234',
    businessNo: 'BIZ202603210001',
    requestNo: 'REQ202603210001',
    sceneCode: 'TOP_UP',
    clientId: 'ios-client',
    channel: 'APP',
    positionCodeList: 'HOME_BANNER',
    amount: '100.00',
    currencyCode: 'CNY',
    paymentMethod: 'BALANCE',
    fundCode: 'AICASH',
    productCode: 'AICREDIT',
    conversationNo: 'CONV202603210001',
    redPacketNo: 'RP202603210001',
    messageText: '测试消息',
    operatorUserId: VALID_USER_ID,
    action: 'ACCEPT',
  };
}

function createRequestMock(): Record<string, unknown> {
  return {
    method: 'POST',
    url: '/api/mock',
    headers: {},
    query: {},
    params: {},
    body: createPayload(),
    authenticatedUserId: VALID_USER_ID,
  };
}

function createResponseMock(): Record<string, unknown> {
  const response: Record<string, unknown> = {};
  response.status = jest.fn(() => response);
  response.json = jest.fn(() => response);
  response.send = jest.fn(() => response);
  response.end = jest.fn(() => response);
  response.type = jest.fn(() => response);
  response.setHeader = jest.fn(() => response);
  response.header = jest.fn(() => response);
  return response;
}
