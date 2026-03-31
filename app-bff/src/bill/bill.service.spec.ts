import { BillService } from './bill.service';
import { BackendHttpService } from '../common/backend-http.service';

describe('BillService', () => {
  const createService = () => {
    const backendHttpService = {
      get: jest.fn(),
    } as unknown as jest.Mocked<BackendHttpService>;
    const service = new BillService(backendHttpService);
    return { service, backendHttpService };
  };

  it('maps tradeType/direction and falls back tradeNo from tradeOrderNo', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032210111200000000000001',
              businessDomainCode: 'ailoan',
              bizOrderNo: 'LOAN-REPAY-0001',
              productType: 'ailoan',
              businessType: 'APP_LOAN_ACCOUNT_CREDIT_REPAY',
              direction: 'debit',
              tradeType: 'loan',
              displayTitle: '爱借还款',
              displaySubtitle: '信用借还',
              amount: '188.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-22T10:11:12',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries(
      '880109000000000001',
      1,
      20,
      '2026-03',
      'AILOAN',
      undefined,
    );
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0].tradeNo).toBe('30922026032210111200000000000001');
    expect(items[0].businessDomainCode).toBe('AILOAN');
    expect(items[0].direction).toBe('DEBIT');
    expect(items[0].tradeType).toBe('LOAN');
    expect(items[0].tradeTime).toBe('2026-03-22 10:11:12');
  });

  it('maps coupon discount from backend discountAmount field', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032309150000000000000001',
              businessDomainCode: 'trade',
              bizOrderNo: 'BIZ-TOPUP-0001',
              productType: 'trade',
              businessType: 'APP_MOBILE_HALL_TOP_UP',
              direction: 'debit',
              tradeType: 'pay',
              displayTitle: '话费充值',
              displaySubtitle: '中国移动',
              amount: '30.00',
              discountAmount: '3.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T09:15:00',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries('880109000000000001', 1, 20, '2026-03');
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0].couponDiscountAmount).toBe('3.00');
  });

  it('maps aicash income settle entries to explicit yield bill title', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032302000000210006500000',
              businessDomainCode: 'aicash',
              bizOrderNo: 'FIS:AICASH:20260323:880109000000000001',
              productType: 'aicash',
              businessType: 'YIELD_SETTLE',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: 'FUND_INCOME_SETTLE',
              displaySubtitle: 'FUND_ACCOUNT',
              amount: '-10.19',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T02:00:00',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries(
      '880109000000000001',
      1,
      20,
      '2026-03',
      'AICASH',
      undefined,
    );
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0].displayTitle).toBe('爱存收益发放');
    expect(items[0].displaySubtitle).toBe('投资理财');
    expect(items[0].direction).toBe('CREDIT');
    expect(items[0].amount).toBe('10.19');
  });

  it('maps aicash redeem entries to destination-aware bill titles', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032311111100110000000001',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-AICASH-REDEEM-BALANCE',
              productType: 'aicash',
              businessType: 'FUND_FAST_REDEEM',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: 'FUND_FAST_REDEEM',
              displaySubtitle: 'FUND_ACCOUNT',
              amount: '500.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T11:11:11',
            },
            {
              tradeOrderNo: '30922026032312121200110000000002',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-AICASH-REDEEM-BANK',
              productType: 'aicash',
              businessType: 'FUND_REDEEM',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: 'FUND_REDEEM',
              displaySubtitle: '招商银行(尾号8888)',
              amount: '800.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T12:12:12',
            },
            {
              tradeOrderNo: '30922026032313131300110000000003',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-AICASH-REDEEM-BANK-GENERIC',
              productType: 'aicash',
              businessType: 'FUND_FAST_REDEEM',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: 'FUND_FAST_REDEEM',
              displaySubtitle: 'BANK_CARD',
              amount: '300.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T13:13:13',
            },
            {
              tradeOrderNo: '30922026032313595900110000000005',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-AICASH-REDEEM-BANK-PREFIXED-TITLE',
              productType: 'aicash',
              businessType: 'FUND_REDEEM',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: '爱存转出至爱存转出至中国银行',
              displaySubtitle: '投资理财',
              amount: '520.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T13:59:59',
            },
            {
              tradeOrderNo: '30922026032314141400110000000004',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-AICASH-REDEEM-BANK-CARD-TEXT',
              productType: 'aicash',
              businessType: 'FUND_FAST_REDEEM',
              direction: 'credit',
              tradeType: 'fund',
              displayTitle: 'FUND_FAST_REDEEM',
              displaySubtitle: '储蓄卡(尾号6666)',
              amount: '260.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T14:14:14',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries(
      '880109000000000001',
      1,
      20,
      '2026-03',
      'AICASH',
      undefined,
    );
    const items = (result.items ?? []) as Array<Record<string, string>>;
    const entryByTradeNo = new Map(items.map((item) => [item.tradeNo, item]));

    expect(items).toHaveLength(5);
    expect(entryByTradeNo.get('30922026032311111100110000000001')?.displayTitle).toBe('爱存转出至余额');
    expect(entryByTradeNo.get('30922026032312121200110000000002')?.displayTitle).toBe('爱存转出至招商银行');
    expect(entryByTradeNo.get('30922026032313131300110000000003')?.displayTitle).toBe('爱存转出至银行卡');
    expect(entryByTradeNo.get('30922026032313595900110000000005')?.displayTitle).toBe('爱存转出至中国银行');
    expect(entryByTradeNo.get('30922026032314141400110000000004')?.displayTitle).toBe('爱存转出至银行卡');
  });

  it('maps ailoan draw entries to destination-aware bill titles', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032817100100110000000001',
              businessDomainCode: 'ailoan',
              bizOrderNo: 'BIZ-AILOAN-DRAW-BANK',
              productType: 'ailoan',
              businessType: 'TRADE_PAY_LOAN_ACCOUNT',
              direction: 'credit',
              tradeType: 'loan',
              displayTitle: 'APP_LOAN_DRAW',
              displaySubtitle: '中国农业银行(尾号9005)',
              amount: '12000.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-28T17:10:01',
            },
            {
              tradeOrderNo: '30922026032817100200110000000002',
              businessDomainCode: 'ailoan',
              bizOrderNo: 'BIZ-AILOAN-DRAW-BALANCE',
              productType: 'ailoan',
              businessType: 'TRADE_PAY_LOAN_ACCOUNT',
              direction: 'credit',
              tradeType: 'loan',
              displayTitle: 'APP_LOAN_DRAW',
              displaySubtitle: 'WALLET',
              amount: '8000.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-28T17:10:02',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries(
      '880109000000000001',
      1,
      20,
      '2026-03',
      'AILOAN',
      undefined,
    );
    const items = (result.items ?? []) as Array<Record<string, string>>;
    const entryByTradeNo = new Map(items.map((item) => [item.tradeNo, item]));

    expect(items).toHaveLength(2);
    expect(entryByTradeNo.get('30922026032817100100110000000001')?.displayTitle).toBe('爱借放款至中国农业银行');
    expect(entryByTradeNo.get('30922026032817100200110000000002')?.displayTitle).toBe('爱借放款至余额');
  });

  it('filters out aicash subscribe freeze entries', async () => {
    const { service, backendHttpService } = createService();
    backendHttpService.get.mockResolvedValue({
      data: {
        success: true,
        data: {
          pageNo: 1,
          pageSize: 20,
          hasMore: false,
          nextPageNo: null,
          items: [
            {
              tradeOrderNo: '30922026032310150000000000000001',
              businessDomainCode: 'aicash',
              bizOrderNo: 'BIZ-FUND-FREEZE-0001',
              productType: 'aicash',
              businessType: 'FUND_PAY_FREEZE',
              direction: 'debit',
              tradeType: 'fund',
              displayTitle: '爱存申购冻结',
              displaySubtitle: '投资理财',
              amount: '100.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T10:15:00',
            },
            {
              tradeOrderNo: '30922026032310160000000000000001',
              businessDomainCode: 'trade',
              bizOrderNo: 'BIZ-TRANSFER-0001',
              productType: 'trade',
              businessType: 'APP_INTERNAL_TRANSFER',
              direction: 'debit',
              tradeType: 'transfer',
              displayTitle: '向林泽楷转账',
              displaySubtitle: '转账',
              amount: '10.00',
              currencyCode: 'cny',
              status: 'SUCCEEDED',
              tradeTime: '2026-03-23T10:16:00',
            },
          ],
        },
      },
    });

    const result = await service.getUserBillEntries(
      '880109000000000001',
      1,
      20,
      '2026-03',
      undefined,
      undefined,
    );
    const items = (result.items ?? []) as Array<Record<string, string>>;

    expect(items).toHaveLength(1);
    expect(items[0].displayTitle).toBe('向林泽楷转账');
  });
});
