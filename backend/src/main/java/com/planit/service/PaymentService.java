package com.planit.service;

import com.planit.exception.PaymentException;
import com.planit.exception.ResourceNotFoundException;
import com.planit.model.Booking;
import com.planit.model.Payment;
import com.planit.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Value("${app.payment.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.payment.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${app.payment.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    /**
     * Create a payment order for a booking.
     * In production this calls Razorpay API.
     */
    public Payment createOrder(String userId, String bookingId, String gateway) {
        Booking booking = bookingService.getBookingById(bookingId, userId);

        if (!booking.getStatus().equals(Booking.BookingStatus.PENDING)) {
            throw new PaymentException("Booking is not in PENDING state");
        }

        // Simulate order creation (replace with actual Razorpay SDK call in production)
        String simulatedOrderId = "order_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .vendorId(booking.getVendorId())
                .amount(booking.getPricing().getTotalAmount())
                .currency("INR")
                .type("BOOKING_PAYMENT")
                .gateway(gateway)
                .gatewayResponse(Payment.GatewayResponse.builder()
                        .orderId(simulatedOrderId)
                        .build())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment order created: {} for booking {}", payment.getId(), bookingId);
        return payment;
    }

    /**
     * Verify Razorpay payment signature and confirm.
     */
    public Payment verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        // Verify HMAC signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        boolean valid = verifyHmacSha256(payload, razorpaySignature, razorpayKeySecret);

        Payment payment = paymentRepository.findByGatewayResponse_PaymentId(razorpayOrderId)
                .orElseGet(() -> paymentRepository.findAll().stream()
                        .filter(p -> p.getGatewayResponse() != null &&
                                razorpayOrderId.equals(p.getGatewayResponse().getOrderId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", razorpayOrderId)));

        if (!valid) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Invalid payment signature");
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed");
        }

        // Update payment
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.getGatewayResponse().setPaymentId(razorpayPaymentId);
        payment.getGatewayResponse().setSignature(razorpaySignature);
        payment = paymentRepository.save(payment);

        // Confirm booking
        bookingService.confirmBooking(payment.getBookingId());

        log.info("Payment verified and booking confirmed: {}", payment.getBookingId());
        return payment;
    }

    private boolean verifyHmacSha256(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSig = HexFormat.of().formatHex(hash);
            return computedSig.equals(signature);
        } catch (Exception e) {
            log.error("HMAC verification error", e);
            return false;
        }
    }
}
