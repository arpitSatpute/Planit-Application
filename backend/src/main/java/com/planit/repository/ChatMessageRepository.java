package com.planit.repository;

import com.planit.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    List<ChatMessage> findByReceiverIdAndReadFalse(String receiverId);

    List<ChatMessage> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(String senderId, String receiverId);

    long countByConversationIdAndReceiverIdAndReadFalse(String conversationId, String receiverId);
}
