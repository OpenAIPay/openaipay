package cn.openaipay.application.creditaccount.facade;

import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import org.joda.money.Money;

/**
 * 信用账户门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CreditAccountFacade {

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
     * 按用户ID获取当前明细信息。
     */
    CreditCurrentBillDetailDTO getCurrentBillDetailByUserId(Long userId);

    /**
     * 按用户ID获取明细信息。
     */
    CreditCurrentBillDetailDTO getNextBillDetailByUserId(Long userId);

    /**
     * 按用户ID获取单号。
     */
    String getAccountNoByUserId(Long userId);

    /**
     * 按用户ID获取单号。
     */
    String getAccountNoByUserId(Long userId, CreditAccountType accountType);

    /** 执行 TCC Try 阶段。 */
    CreditTccBranchDTO tccTry(String xid,
                              String branchId,
                              String accountNo,
                              String operationType,
                              String assetCategory,
                              Money amount,
                              String businessNo);

    /**
     * 处理TCC信息。
     */
    CreditTccBranchDTO tccConfirm(String xid, String branchId);

    /** 执行 TCC Cancel 阶段。 */
    CreditTccBranchDTO tccCancel(String xid,
                                 String branchId,
                                 String accountNo,
                                 String operationType,
                                 String assetCategory,
                                 Money amount,
                                 String businessNo);
}
