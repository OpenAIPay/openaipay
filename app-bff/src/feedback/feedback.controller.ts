import { BadRequestException, Body, Controller, Post } from '@nestjs/common';
import { FeedbackService } from './feedback.service';

@Controller('bff/feedback')
export class FeedbackController {
  constructor(private readonly feedbackService: FeedbackService) {}

  @Post('tickets')
  async submitTicket(@Body() payload?: Record<string, unknown>) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: '请求体不能为空',
      });
    }
    return this.feedbackService.submitTicket(payload);
  }
}
