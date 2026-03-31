package cn.openaipay.domain.contact.service.impl;

import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import cn.openaipay.domain.contact.service.ContactDomainService;
import cn.openaipay.domain.contact.service.ContactRequestHandlePlan;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 联系人领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class ContactDomainServiceImpl implements ContactDomainService {

    /**
     * 处理请求。
     */
    @Override
    public ContactRequest prepareFriendRequest(Long requesterUserId,
                                               Long targetUserId,
                                               boolean blockedByEither,
                                               boolean alreadyFriend,
                                               ContactRequest existingPendingRequest,
                                               String applyMessage,
                                               LocalDateTime now) {
        Long requester = requirePositive(requesterUserId, "requesterUserId");
        Long target = requirePositive(targetUserId, "targetUserId");
        if (requester.equals(target)) {
            throw new IllegalArgumentException("requesterUserId and targetUserId must be different");
        }
        if (blockedByEither) {
            throw new IllegalStateException("target user can not be added now");
        }
        if (alreadyFriend) {
            throw new IllegalStateException("already friend");
        }
        if (existingPendingRequest != null) {
            return existingPendingRequest;
        }
        return ContactRequest.create(buildRequestNo(now), requester, target, applyMessage, now);
    }

    /**
     * 处理请求。
     */
    @Override
    public ContactRequestHandlePlan handleRequest(ContactRequest request,
                                                  String action,
                                                  Long operatorUserId,
                                                  LocalDateTime now) {
        ContactRequest targetRequest = requireRequest(request);
        Long operator = requirePositive(operatorUserId, "operatorUserId");
        LocalDateTime handledAt = now == null ? LocalDateTime.now() : now;
        String normalizedAction = normalizeRequired(action, "action").toUpperCase(Locale.ROOT);
        return switch (normalizedAction) {
            case "ACCEPT" -> {
                ensureOperator(operator, targetRequest.getTargetUserId(), "targetUserId");
                targetRequest.accept(operator, handledAt);
                yield new ContactRequestHandlePlan(
                        targetRequest,
                        List.of(
                                ContactFriendship.create(
                                        targetRequest.getRequesterUserId(),
                                        targetRequest.getTargetUserId(),
                                        targetRequest.getRequestNo(),
                                        handledAt
                                ),
                                ContactFriendship.create(
                                        targetRequest.getTargetUserId(),
                                        targetRequest.getRequesterUserId(),
                                        targetRequest.getRequestNo(),
                                        handledAt
                                )
                        )
                );
            }
            case "REJECT" -> {
                ensureOperator(operator, targetRequest.getTargetUserId(), "targetUserId");
                targetRequest.reject(operator, handledAt);
                yield new ContactRequestHandlePlan(targetRequest, List.of());
            }
            case "CANCEL" -> {
                ensureOperator(operator, targetRequest.getRequesterUserId(), "requesterUserId");
                targetRequest.cancel(operator, handledAt);
                yield new ContactRequestHandlePlan(targetRequest, List.of());
            }
            default -> throw new IllegalArgumentException("action must be ACCEPT/REJECT/CANCEL");
        };
    }

    /**
     * 取消请求用于信息。
     */
    @Override
    public void cancelPendingRequestForBlock(ContactRequest request, Long operatorUserId, LocalDateTime now) {
        requireRequest(request).cancel(requirePositive(operatorUserId, "operatorUserId"), now);
    }

    private String buildRequestNo(LocalDateTime now) {
        long timestamp = (now == null ? LocalDateTime.now() : now)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return "CTRQ" + timestamp
                + String.format(Locale.ROOT, "%03d", ThreadLocalRandom.current().nextInt(0, 1000));
    }

    private ContactRequest requireRequest(ContactRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        return request;
    }

    private void ensureOperator(Long operatorUserId, Long expectedUserId, String fieldName) {
        if (!operatorUserId.equals(expectedUserId)) {
            throw new IllegalArgumentException(fieldName + " mismatch");
        }
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }
}
