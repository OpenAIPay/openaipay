package cn.openaipay.infrastructure.contact.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.contact.dataobject.ContactRequestDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友申请持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface ContactRequestMapper extends BaseMapper<ContactRequestDO> {

    /**
     * 按请求单号查找记录。
     */
    default Optional<ContactRequestDO> findByRequestNo(String requestNo) {
        QueryWrapper<ContactRequestDO> wrapper = new QueryWrapper<>();
        wrapper.eq("request_no", requestNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 查找请求。
     */
    default Optional<ContactRequestDO> findPendingRequest(Long requesterUserId, Long targetUserId) {
        QueryWrapper<ContactRequestDO> wrapper = new QueryWrapper<>();
        wrapper.eq("requester_user_id", requesterUserId);
        wrapper.eq("target_user_id", targetUserId);
        wrapper.eq("status", "PENDING");
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 查询业务数据列表。
     */
    default List<ContactRequestDO> listReceived(Long targetUserId, int limit) {
        QueryWrapper<ContactRequestDO> wrapper = new QueryWrapper<>();
        wrapper.eq("target_user_id", targetUserId);
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 查询业务数据列表。
     */
    default List<ContactRequestDO> listSent(Long requesterUserId, int limit) {
        QueryWrapper<ContactRequestDO> wrapper = new QueryWrapper<>();
        wrapper.eq("requester_user_id", requesterUserId);
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }
}
