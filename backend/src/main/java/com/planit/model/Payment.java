package com.planit.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String userId;

    @Indexed
    private String vendorId;

    private long amount;     // in paise
    private String currency;
    private String type;     // BOOKING_PAYMENT, REFUND, PAYOUT
    private String gateway;  // RAZORPAY, STRIPE, WALLET

    private GatewayResponse gatewayResponse;

    @Indexed
    private PaymentStatus status = PaymentStatus.PENDING;

    private String failureReason;
    private PaymentMetadata metadata;
    private Payout payout;
    private Refund refund;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GatewayResponse {
        private String orderId;
        private String paymentId;
        private String signature;
        private String method;  // upi, card, netbanking, wallet
        private String vpa;     // UPI VPA
        private String bank;
        private String wallet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMetadata {
        private String ip;
        private String userAgent;
        private String deviceType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payout {
        private String payoutId;
        private String accountNumber;
        private String ifscCode;
        private long payoutAmount;
        private long platformFee;
        private long paymentGatewayFee;
        private long vendorEarnings;
        private String payoutStatus; // PENDING, PROCESSING, COMPLETED, FAILED
        private LocalDateTime payoutDate;
        private LocalDateTime processedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Refund {
        private String refundId;
        private long refundAmount;
        private String refundStatus;
        private LocalDateTime refundedAt;
        private String reason;
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
