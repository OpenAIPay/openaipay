import { All, Controller, Req, Res } from '@nestjs/common';
import type { Request, Response } from 'express';
import { ApiProxyService } from './api-proxy.service';
import { RawResponse } from '../common/raw-response.decorator';

@Controller()
export class ApiProxyController {
  constructor(private readonly apiProxyService: ApiProxyService) {}

  @All('api/*path')
  @RawResponse()
  async proxy(@Req() req: Request, @Res() res: Response): Promise<void> {
    await this.apiProxyService.forward(req, res);
  }
}
