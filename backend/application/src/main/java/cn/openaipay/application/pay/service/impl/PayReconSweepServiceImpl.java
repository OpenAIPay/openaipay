package cn.openaipay.application.pay.service.impl;

import cn.openaipay.application.pay.service.PayReconSweepService;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付待对账主单兜底续跑服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class PayReconSweepServiceImpl implements PayReconSweepService {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(PayReconSweepServiceImpl.class);

    /** 支付订单信息 */
    private final PayOrderRepository payOrderRepository;
    /** 支付信息 */
    private final PayServiceImpl payService;

    public PayReconSweepServiceImpl(PayOrderRepository payOrderRepository,
                                               PayServiceImpl payService) {
        this.payOrderRepository = payOrderRepository;
        this.payService = payService;
    }

    /**
     * 扫描处理业务数据。
     */
    @Override
    public int sweepReconPendingPayments(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        List<PayOrder> pendingOrders = payOrderRepository.findReconPendingOrders(normalizedLimit);
        int handledCount = 0;
        for (PayOrder pendingOrder : pendingOrders) {
            String payOrderNo = pendingOrder.getPayOrderNo();
            try {
                payService.reconcilePendingPayment(payOrderNo);
                handledCount++;
            } catch (RuntimeException ex) {
                String error = compactError(ex.getMessage());
                log.warn("pay recon sweep failed, payOrderNo={}, error={}", payOrderNo, error);
            }
        }
        return handledCount;
    }

    private String compactError(String message) {
        if (message == null) {
            return "unknown error";
        }
        String compacted = message.replace('\n', ' ').replace('\r', ' ').trim();
        if (compacted.isEmpty()) {
            return "unknown error";
        }
        return compacted.length() > 240 ? compacted.substring(0, 240) : compacted;
    }
}
