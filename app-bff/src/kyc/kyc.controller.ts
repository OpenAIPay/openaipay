import { BadRequestException, Body, Controller, Get, Param, Post } from '@nestjs/common';
import { normalizePositiveLongId } from '../common/id-normalizer';
import { KycService } from './kyc.service';

@Controller('bff/kyc/users')
export class KycController {
  constructor(private readonly kycService: KycService) {}

  @Get(':userId/status')
  async getStatus(@Param('userId') userId?: unknown) {
    return this.kycService.getStatus(this.assertUserId(userId));
  }

  @Post(':userId/submissions')
  async submit(@Param('userId') userId?: unknown, @Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    const realName = typeof payload['realName'] === 'string' ? payload['realName'].trim() : '';
    const idCardNo = typeof payload['idCardNo'] === 'string' ? payload['idCardNo'].trim().toUpperCase() : '';
    if (!realName || !idCardNo) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'realName 和 idCardNo 不能为空',
      });
    }
    return this.kycService.submit(this.assertUserId(userId), { realName, idCardNo });
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
}
