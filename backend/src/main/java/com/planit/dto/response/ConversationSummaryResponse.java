package com.planit.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationSummaryResponse {
    private String id;
    private String participantId;
    private String participantName;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private long unreadCount;
}

