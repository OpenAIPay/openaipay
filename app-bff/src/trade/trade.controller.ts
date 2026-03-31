import { BadRequestException, Body, Controller, Post } from '@nestjs/common';
import { TradeService } from './trade.service';

@Controller('bff/trade')
export class TradeController {
  constructor(private readonly tradeService: TradeService) {}

  @Post('transfer')
  async createTransferTrade(@Body() payload?: Record<string, unknown>) {
    return this.tradeService.createTransferTrade(this.assertPayload(payload));
  }

  @Post('deposit')
  async createDepositTrade(@Body() payload?: Record<string, unknown>) {
    return this.tradeService.createDepositTrade(this.assertPayload(payload));
  }

  @Post('withdraw')
  async createWithdrawTrade(@Body() payload?: Record<string, unknown>) {
    return this.tradeService.createWithdrawTrade(this.assertPayload(payload));
  }

  @Post('pay')
  async createPayTrade(@Body() payload?: Record<string, unknown>) {
    return this.tradeService.createPayTrade(this.assertPayload(payload));
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
