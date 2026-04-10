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
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingNumber;  // Human-readable ID: BKG20260410-0001

    @Indexed
    private String userId;

    @Indexed
    private String vendorId;

    @Indexed
    private String productId;

    private String bookingType; // RENTAL, SERVICE, PACKAGE

    private EventDetails eventDetails;
    private Schedule schedule;
    private BookingLocation location;
    private Pricing pricing;
    private PaymentInfo payment;

    @Indexed
    private BookingStatus status = BookingStatus.PENDING;

    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();

    private Cancellation cancellation;
    private Communication communication;
    private ReviewInfo review;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ---- Embedded classes ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventDetails {
        private String eventName;
        private String eventType; // WEDDING, CORPORATE, BIRTHDAY, etc.
        private int guestCount;
        private String specialRequests;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Schedule {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int duration;    // hours
        private String timeSlot; // HOURLY, HALF_DAY, FULL_DAY, OVERNIGHT
        private LocalDateTime setupTime;
        private LocalDateTime teardownTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingLocation {
        private String address;
        private String city;
        private String state;
        private User.GeoPoint location;
        private boolean deliveryRequired;
        private long deliveryCharge;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Pricing {
        private long basePrice;
        private int quantity;
        private long subtotal;
        private long deliveryCharge;
        private Tax tax;
        private long platformFee;
        private long totalAmount;
        private long securityDeposit;
        private long advancePayment;
        private String currency;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Tax {
            private long gst;
            private long serviceTax;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String paymentId;
        private String method; // RAZORPAY, STRIPE, WALLET
        private String status; // PENDING, COMPLETED, FAILED, REFUNDED
        private LocalDateTime paidAt;
        private String transactionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistory {
        private BookingStatus status;
        private LocalDateTime timestamp;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Cancellation {
        private String cancelledBy; // USER, VENDOR, ADMIN
        private LocalDateTime cancelledAt;
        private String reason;
        private long refundAmount;
        private String refundStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Communication {
        private String conversationId;
        private LocalDateTime lastMessageAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewInfo {
        private String reviewId;
        private boolean reviewed;
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
