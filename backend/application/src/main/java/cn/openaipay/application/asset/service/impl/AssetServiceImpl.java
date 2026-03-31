package cn.openaipay.application.asset.service.impl;

import cn.openaipay.application.asset.dto.AssetOverviewDTO;
import cn.openaipay.application.asset.service.AssetService;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
/**
 * 资产应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class AssetServiceImpl implements AssetService {

    /** 钱包账户门面组件 */
    private final WalletAccountFacade walletAccountFacade;

    public AssetServiceImpl(WalletAccountFacade walletAccountFacade) {
        this.walletAccountFacade = walletAccountFacade;
    }

    /**
     * 查询用户资源概览信息。
     */
    @Override
    public AssetOverviewDTO queryUserAssetOverview(Long userId) {
        Long normalizedUserId = requirePositive(userId, "userId");
        WalletAccountDTO walletAccount = walletAccountFacade.getOrCreateWalletAccount(normalizedUserId, "CNY");

        BigDecimal available = normalizeAmount(walletAccount.availableBalance());
        BigDecimal reserved = normalizeAmount(walletAccount.reservedBalance());
        BigDecimal total = available.add(reserved).setScale(2, RoundingMode.HALF_UP);

        return new AssetOverviewDTO(
                Long.toString(normalizedUserId),
                walletAccount.currencyCode(),
                available,
                reserved,
                total,
                walletAccount.accountStatus(),
                LocalDateTime.now()
        );
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private BigDecimal normalizeAmount(Money money) {
        if (money == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return money.getAmount().setScale(2, RoundingMode.HALF_UP);
    }
}
