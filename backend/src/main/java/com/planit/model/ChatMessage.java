package com.planit.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private String senderId;

    private String receiverId;
    private String bookingId;

    private String type;    // TEXT, IMAGE, DOCUMENT
    private String content;
    private String fileUrl;

    @Indexed
    private boolean read;
    private LocalDateTime readAt;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;
}
