import { BadRequestException, Body, Controller, Get, Post, Query } from '@nestjs/common';
import { DeliverService } from './deliver.service';

@Controller('bff/deliver')
export class DeliverController {
  constructor(private readonly deliverService: DeliverService) {}

  @Get()
  async queryDeliver(
    @Query('positionCodeList') positionCodeList?: string,
    @Query('clientId') clientId?: string,
    @Query('sceneCode') sceneCode?: string,
    @Query('channel') channel?: string,
    @Query('userId') userId?: string,
  ) {
    if (!positionCodeList?.trim() || !clientId?.trim() || !sceneCode?.trim() || !channel?.trim()) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'positionCodeList、clientId、sceneCode、channel 不能为空',
      });
    }
    return this.deliverService.queryDeliver({
      positionCodeList,
      clientId,
      sceneCode,
      channel,
      userId,
    });
  }

  @Post('events')
  async reportEvent(@Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return this.deliverService.reportEvent(payload);
  }
}
