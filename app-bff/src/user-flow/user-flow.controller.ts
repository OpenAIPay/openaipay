import { BadRequestException, Body, Controller, Get, Headers, Post, Query } from '@nestjs/common';
import { UserFlowService } from './user-flow.service';

@Controller('bff/user-flows')
export class UserFlowController {
  constructor(private readonly userFlowService: UserFlowService) {}

  @Get('register/check')
  async checkRegisterPhone(
    @Query('loginId') loginId?: unknown,
    @Headers('x-device-id') deviceId?: unknown,
  ) {
    return this.userFlowService.checkRegisterPhone(
      this.assertMainlandMobile(loginId),
      this.normalizeOptionalText(deviceId),
    );
  }

  @Post('registrations')
  async register(
    @Body() payload?: Record<string, unknown>,
  ) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    const loginId = this.assertMainlandMobile(payload['loginId']);
    const mobile = payload['mobile'] === undefined || payload['mobile'] === null
      ? loginId
      : this.assertMainlandMobile(payload['mobile']);
    const nextPayload = {
      ...payload,
      deviceId: this.assertDeviceId(payload['deviceId']),
      legacyDeviceIds: this.normalizeLegacyDeviceIds(payload['legacyDeviceIds']),
      loginId,
      mobile,
    };
    return this.userFlowService.register(nextPayload);
  }

  private assertMainlandMobile(value: unknown): string {
    const normalized = this.normalizeMainlandMobile(value);
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'loginId 参数格式不正确',
    });
  }

  private normalizeMainlandMobile(value: unknown): string | null {
    if (value === null || value === undefined) {
      return null;
    }
    const raw = typeof value === 'number' ? String(Math.trunc(value)) : String(value).trim();
    if (!raw) {
      return null;
    }
    const ascii = raw.replace(/[０-９]/g, (digit) =>
      String.fromCharCode(digit.charCodeAt(0) - 0xff10 + 0x30),
    );
    let digits = ascii.replace(/\D/g, '');
    if (digits.length === 13 && digits.startsWith('86')) {
      digits = digits.slice(2);
    } else if (digits.length === 15 && digits.startsWith('0086')) {
      digits = digits.slice(4);
    }
    return /^1[3-9]\d{9}$/.test(digits) ? digits : null;
  }

  private normalizeOptionalText(value: unknown): string | undefined {
    if (typeof value !== 'string') {
      return undefined;
    }
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
  }

  private assertDeviceId(value: unknown): string {
    const normalized = this.normalizeOptionalText(value);
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'deviceId 不能为空',
    });
  }

  private normalizeLegacyDeviceIds(value: unknown): string[] {
    if (!Array.isArray(value) || value.length === 0) {
      return [];
    }
    const deduplicated = new Set<string>();
    value.forEach((item) => {
      const normalized = this.normalizeOptionalText(item);
      if (normalized) {
        deduplicated.add(normalized);
      }
    });
    return Array.from(deduplicated);
  }
}
