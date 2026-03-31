package cn.openaipay.domain.message.repository;

import cn.openaipay.domain.message.model.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.joda.money.Money;

/**
 * 消息仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface MessageRepository {

    /**
     * 按消息ID查找记录。
     */
    Optional<ChatMessage> findByMessageId(String messageId);

    /**
     * 保存业务数据。
     */
    ChatMessage save(ChatMessage message);

    /**
     * 查询RED红包历史信息列表。
     */
    List<ChatMessage> listRedPacketHistory(Long userId, String direction, Integer year, int limit);

    /**
     * 处理数量RED红包历史信息。
     */
    long countRedPacketHistory(Long userId, String direction, Integer year);

    /**
     * 汇总RED红包历史金额。
     */
    Money sumRedPacketHistoryAmount(Long userId, String direction, Integer year);

    /**
     * 按会话查询记录列表。
     */
    List<ChatMessage> listByConversation(String conversationNo, String beforeMessageId, int limit);
}
