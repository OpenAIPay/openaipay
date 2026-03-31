import { BadRequestException, Body, Controller, Get, Headers, HttpCode, Post, Query } from '@nestjs/common';
import { AuthService } from './auth.service';

@Controller('bff/auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('mobile-verify-login')
  @HttpCode(200)
  async mobileVerifyLogin(
    @Body('loginId') loginId?: string | number,
    @Body('deviceId') deviceId?: string | number,
    @Headers('x-legacy-device-ids') legacyDeviceIdsHeader?: string,
  ) {
    const normalizedLoginId = this.normalizeRequiredText(loginId);
    const normalizedDeviceId = this.normalizeRequiredText(deviceId);
    if (!normalizedLoginId || !normalizedDeviceId) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'loginId 和 deviceId 不能为空',
      });
    }
    return this.authService.mobileVerifyLogin(
      normalizedLoginId,
      normalizedDeviceId,
      undefined,
      this.normalizeOptionalText(legacyDeviceIdsHeader),
    );
  }

  @Post('demo-auto-login')
  @HttpCode(200)
  async demoAutoLogin(
    @Body('deviceId') deviceId?: string | number,
    @Body('preferredLoginId') preferredLoginId?: string | number,
    @Headers('x-legacy-device-ids') legacyDeviceIdsHeader?: string,
  ) {
    const normalizedDeviceId = this.normalizeRequiredText(deviceId);
    const normalizedPreferredLoginId = this.normalizeOptionalText(preferredLoginId);
    if (!normalizedDeviceId) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'deviceId 不能为空',
      });
    }
    return this.authService.demoAutoLogin(
      normalizedDeviceId,
      normalizedPreferredLoginId,
      this.normalizeOptionalText(legacyDeviceIdsHeader),
    );
  }

  @Get('preset-login-accounts')
  @HttpCode(200)
  async listPresetLoginAccounts(
    @Query('deviceId') deviceId?: string | number,
    @Headers('x-legacy-device-ids') legacyDeviceIdsHeader?: string,
  ) {
    const normalizedDeviceId = this.normalizeRequiredText(deviceId);
    if (!normalizedDeviceId) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'deviceId 不能为空',
      });
    }
    return this.authService.listPresetLoginAccounts(
      normalizedDeviceId,
      this.normalizeOptionalText(legacyDeviceIdsHeader),
    );
  }

  private normalizeRequiredText(value: string | number | undefined): string | undefined {
    if (typeof value === 'number') {
      return String(value);
    }
    if (typeof value === 'string') {
      const normalized = value.trim();
      return normalized.length > 0 ? normalized : undefined;
    }
    return undefined;
  }

  private normalizeOptionalText(value: string | number | undefined): string | undefined {
    return this.normalizeRequiredText(value);
  }
}
