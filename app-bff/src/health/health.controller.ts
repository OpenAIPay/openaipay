import { Controller, Get, NotFoundException } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';

@Controller('health')
export class HealthController {
  constructor(private readonly backendHttpService: BackendHttpService) {}

  @Get()
  check() {
    return {
      status: 'ok',
      service: 'app-bff',
      now: new Date().toISOString(),
    };
  }

  @Get('backend-http')
  backendHttp() {
    if (!this.shouldExposeBackendHttpMetrics()) {
      throw new NotFoundException({
        code: 'NOT_FOUND',
        message: '资源不存在',
      });
    }
    return this.backendHttpService.getMetricsSnapshot();
  }

  private shouldExposeBackendHttpMetrics(): boolean {
    const explicit = (process.env.BFF_EXPOSE_BACKEND_HTTP_METRICS ?? '').trim().toLowerCase();
    if (explicit === 'true') {
      return true;
    }
    const nodeEnv = (process.env.NODE_ENV ?? '').trim().toLowerCase();
    return nodeEnv === 'development' || nodeEnv === 'test';
  }
}
