package com.planit.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;

    private EventDetailsRequest eventDetails;
    private LocationRequest location;

    @Data
    public static class EventDetailsRequest {
        private String eventName;
        private String eventType;
        private int guestCount;
        private String specialRequests;
    }

    @Data
    public static class LocationRequest {
        @NotBlank(message = "Address is required")
        private String address;
        private String city;
        private String state;
        private double latitude;
        private double longitude;
    }
}
