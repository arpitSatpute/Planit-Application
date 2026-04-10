package com.planit.service;

import com.planit.exception.ResourceNotFoundException;
import com.planit.model.ChatMessage;
import com.planit.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage sendMessage(String senderId, String receiverId,
                                   String conversationId, String bookingId,
                                   String type, String content) {
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
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

    public Page<ChatMessage> getConversationMessages(String conversationId, int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "createdAt"));
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
    }

    public void markAsRead(String conversationId, String userId) {
        List<ChatMessage> unread = chatMessageRepository.findByReceiverIdAndReadFalse(userId);
        unread.forEach(msg -> {
            if (msg.getConversationId().equals(conversationId)) {
                msg.setRead(true);
                msg.setReadAt(LocalDateTime.now());
            }
        });
        chatMessageRepository.saveAll(unread);
    }

    public long getUnreadCount(String conversationId, String userId) {
        return chatMessageRepository.countByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);
    }
}
