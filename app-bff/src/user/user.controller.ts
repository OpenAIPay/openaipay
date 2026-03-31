import { BadRequestException, Body, Controller, Get, Param, Put, Query } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { UserService } from './user.service';

@Controller('bff/users')
export class UserController {
  constructor(private readonly userService: UserService) {}

  @Get('profile-by-login')
  async getProfileByLoginId(@Query('loginId') loginId?: unknown) {
    return this.userService.getProfileByLoginId(this.assertMainlandMobile(loginId));
  }

  @Get(':userId/profile')
  async getProfile(@Param('userId') userId?: unknown) {
    return this.userService.getProfile(this.assertUserId(userId));
  }

  @Put(':userId/profile')
  async updateProfile(@Param('userId') userId?: unknown, @Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return this.userService.updateProfile(this.assertUserId(userId), payload);
  }

  @Get(':userId/recent-contacts')
  async getRecentContacts(@Param('userId') userId?: unknown, @Query('limit') limit?: unknown) {
    return this.userService.getRecentContacts(this.assertUserId(userId), this.normalizeLimit(limit, 50, 20));
  }

  @Get(':userId/friends')
  async getFriends(@Param('userId') userId?: unknown, @Query('limit') limit?: unknown) {
    return this.userService.getFriends(this.assertUserId(userId), this.normalizeLimit(limit, 200, 100));
  }

  private assertUserId(value: unknown): string {
    const normalized = normalizePositiveLongId(value, { minDigits: 6, maxDigits: 20 });
    if (normalized) {
      return normalized;
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: 'userId 参数格式不正确，请使用字符串传递18位ID',
    });
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

  private normalizeLimit(value: unknown, max: number, fallback: number): number {
    if (value === undefined || value === null || value === '') {
      return fallback;
    }
    if (typeof value === 'number' && Number.isInteger(value) && value > 0 && value <= max) {
      return value;
    }
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      const parsed = Number.parseInt(value, 10);
      if (parsed > 0 && parsed <= max) {
        return parsed;
      }
    }
    throw new BadRequestException({
      code: 'INVALID_ARGUMENT',
      message: `limit 参数必须是 1-${max} 的整数`,
    });
  }
}
