package cn.openaipay.application.payroute.service.impl;

import cn.openaipay.application.payroute.dto.PayRouteDTO;
import cn.openaipay.application.payroute.port.CreditRouteAccountPort;
import cn.openaipay.application.payroute.port.LoanRouteAccountPort;
import cn.openaipay.application.payroute.service.PayRouteService;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.payroute.service.CreditPayRoutePlan;
import cn.openaipay.domain.payroute.service.PayRouteDomainService;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

/**
 * 支付产品路由应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class PayRouteServiceImpl implements PayRouteService {

    /** 借款信息 */
    private static final String ACCOUNT_MODULE_LOAN = "LOAN_ACCOUNT";

    /** 支付域信息 */
    private final PayRouteDomainService payRouteDomainService;
    /** 信用账户路由能力端口。 */
    private final CreditRouteAccountPort creditRouteAccountPort;
    /** 借款账户路由能力端口。 */
    private final LoanRouteAccountPort loanRouteAccountPort;

    public PayRouteServiceImpl(PayRouteDomainService payRouteDomainService,
                               CreditRouteAccountPort creditRouteAccountPort,
                               LoanRouteAccountPort loanRouteAccountPort) {
        this.payRouteDomainService = payRouteDomainService;
        this.creditRouteAccountPort = creditRouteAccountPort;
        this.loanRouteAccountPort = loanRouteAccountPort;
    }

    /**
     * 处理信用用于支付信息。
     */
    @Override
    public PayRouteDTO routeCreditForPay(String businessSceneCode,
                                         String paymentMethod,
                                         Long payerUserId,
                                         Long payeeUserId) {
        return toDTO(payRouteDomainService.routeCreditForPay(
                businessSceneCode,
                paymentMethod,
                payerUserId,
                payeeUserId
        ));
    }

    /**
     * 处理信用用于交易信息。
     */
    @Override
    public PayRouteDTO routeCreditForTrade(String businessDomainCode,
                                           String businessSceneCode,
                                           String paymentMethod,
                                           Long payerUserId,
                                           Long payeeUserId) {
        return toDTO(payRouteDomainService.routeCreditForTrade(
                businessDomainCode,
                businessSceneCode,
                paymentMethod,
                payerUserId,
                payeeUserId
        ));
    }

    /**
     * 处理TCCTRY信用信息。
     */
    @Override
    public void tccTryCredit(String xid,
                             String branchId,
                             String accountNo,
                             String operationType,
                             String assetCategory,
                             Money amount,
                             String businessNo) {
        if (isLoanAccountNo(accountNo)) {
            loanRouteAccountPort.tccTry(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
            return;
        }
        creditRouteAccountPort.tccTry(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccConfirmCredit(String xid, String branchId, String accountNo) {
        if (isLoanAccountNo(accountNo)) {
            loanRouteAccountPort.tccConfirm(xid, branchId);
            return;
        }
        creditRouteAccountPort.tccConfirm(xid, branchId);
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccCancelCredit(String xid,
                                String branchId,
                                String accountNo,
                                String operationType,
                                String assetCategory,
                                Money amount,
                                String businessNo) {
        if (isLoanAccountNo(accountNo)) {
            loanRouteAccountPort.tccCancel(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
            return;
        }
        creditRouteAccountPort.tccCancel(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
    }

    private PayRouteDTO toDTO(CreditPayRoutePlan plan) {
        if (plan == null) {
            return null;
        }
        return new PayRouteDTO(
                plan.businessDomainCode(),
                plan.productCode(),
                plan.accountModuleCode(),
                plan.accountTypeCode(),
                plan.accountOwnerUserId(),
                resolveAccountNo(plan),
                plan.operationType(),
                plan.assetCategory()
        );
    }

    private String resolveAccountNo(CreditPayRoutePlan plan) {
        if (ACCOUNT_MODULE_LOAN.equals(plan.accountModuleCode())) {
            return loanRouteAccountPort.getAccountNoByUserId(plan.accountOwnerUserId());
        }
        CreditAccountType accountType = CreditAccountType.valueOf(normalizeRequired(plan.accountTypeCode(), "accountTypeCode"));
        return creditRouteAccountPort.getAccountNoByUserId(plan.accountOwnerUserId(), accountType);
    }

    private boolean isLoanAccountNo(String accountNo) {
        return CreditAccountType.fromAccountNo(normalizeRequired(accountNo, "accountNo")) == CreditAccountType.LOAN_ACCOUNT;
    }

    private String normalizeRequired(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return rawValue.trim();
    }
}
