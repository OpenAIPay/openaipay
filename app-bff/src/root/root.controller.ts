import { Controller, Get } from '@nestjs/common';

@Controller()
export class RootController {
  @Get()
  index() {
    return {
      service: 'app-bff',
      status: 'running',
      endpoints: {
        health: '/health',
        pageInit: '/bff/page-init?page=home&uid=880100068483692100',
        search: '/bff/search?uid=880100068483692100&keyword=红包',
        login: '/bff/auth/mobile-verify-login',
        profileExample: '/bff/users/880100068483692100/profile',
      },
      now: new Date().toISOString(),
    };
  }
}
