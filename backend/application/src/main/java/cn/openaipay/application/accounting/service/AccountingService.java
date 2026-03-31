package cn.openaipay.application.accounting.service;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.accounting.command.ReverseAccountingVoucherCommand;
import cn.openaipay.application.accounting.command.SaveAccountingSubjectCommand;
import cn.openaipay.application.accounting.dto.AccountingEntryDTO;
import cn.openaipay.application.accounting.dto.AccountingEventDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectSyncResultDTO;
import cn.openaipay.application.accounting.dto.AccountingVoucherDTO;

import java.util.List;

/**
 * 会计应用服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingService {

    /**
     * 处理事件信息。
     */
    AccountingVoucherDTO acceptEvent(AcceptAccountingEventCommand command);

    /**
     * 获取事件信息。
     */
    AccountingEventDTO getEvent(String eventId);

    List<AccountingEventDTO> listEvents(String eventId,
                                        String eventType,
                                        String sourceBizType,
                                        String sourceBizNo,
                                        String bizOrderNo,
                                        String tradeOrderNo,
                                        String payOrderNo,
                                        String status,
                                        Integer limit);

    /**
     * 重试事件信息。
     */
    AccountingVoucherDTO retryEvent(String eventId);

    /**
     * 获取凭证信息。
     */
    AccountingVoucherDTO getVoucher(String voucherNo);

    List<AccountingVoucherDTO> listVouchers(String voucherNo,
                                            String sourceBizType,
                                            String sourceBizNo,
                                            String bizOrderNo,
                                            String tradeOrderNo,
                                            String payOrderNo,
                                            String status,
                                            Integer limit);

    List<AccountingEntryDTO> listEntries(String voucherNo,
                                         String subjectCode,
                                         String ownerType,
                                         Long ownerId,
                                         String bizOrderNo,
                                         String tradeOrderNo,
                                         String payOrderNo,
                                         String sourceBizType,
                                         String sourceBizNo,
                                         Integer limit);

    /**
     * 处理凭证信息。
     */
    AccountingVoucherDTO reverseVoucher(ReverseAccountingVoucherCommand command);

    /**
     * 查询科目信息列表。
     */
    List<AccountingSubjectDTO> listSubjects(Boolean enabled, String subjectType, Integer limit);

    /**
     * 保存科目信息。
     */
    AccountingSubjectDTO saveSubject(SaveAccountingSubjectCommand command);

    /**
     * 更新科目状态。
     */
    AccountingSubjectDTO updateSubjectStatus(String subjectCode, Boolean enabled);

    /**
     * 初始化科目信息。
     */
    AccountingSubjectSyncResultDTO initializeStandardSubjects();

    /**
     * 重置科目信息。
     */
    AccountingSubjectSyncResultDTO resetStandardSubjects();
}
