package cn.openaipay.infrastructure.message.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.message.dataobject.MessageRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecordDO> {

    /**
     * 按消息ID查找记录。
     */
    default Optional<MessageRecordDO> findByMessageId(String messageId) {
        QueryWrapper<MessageRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("message_id", messageId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按会话查询记录列表。
     */
    default List<MessageRecordDO> listByConversation(String conversationNo, Long beforeId, int limit) {
        QueryWrapper<MessageRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_no", conversationNo);
        if (beforeId != null && beforeId > 0) {
            wrapper.lt("id", beforeId);
        }
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 查询红包记录列表。
     *
     * @param userId 用户ID
     * @param direction 记录方向：SENT 或 RECEIVED
     * @param year 年份过滤
     * @param limit 最大返回条数
     * @return 红包消息实体列表
     */
    default List<MessageRecordDO> listRedPacketHistory(Long userId, String direction, Integer year, int limit) {
        QueryWrapper<MessageRecordDO> wrapper = buildRedPacketHistoryWrapper(userId, direction, year);
        wrapper.orderByDesc("created_at", "id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 统计红包记录数量。
     *
     * @param userId 用户ID
     * @param direction 记录方向
     * @param year 年份过滤
     * @return 记录数量
     */
    default long countRedPacketHistory(Long userId, String direction, Integer year) {
        return selectCount(buildRedPacketHistoryWrapper(userId, direction, year));
    }

    /**
     * 汇总红包记录金额。
     *
     * @param userId 用户ID
     * @param direction 记录方向
     * @param year 年份过滤
     * @return 汇总金额
     */
    default Money sumRedPacketHistoryAmount(Long userId, String direction, Integer year) {
        QueryWrapper<MessageRecordDO> wrapper = buildRedPacketHistoryWrapper(userId, direction, year);
        wrapper.select("amount");
        List<MessageRecordDO> records = selectList(wrapper);
        Money total = Money.zero(CurrencyUnit.of("CNY"));
        for (MessageRecordDO record : records) {
            if (record.getAmount() == null) {
                continue;
            }
            total = total.plus(record.getAmount());
        }
        return total;
    }

    /**
     * 构建红包记录查询条件。
     *
     * @param userId 用户ID
     * @param direction 记录方向
     * @param year 年份过滤
     * @return 查询条件
     */
    private QueryWrapper<MessageRecordDO> buildRedPacketHistoryWrapper(Long userId, String direction, Integer year) {
        QueryWrapper<MessageRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("message_type", "RED_PACKET");
        if ("RECEIVED".equalsIgnoreCase(direction)) {
            wrapper.eq("receiver_user_id", userId);
        } else {
            wrapper.eq("sender_user_id", userId);
        }
        if (year != null && year > 0) {
            LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDateTime end = start.plusYears(1);
            wrapper.ge("created_at", start);
            wrapper.lt("created_at", end);
        }
        return wrapper;
    }
}
