import { HttpModule } from '@nestjs/axios';
import { MiddlewareConsumer, Module, NestModule } from '@nestjs/common';
import { APP_FILTER, APP_INTERCEPTOR, APP_PIPE, Reflector } from '@nestjs/core';
import { AccountController } from './account/account.controller';
import { AccountService } from './account/account.service';
import { AssetController } from './asset/asset.controller';
import { AssetService } from './asset/asset.service';
import { BillController } from './bill/bill.controller';
import { BillService } from './bill/bill.service';
import { AuthController } from './auth/auth.controller';
import { AuthService } from './auth/auth.service';
import { CashierController } from './cashier/cashier.controller';
import { CashierService } from './cashier/cashier.service';
import { BackendHttpService } from './common/backend-http.service';
import { AuthMiddleware } from './common/auth.middleware';
import { GlobalErrorFilter } from './common/error.filter';
import { NormalizePipe } from './common/normalize.pipe';
import { RequestContextMiddleware } from './common/request-context.middleware';
import { RequestLogInterceptor } from './common/request-log.interceptor';
import { ResponseInterceptor } from './common/response.interceptor';
import { ContactController } from './contact/contact.controller';
import { ContactService } from './contact/contact.service';
import { CouponController } from './coupon/coupon.controller';
import { CouponService } from './coupon/coupon.service';
import { DeliverController } from './deliver/deliver.controller';
import { DeliverService } from './deliver/deliver.service';
import { FeedbackController } from './feedback/feedback.controller';
import { FeedbackService } from './feedback/feedback.service';
import { HealthController } from './health/health.controller';
import { KycController } from './kyc/kyc.controller';
import { KycService } from './kyc/kyc.service';
import { MessageController } from './message/message.controller';
import { MessageService } from './message/message.service';
import { MediaController } from './media/media.controller';
import { MediaService } from './media/media.service';
import { MobileAppController } from './mobile-app/mobile-app.controller';
import { MobileAppService } from './mobile-app/mobile-app.service';
import { PageInitController } from './page-init/page-init.controller';
import { PageInitService } from './page-init/page-init.service';
import { ApiProxyController } from './proxy/api-proxy.controller';
import { ApiProxyService } from './proxy/api-proxy.service';
import { RootController } from './root/root.controller';
import { SearchController } from './search/search.controller';
import { SearchService } from './search/search.service';
import { TradeController } from './trade/trade.controller';
import { TradeService } from './trade/trade.service';
import { UserController } from './user/user.controller';
import { UserService } from './user/user.service';
import { UserFlowController } from './user-flow/user-flow.controller';
import { UserFlowService } from './user-flow/user-flow.service';

@Module({
  imports: [
    HttpModule.register({
      timeout: 10000,
      maxRedirects: 0,
    }),
  ],
  controllers: [
    RootController,
    HealthController,
    PageInitController,
    SearchController,
    ApiProxyController,
    AuthController,
    AccountController,
    AssetController,
    BillController,
    UserFlowController,
    UserController,
    KycController,
    ContactController,
    CouponController,
    DeliverController,
    FeedbackController,
    MessageController,
    MediaController,
    MobileAppController,
    CashierController,
    TradeController,
  ],
  providers: [
    PageInitService,
    SearchService,
    ApiProxyService,
    AuthService,
    AccountService,
    AssetService,
    BillService,
    UserFlowService,
    UserService,
    KycService,
    ContactService,
    CouponService,
    DeliverService,
    FeedbackService,
    MessageService,
    MediaService,
    MobileAppService,
    CashierService,
    TradeService,
    BackendHttpService,
    Reflector,
    {
      provide: APP_PIPE,
      useClass: NormalizePipe,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: RequestLogInterceptor,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: ResponseInterceptor,
    },
    {
      provide: APP_FILTER,
      useClass: GlobalErrorFilter,
    },
  ],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer): void {
    consumer.apply(RequestContextMiddleware, AuthMiddleware).forRoutes('*');
  }
}
