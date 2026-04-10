package com.planit.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String subcategory;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    private List<ImageRequest> images;

    private SpecsRequest specifications;

    @NotNull(message = "Pricing model is required")
    private PricingRequest pricingModel;

    private InventoryRequest inventory;

    private List<String> tags;

    @Data
    public static class ImageRequest {
        private String url;
        private int order;
        private boolean isPrimary;
    }

    @Data
    public static class SpecsRequest {
        private String capacity;
        private String dimensions;
        private String material;
        private List<String> features;
    }

    @Data
    public static class PricingRequest {
        @NotBlank
        private String type;
        @Min(0)
        private long basePrice;
        private String currency = "INR";
        private List<TierRequest> tiers;
        private long securityDeposit;
        @Min(0) @Max(100)
        private int advancePayment;

        @Data
        public static class TierRequest {
            private String duration;
            private long price;
            private int minHours;
        }
    }

    @Data
    public static class InventoryRequest {
        @Min(1)
        private int totalQuantity;
        private boolean trackInventory = true;
    }
}
