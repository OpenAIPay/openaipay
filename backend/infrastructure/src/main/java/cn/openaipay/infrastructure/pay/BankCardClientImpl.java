package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.bankcard.dto.BankCardDTO;
import cn.openaipay.application.bankcard.facade.BankCardFacade;
import cn.openaipay.domain.pay.client.BankCardClient;
import cn.openaipay.domain.pay.client.PayBankCardSnapshot;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * BankCardClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class BankCardClientImpl implements BankCardClient {

    /** 银行卡信息 */
    private final BankCardFacade bankCardFacade;

    public BankCardClientImpl(BankCardFacade bankCardFacade) {
        this.bankCardFacade = bankCardFacade;
    }

    /**
     * 查询用户银行信息列表。
     */
    @Override
    public List<PayBankCardSnapshot> listUserActiveBankCards(Long userId) {
        return bankCardFacade.listUserActiveBankCards(userId).stream().map(this::toSnapshot).toList();
    }

    private PayBankCardSnapshot toSnapshot(BankCardDTO bankCard) {
        return new PayBankCardSnapshot(
                bankCard.cardNo(),
                bankCard.maskedCardNo(),
                bankCard.userId(),
                bankCard.bankCode(),
                bankCard.bankName(),
                bankCard.cardType(),
                bankCard.cardHolderName(),
                bankCard.reservedMobile(),
                bankCard.phoneTailNo(),
                bankCard.cardStatus(),
                bankCard.defaultCard(),
                bankCard.singleLimit(),
                bankCard.dailyLimit()
        );
    }
}
