package com.planit.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String type;    // BOOKING_CONFIRMED, PAYMENT_RECEIVED, etc.
    private String channel; // PUSH, SMS, EMAIL
    private String title;
    private String body;

    private Map<String, String> data; // additional payload

    private boolean read;
    private LocalDateTime readAt;

    private String status;  // PENDING, SENT, FAILED

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;
}
