package cn.openaipay.application.userflow.async;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 注册初始余额发放异步处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
@Component
public class RegisterInitialWalletTopUpRequestedHandler implements OutboxMessageHandler {

    /** 发放币种。 */
    private static final CurrencyUnit TOPUP_CURRENCY = CurrencyUnit.of("CNY");
    /** 发放金额。 */
    private static final Money TOPUP_AMOUNT = Money.of(TOPUP_CURRENCY, new BigDecimal("100000.00"));
    /** 钱包 TCC 操作类型。 */
    private static final String WALLET_TCC_OPERATION_CREDIT = "CREDIT";
    /** 钱包冻结类型。 */
    private static final String WALLET_FREEZE_TYPE_PROMO = "PROMO_HOLD";
    /** TCC xid 前缀。 */
    private static final String WALLET_TOPUP_XID_PREFIX = "REG_INIT_WALLET_TOPUP";
    /** TCC branchId 前缀。 */
    private static final String WALLET_TOPUP_BRANCH_PREFIX = "REG_INIT_WALLET_BRANCH";
    /** 业务单号前缀。 */
    private static final String WALLET_TOPUP_BIZ_NO_PREFIX = "REG_INIT_WALLET_BIZ";

    /** 钱包账户门面。 */
    private final WalletAccountFacade walletAccountFacade;

    public RegisterInitialWalletTopUpRequestedHandler(WalletAccountFacade walletAccountFacade) {
        this.walletAccountFacade = walletAccountFacade;
    }

    /**
     * 处理主题。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.USER_REGISTER_INITIAL_WALLET_TOPUP_REQUESTED;
    }

    /**
     * 处理注册余额发放。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        RegisterInitialWalletTopUpPayload payload = RegisterInitialWalletTopUpPayload.fromPayload(outboxMessage.getPayload());
        Long userId = parseUserId(payload.userId());
        String xid = WALLET_TOPUP_XID_PREFIX + "_" + userId;
        String branchId = WALLET_TOPUP_BRANCH_PREFIX + "_" + userId;
        String businessNo = resolveBusinessNo(outboxMessage.getMessageKey(), userId);

        walletAccountFacade.getOrCreateWalletAccount(userId, TOPUP_CURRENCY.getCode());
        walletAccountFacade.tccTry(
                xid,
                branchId,
                userId,
                WALLET_TCC_OPERATION_CREDIT,
                WALLET_FREEZE_TYPE_PROMO,
                TOPUP_AMOUNT,
                businessNo
        );
        walletAccountFacade.tccConfirm(xid, branchId);
    }

    private Long parseUserId(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            throw new IllegalArgumentException("register wallet top-up payload missing userId");
        }
        try {
            Long parsed = Long.parseLong(rawUserId.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("register wallet top-up payload userId must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("register wallet top-up payload userId is invalid");
        }
    }

    private String resolveBusinessNo(String messageKey, Long userId) {
        if (messageKey != null && !messageKey.isBlank()) {
            return messageKey.trim();
        }
        return WALLET_TOPUP_BIZ_NO_PREFIX + "_" + userId;
    }
}
