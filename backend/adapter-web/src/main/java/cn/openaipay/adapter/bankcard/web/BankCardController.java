package cn.openaipay.adapter.bankcard.web;

import cn.openaipay.adapter.bankcard.web.request.BindBankCardRequest;
import cn.openaipay.adapter.bankcard.web.request.SetDefaultBankCardRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.bankcard.command.BindBankCardCommand;
import cn.openaipay.application.bankcard.command.SetDefaultBankCardCommand;
import cn.openaipay.application.bankcard.dto.BankCardDTO;
import cn.openaipay.application.bankcard.facade.BankCardFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 银行卡控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/bankcards")
public class BankCardController {

    /** BankCardFacade组件 */
    private final BankCardFacade bankCardFacade;

    public BankCardController(BankCardFacade bankCardFacade) {
        this.bankCardFacade = bankCardFacade;
    }

    /**
     * 处理银行信息。
     */
    @PostMapping
    public ApiResponse<BankCardDTO> bindBankCard(@Valid @RequestBody BindBankCardRequest request) {
        BankCardDTO bankCard = bankCardFacade.bindBankCard(new BindBankCardCommand(
                request.userId(),
                request.cardNo(),
                request.bankCode(),
                request.bankName(),
                request.cardType(),
                request.cardHolderName(),
                request.reservedMobile(),
                request.phoneTailNo(),
                request.defaultCard(),
                request.singleLimit(),
                request.dailyLimit()
        ));
        return ApiResponse.success(bankCard);
    }

    /**
     * 处理SET银行信息。
     */
    @PostMapping("/default")
    public ApiResponse<BankCardDTO> setDefaultBankCard(@Valid @RequestBody SetDefaultBankCardRequest request) {
        BankCardDTO bankCard = bankCardFacade.setDefaultBankCard(new SetDefaultBankCardCommand(
                request.userId(),
                request.cardNo()
        ));
        return ApiResponse.success(bankCard);
    }

    /**
     * 查询用户银行信息列表。
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<BankCardDTO>> listUserBankCards(@PathVariable("userId") Long userId) {
        return ApiResponse.success(bankCardFacade.listUserBankCards(userId));
    }

    /**
     * 查询用户银行信息列表。
     */
    @GetMapping("/users/{userId}/active")
    public ApiResponse<List<BankCardDTO>> listUserActiveBankCards(@PathVariable("userId") Long userId) {
        return ApiResponse.success(bankCardFacade.listUserActiveBankCards(userId));
    }
}
