package com.planit.controller;

import com.planit.dto.request.CreateBookingRequest;
import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.PagedResponse;
import com.planit.model.Booking;
import com.planit.security.UserPrincipal;
import com.planit.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created. Complete payment to confirm."));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Booking>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Booking> bookings = bookingService.getUserBookings(currentUser.getId(), status, page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(bookings.getContent(), page, pageSize, bookings.getTotalElements()));
    }

    @GetMapping("/vendor")
    public ResponseEntity<PagedResponse<Booking>> getVendorBookings(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Booking> bookings = bookingService.getVendorBookings(currentUser.getId(), status, page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(bookings.getContent(), page, pageSize, bookings.getTotalElements()));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Booking>> getBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String bookingId) {
        Booking booking = bookingService.getBookingById(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String bookingId,
            @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "No reason provided");
        boolean requestRefund = (Boolean) body.getOrDefault("requestRefund", false);
        Booking booking = bookingService.cancelBooking(bookingId, currentUser.getId(), reason, requestRefund);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled"));
    }
}
