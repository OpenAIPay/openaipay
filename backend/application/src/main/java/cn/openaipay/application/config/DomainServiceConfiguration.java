package cn.openaipay.application.config;

import cn.openaipay.domain.app.service.AppVersionDomainService;
import cn.openaipay.domain.app.service.impl.AppVersionDomainServiceImpl;
import cn.openaipay.domain.accounting.service.AccountingPostingDomainService;
import cn.openaipay.domain.accounting.service.impl.AccountingPostingDomainServiceImpl;
import cn.openaipay.domain.cashier.service.CashierDomainService;
import cn.openaipay.domain.cashier.service.impl.CashierDomainServiceImpl;
import cn.openaipay.domain.contact.service.ContactDomainService;
import cn.openaipay.domain.contact.service.impl.ContactDomainServiceImpl;
import cn.openaipay.domain.conversation.service.ConversationDomainService;
import cn.openaipay.domain.conversation.service.impl.ConversationDomainServiceImpl;
import cn.openaipay.domain.creditaccount.service.CreditBillDomainService;
import cn.openaipay.domain.creditaccount.service.impl.CreditBillDomainServiceImpl;
import cn.openaipay.domain.deliver.repository.PositionUnitCreativeRelationRepository;
import cn.openaipay.domain.deliver.service.RecallDomainService;
import cn.openaipay.domain.deliver.service.impl.RecallDomainServiceImpl;
import cn.openaipay.domain.pay.service.PayOrderDomainService;
import cn.openaipay.domain.pay.service.impl.PayOrderDomainServiceImpl;
import cn.openaipay.domain.payroute.service.PayRouteDomainService;
import cn.openaipay.domain.payroute.service.impl.PayRouteDomainServiceImpl;
import cn.openaipay.domain.riskpolicy.model.RiskSceneCode;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.riskpolicy.service.impl.RiskPolicyDomainServiceImpl;
import cn.openaipay.domain.settle.service.SettleDomainService;
import cn.openaipay.domain.settle.service.impl.SettleDomainServiceImpl;
import cn.openaipay.domain.trade.service.TradePayResultDomainService;
import cn.openaipay.domain.trade.service.TradeRefundDomainService;
import cn.openaipay.domain.trade.service.TradeSplitDomainService;
import cn.openaipay.domain.trade.service.impl.TradePayResultDomainServiceImpl;
import cn.openaipay.domain.trade.service.impl.TradeRefundDomainServiceImpl;
import cn.openaipay.domain.trade.service.impl.TradeSplitDomainServiceImpl;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import cn.openaipay.domain.shared.security.impl.CredentialDomainServiceImpl;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务装配配置。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Configuration
public class DomainServiceConfiguration {

    /**
     * 处理领域服务信息。
     */
    @Bean
    public RecallDomainService recallDomainService(
            PositionUnitCreativeRelationRepository positionUnitCreativeRelationRepository) {
        return new RecallDomainServiceImpl(positionUnitCreativeRelationRepository);
    }

    /**
     * 处理支付订单领域服务信息。
     */
    @Bean
    public PayOrderDomainService payOrderDomainService() {
        return new PayOrderDomainServiceImpl();
    }

    /**
     * 处理支付领域服务信息。
     */
    @Bean
    public PayRouteDomainService payRouteDomainService() {
        return new PayRouteDomainServiceImpl();
    }

    /**
     * 处理结算领域服务信息。
     */
    @Bean
    public SettleDomainService settleDomainService() {
        return new SettleDomainServiceImpl();
    }

    /**
     * 处理交易退款领域服务信息。
     */
    @Bean
    public TradeRefundDomainService tradeRefundDomainService() {
        return new TradeRefundDomainServiceImpl();
    }

    /**
     * 处理交易领域服务信息。
     */
    @Bean
    public TradeSplitDomainService tradeSplitDomainService() {
        return new TradeSplitDomainServiceImpl();
    }

    /**
     * 处理交易支付结果领域服务信息。
     */
    @Bean
    public TradePayResultDomainService tradePayResultDomainService() {
        return new TradePayResultDomainServiceImpl();
    }

    /**
     * 处理应用版本领域服务信息。
     */
    @Bean
    public AppVersionDomainService appVersionDomainService() {
        return new AppVersionDomainServiceImpl();
    }

    /**
     * 处理领域服务信息。
     */
    @Bean
    public AccountingPostingDomainService accountingPostingDomainService() {
        return new AccountingPostingDomainServiceImpl();
    }

    /**
     * 处理信用领域服务信息。
     */
    @Bean
    public CreditBillDomainService creditBillDomainService() {
        return new CreditBillDomainServiceImpl();
    }

    /**
     * 处理领域服务信息。
     */
    @Bean
    public CashierDomainService cashierDomainService() {
        return new CashierDomainServiceImpl();
    }

    /**
     * 处理联系人领域服务信息。
     */
    @Bean
    public ContactDomainService contactDomainService() {
        return new ContactDomainServiceImpl();
    }

    /**
     * 处理会话领域服务信息。
     */
    @Bean
    public ConversationDomainService conversationDomainService() {
        return new ConversationDomainServiceImpl();
    }

    /**
     * 处理领域服务信息。
     */
    @Bean
    public CredentialDomainService credentialDomainService(
            @Value("${aipay.security.token-signing-secret}")
            String tokenSigningSecret,
            @Value("${aipay.security.token-default-expires-in-seconds:604800}")
            long tokenDefaultExpiresInSeconds) {
        return new CredentialDomainServiceImpl(tokenSigningSecret, tokenDefaultExpiresInSeconds);
    }

    /**
     * 处理风控策略领域服务信息。
     */
    @Bean
    public RiskPolicyDomainService riskPolicyDomainService(
            @Value("${aipay.risk.loan-draw-single-limit:880000}") BigDecimal loanDrawSingleLimit,
            @Value("${aipay.risk.loan-repay-single-limit:3000000}") BigDecimal loanRepaySingleLimit,
            @Value("${aipay.risk.fund-subscribe-single-limit:500000}") BigDecimal fundSubscribeSingleLimit,
            @Value("${aipay.risk.fund-redeem-single-limit:500000}") BigDecimal fundRedeemSingleLimit,
            @Value("${aipay.risk.fund-fast-redeem-single-limit:20000}") BigDecimal fundFastRedeemSingleLimit,
            @Value("${aipay.risk.fund-switch-single-limit:500000}") BigDecimal fundSwitchSingleLimit,
            @Value("${aipay.risk.fund-pay-freeze-single-limit:500000}") BigDecimal fundPayFreezeSingleLimit) {
        Map<RiskSceneCode, BigDecimal> limits = new EnumMap<>(RiskSceneCode.class);
        limits.put(RiskSceneCode.LOAN_DRAW, loanDrawSingleLimit);
        limits.put(RiskSceneCode.LOAN_REPAY, loanRepaySingleLimit);
        limits.put(RiskSceneCode.FUND_SUBSCRIBE, fundSubscribeSingleLimit);
        limits.put(RiskSceneCode.FUND_REDEEM, fundRedeemSingleLimit);
        limits.put(RiskSceneCode.FUND_FAST_REDEEM, fundFastRedeemSingleLimit);
        limits.put(RiskSceneCode.FUND_SWITCH, fundSwitchSingleLimit);
        limits.put(RiskSceneCode.FUND_PAY_FREEZE, fundPayFreezeSingleLimit);
        return new RiskPolicyDomainServiceImpl(limits);
    }
}
