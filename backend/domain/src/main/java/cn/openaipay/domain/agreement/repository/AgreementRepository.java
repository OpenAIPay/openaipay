package cn.openaipay.domain.agreement.repository;

import cn.openaipay.domain.agreement.model.AgreementBizType;
import cn.openaipay.domain.agreement.model.AgreementSignItem;
import cn.openaipay.domain.agreement.model.AgreementSignRecord;
import cn.openaipay.domain.agreement.model.AgreementTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 协议仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface AgreementRepository {

    /**
     * 查询生效协议模板列表。
     */
    List<AgreementTemplate> listActiveTemplates(AgreementBizType bizType);

    /**
     * 按签约单号查询签约记录。
     */
    Optional<AgreementSignRecord> findSignRecordBySignNo(String signNo);

    /**
     * 按用户+业务+幂等键查询签约记录。
     */
    Optional<AgreementSignRecord> findSignRecordByIdempotencyKey(Long userId, AgreementBizType bizType, String idempotencyKey);

    /**
     * 保存签约记录。
     */
    AgreementSignRecord saveSignRecord(AgreementSignRecord signRecord);

    /**
     * 覆盖保存签约明细。
     */
    void replaceSignItems(String signNo, List<AgreementSignItem> signItems);

    /**
     * 查询签约明细。
     */
    List<AgreementSignItem> listSignItems(String signNo);
}
