package com.planit.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed
    private String vendorId;

    private String name;

    @Indexed(unique = true)
    private String slug;

    @Indexed
    private String category;    // TENTS, FURNITURE, AUDIO_VISUAL, CATERING, etc.
    private String subcategory;

    private String description;

    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    private Specifications specifications;
    private PricingModel pricingModel;
    private Inventory inventory;
    private AvailabilityConfig availability;
    private ProductLocation location;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private Ratings ratings;
    private Stats stats;

    @Indexed
    private ProductStatus status = ProductStatus.DRAFT;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ---- Embedded classes ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImage {
        private String url;
        private String thumbnail;
        private boolean isPrimary;
        private int order;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Specifications {
        private String capacity;
        private String dimensions;
        private String material;
        @Builder.Default
        private List<String> features = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingModel {
        private String type;        // DURATION_BASED, FIXED, CUSTOM
        private long basePrice;     // in paise
        private String currency;
        @Builder.Default
        private List<PricingTier> tiers = new ArrayList<>();
        private long securityDeposit;
        private int advancePayment; // percentage

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PricingTier {
            private String duration;  // HOURLY, HALF_DAY, FULL_DAY, OVERNIGHT, WEEKLY, MONTHLY
            private long price;       // in paise
            private int minHours;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Inventory {
        private int totalQuantity;
        private int availableQuantity;
        private boolean trackInventory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilityConfig {
        private boolean isAvailable;
        private int bufferTime;        // minutes
        private int advanceBookingDays;
        private int minBookingDays;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductLocation {
        private String address;
        private String city;
        private String state;

        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
        private User.GeoPoint location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Ratings {
        private double average;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stats {
        private int totalBookings;
        private int viewCount;
        private int favoriteCount;
    }

    public enum ProductStatus {
        DRAFT, PUBLISHED, OUT_OF_STOCK, ARCHIVED
    }
}
