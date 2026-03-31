import { BadRequestException, Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { MobileAppService } from './mobile-app.service';

@Controller('bff/apps')
export class MobileAppController {
  constructor(private readonly mobileAppService: MobileAppService) {}

  @Get(':appCode/versions/check')
  async checkVersion(
    @Param('appCode') appCode?: string,
    @Query('currentVersionNo') currentVersionNo?: string,
    @Query('deviceId') deviceId?: string,
  ) {
    const normalizedAppCode = appCode?.trim() ?? '';
    if (!normalizedAppCode) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'appCode 不能为空',
      });
    }
    return this.mobileAppService.checkVersion(normalizedAppCode, currentVersionNo, deviceId);
  }

  @Post('devices')
  async upsertDevice(@Body() payload?: Record<string, unknown>) {
    return this.mobileAppService.upsertDevice(this.assertPayload(payload));
  }

  @Post('visit-records')
  async recordVisit(@Body() payload?: Record<string, unknown>) {
    return this.mobileAppService.recordVisit(this.assertPayload(payload));
  }

  @Post('behavior-events')
  async recordBehaviorEvent(@Body() payload?: Record<string, unknown>) {
    return this.mobileAppService.recordBehaviorEvent(this.assertPayload(payload));
  }

  private assertPayload(payload?: Record<string, unknown>): Record<string, unknown> {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return payload;
  }
}
