package cn.openaipay.application.creditaccount.service;

import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import org.joda.money.Money;

/**
 * 信用账户应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CreditAccountService {

    /**
     * 创建信用信息。
     */
    String createCreditAccount(CreateCreditAccountCommand command);

    /**
     * 获取信用信息。
     */
    CreditAccountDTO getCreditAccount(String accountNo);

    /**
     * 按用户ID获取信用信息。
     */
    CreditAccountDTO getCreditAccountByUserId(Long userId);

    /**
     * 按用户ID获取信用信息。
     */
    CreditAccountDTO getCreditAccountByUserId(Long userId, CreditAccountType accountType);

    /** 按用户ID查询或创建信用账户。 */
    CreditAccountDTO getOrCreateCreditAccountByUserId(Long userId,
                                                      CreditAccountType accountType,
                                                      Money totalLimit,
                                                      Integer repayDayOfMonth);

    /**
     * 查询爱花当前账单明细页数据。
     *
     * 业务场景：爱花总计账单页点击右上角“明细”后，
     * 客户端需要一次性拿到顶部摘要与第一页可见账单消费列表。
     */
    CreditCurrentBillDetailDTO getCurrentBillDetailByUserId(Long userId);

    /**
     * 查询爱花下期账单明细页数据。
     *
     * 业务场景：爱花“3月总计账单”页中点击“4月账单累计中(元)”后，
     * 客户端需要查看 4 月即将出账账单的顶部摘要与 3 月消费列表。
     */
    CreditCurrentBillDetailDTO getNextBillDetailByUserId(Long userId);

    /**
     * 处理TCCTRY信息。
     */
    CreditTccBranchDTO tccTry(CreditTccTryCommand command);

    /**
     * 处理TCC信息。
     */
    CreditTccBranchDTO tccConfirm(CreditTccConfirmCommand command);

    /**
     * 处理TCC信息。
     */
    CreditTccBranchDTO tccCancel(CreditTccCancelCommand command);
}
