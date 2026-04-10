package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.model.Payment;
import com.planit.security.UserPrincipal;
import com.planit.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<Payment>> createOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, String> body) {
        String bookingId = body.get("bookingId");
        String gateway = body.getOrDefault("gateway", "RAZORPAY");
        Payment payment = paymentService.createOrder(currentUser.getId(), bookingId, gateway);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(
            @RequestBody Map<String, String> body) {
        String orderId = body.get("razorpay_order_id");
        String paymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");
        Payment payment = paymentService.verifyPayment(orderId, paymentId, signature);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment verified and booking confirmed"));
    }
}
