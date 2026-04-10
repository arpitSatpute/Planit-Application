package com.planit.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String userId;

    @Indexed
    private String vendorId;

    @Indexed
    private String productId;

    private int rating; // 1-5

    private String title;
    private String comment;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private AspectRatings aspectRatings;
    private VendorResponse vendorResponse;
    private Helpful helpful;

    @Indexed
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AspectRatings {
        private int quality;
        private int value;
        private int service;
        private int communication;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorResponse {
        private String comment;
        private LocalDateTime respondedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Helpful {
        private int count;
        @Builder.Default
        private List<String> users = new ArrayList<>(); // user IDs
    }

    public enum ReviewStatus {
        PENDING, PUBLISHED, FLAGGED, REMOVED
    }
}
