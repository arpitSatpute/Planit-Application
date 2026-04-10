package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.PagedResponse;
import com.planit.model.ChatMessage;
import com.planit.security.UserPrincipal;
import com.planit.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** REST endpoint to send a message */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, String> body) {
        ChatMessage message = chatService.sendMessage(
                currentUser.getId(),
                body.get("receiverId"),
                body.get("conversationId"),
                body.get("bookingId"),
                body.getOrDefault("type", "TEXT"),
                body.get("content")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message));
    }

    /** REST endpoint to get conversation messages */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<PagedResponse<ChatMessage>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<ChatMessage> messages = chatService.getConversationMessages(conversationId, page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(messages.getContent(), page, pageSize, messages.getTotalElements()));
    }

    /** REST endpoint to mark all messages in conversation as read */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String conversationId) {
        chatService.markAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    /** WebSocket endpoint - client sends to /app/chat.send */
    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(@Payload Map<String, String> payload, Principal principal) {
        chatService.sendMessage(
                principal.getName(),
                payload.get("receiverId"),
                payload.get("conversationId"),
                payload.get("bookingId"),
                payload.getOrDefault("type", "TEXT"),
                payload.get("content")
        );
    }
}
