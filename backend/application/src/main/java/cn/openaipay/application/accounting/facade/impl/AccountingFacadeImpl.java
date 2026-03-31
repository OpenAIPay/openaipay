package cn.openaipay.application.accounting.facade.impl;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.accounting.command.ReverseAccountingVoucherCommand;
import cn.openaipay.application.accounting.command.SaveAccountingSubjectCommand;
import cn.openaipay.application.accounting.dto.AccountingEntryDTO;
import cn.openaipay.application.accounting.dto.AccountingEventDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectSyncResultDTO;
import cn.openaipay.application.accounting.dto.AccountingVoucherDTO;
import cn.openaipay.application.accounting.facade.AccountingFacade;
import cn.openaipay.application.accounting.service.AccountingService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会计门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class AccountingFacadeImpl implements AccountingFacade {

    /** 核算application服务信息 */
    private final AccountingService accountingService;

    public AccountingFacadeImpl(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    /**
     * 处理事件信息。
     */
    @Override
    public AccountingVoucherDTO acceptEvent(AcceptAccountingEventCommand command) {
        return accountingService.acceptEvent(command);
    }

    /**
     * 获取事件信息。
     */
    @Override
    public AccountingEventDTO getEvent(String eventId) {
        return accountingService.getEvent(eventId);
    }

    /**
     * 查询事件信息列表。
     */
    @Override
    public List<AccountingEventDTO> listEvents(String eventId,
                                               String eventType,
                                               String sourceBizType,
                                               String sourceBizNo,
                                               String bizOrderNo,
                                               String tradeOrderNo,
                                               String payOrderNo,
                                               String status,
                                               Integer limit) {
        return accountingService.listEvents(
                eventId,
                eventType,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                status,
                limit
        );
    }

    /**
     * 重试事件信息。
     */
    @Override
    public AccountingVoucherDTO retryEvent(String eventId) {
        return accountingService.retryEvent(eventId);
    }

    /**
     * 获取凭证信息。
     */
    @Override
    public AccountingVoucherDTO getVoucher(String voucherNo) {
        return accountingService.getVoucher(voucherNo);
    }

    /**
     * 查询凭证信息列表。
     */
    @Override
    public List<AccountingVoucherDTO> listVouchers(String voucherNo,
                                                   String sourceBizType,
                                                   String sourceBizNo,
                                                   String bizOrderNo,
                                                   String tradeOrderNo,
                                                   String payOrderNo,
                                                   String status,
                                                   Integer limit) {
        return accountingService.listVouchers(
                voucherNo,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                status,
                limit
        );
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    public List<AccountingEntryDTO> listEntries(String voucherNo,
                                                String subjectCode,
                                                String ownerType,
                                                Long ownerId,
                                                String bizOrderNo,
                                                String tradeOrderNo,
                                                String payOrderNo,
                                                String sourceBizType,
                                                String sourceBizNo,
                                                Integer limit) {
        return accountingService.listEntries(
                voucherNo,
                subjectCode,
                ownerType,
                ownerId,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                sourceBizType,
                sourceBizNo,
                limit
        );
    }

    /**
     * 处理凭证信息。
     */
    @Override
    public AccountingVoucherDTO reverseVoucher(ReverseAccountingVoucherCommand command) {
        return accountingService.reverseVoucher(command);
    }

    /**
     * 查询科目信息列表。
     */
    @Override
    public List<AccountingSubjectDTO> listSubjects(Boolean enabled, String subjectType, Integer limit) {
        return accountingService.listSubjects(enabled, subjectType, limit);
    }

    /**
     * 保存科目信息。
     */
    @Override
    public AccountingSubjectDTO saveSubject(SaveAccountingSubjectCommand command) {
        return accountingService.saveSubject(command);
    }

    /**
     * 更新科目状态。
     */
    @Override
    public AccountingSubjectDTO updateSubjectStatus(String subjectCode, Boolean enabled) {
        return accountingService.updateSubjectStatus(subjectCode, enabled);
    }

    /**
     * 初始化科目信息。
     */
    @Override
    public AccountingSubjectSyncResultDTO initializeStandardSubjects() {
        return accountingService.initializeStandardSubjects();
    }

    /**
     * 重置科目信息。
     */
    @Override
    public AccountingSubjectSyncResultDTO resetStandardSubjects() {
        return accountingService.resetStandardSubjects();
    }
}
