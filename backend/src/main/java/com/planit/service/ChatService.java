package com.planit.service;

import com.planit.dto.response.ConversationSummaryResponse;
import com.planit.model.ChatMessage;
import com.planit.model.User;
import com.planit.model.Vendor;
import com.planit.repository.ChatMessageRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage sendMessage(String senderId, String receiverId,
                                   String conversationId, String bookingId,
                                   String type, String content) {
        String resolvedConversationId = normalizeConversationId(conversationId, senderId, receiverId);

        ChatMessage message = ChatMessage.builder()
                .conversationId(resolvedConversationId)
                .senderId(senderId)
                .receiverId(receiverId)
                .bookingId(bookingId)
                .type(type != null ? type : "TEXT")
                .content(content)
                .read(false)
                .build();

        message = chatMessageRepository.save(message);

        // Send via WebSocket to receiver
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                message
        );

        log.debug("Message sent from {} to {}", senderId, receiverId);
        return message;
    }

    public List<ConversationSummaryResponse> getConversations(String userId) {
        List<ChatMessage> messages = chatMessageRepository
                .findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);

        Map<String, ChatMessage> latestByConversation = new LinkedHashMap<>();
        for (ChatMessage message : messages) {
            String conversationId = normalizeConversationId(
                    message.getConversationId(),
                    message.getSenderId(),
                    message.getReceiverId()
            );
            latestByConversation.putIfAbsent(conversationId, message);
        }

        return latestByConversation.entrySet().stream()
                .map(entry -> toConversationSummary(entry.getKey(), entry.getValue(), userId))
                .sorted(Comparator.comparing(ConversationSummaryResponse::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Page<ChatMessage> getConversationMessages(String conversationId, int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "createdAt"));
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(
                normalizeConversationId(conversationId, null, null), pageable);
    }

    public void markAsRead(String conversationId, String userId) {
        String resolvedConversationId = normalizeConversationId(conversationId, null, null);
        List<ChatMessage> unread = chatMessageRepository.findByReceiverIdAndReadFalse(userId);
        unread.forEach(msg -> {
            if (Objects.equals(msg.getConversationId(), resolvedConversationId)) {
                msg.setRead(true);
                msg.setReadAt(LocalDateTime.now());
            }
        });
        chatMessageRepository.saveAll(unread);
    }

    public long getUnreadCount(String conversationId, String userId) {
        return chatMessageRepository.countByConversationIdAndReceiverIdAndReadFalse(
                normalizeConversationId(conversationId, null, null), userId);
    }

    private ConversationSummaryResponse toConversationSummary(
            String conversationId, ChatMessage latestMessage, String currentUserId) {
        String participantId = resolveParticipantId(latestMessage, currentUserId);
        String participantName = resolveParticipantName(participantId);

        return ConversationSummaryResponse.builder()
                .id(conversationId)
                .participantId(participantId)
                .participantName(participantName)
                .lastMessage(latestMessage.getContent())
                .updatedAt(latestMessage.getCreatedAt())
                .unreadCount(getUnreadCount(conversationId, currentUserId))
                .build();
    }

    private String resolveParticipantId(ChatMessage message, String currentUserId) {
        if (currentUserId.equals(message.getSenderId())) {
            return message.getReceiverId();
        }
        return message.getSenderId();
    }

    private String resolveParticipantName(String participantId) {
        if (participantId == null || participantId.isBlank()) {
            return "Unknown User";
        }

        User user = userRepository.findById(participantId).orElse(null);
        if (user != null && user.getProfile() != null) {
            String fullName = (user.getProfile().getFirstName() + " " + user.getProfile().getLastName()).trim();
            if (!fullName.isBlank()) {
                return fullName;
            }
        }

        Vendor vendor = vendorRepository.findByUserId(participantId).orElse(null);
        if (vendor != null && vendor.getBusinessName() != null && !vendor.getBusinessName().isBlank()) {
            return vendor.getBusinessName();
        }

        return "User " + participantId;
    }

    private String normalizeConversationId(String conversationId, String senderId, String receiverId) {
        if (conversationId != null && !conversationId.isBlank()) {
            return conversationId;
        }
        if (senderId == null || receiverId == null) {
            return conversationId;
        }
        return senderId.compareTo(receiverId) < 0
                ? "conv-" + senderId + "-" + receiverId
                : "conv-" + receiverId + "-" + senderId;
    }
}
