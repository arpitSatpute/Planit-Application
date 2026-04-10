package com.planit.service;

import com.planit.dto.request.CreateBookingRequest;
import com.planit.exception.ResourceNotFoundException;
import com.planit.exception.UnauthorizedException;
import com.planit.exception.ValidationException;
import com.planit.model.Booking;
import com.planit.model.Product;
import com.planit.model.User;
import com.planit.model.Vendor;
import com.planit.repository.BookingRepository;
import com.planit.repository.ProductRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    private static final AtomicLong bookingCounter = new AtomicLong(0);
    private static final double PLATFORM_FEE_PERCENT = 0.10;
    private static final double GST_PERCENT = 0.18;

    public Booking createBooking(String userId, CreateBookingRequest request) {
        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != Product.ProductStatus.PUBLISHED) {
            throw new ValidationException("Product is not available for booking");
        }

        // Check date validity
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date must be after start date");
        }

        // Check availability
        List<Booking> conflicting = bookingRepository.findByProductIdAndStatusIn(
                request.getProductId(),
                List.of(Booking.BookingStatus.CONFIRMED, Booking.BookingStatus.IN_PROGRESS)
        );

        boolean hasConflict = conflicting.stream().anyMatch(b ->
                b.getSchedule().getStartDate().isBefore(request.getEndDate()) &&
                b.getSchedule().getEndDate().isAfter(request.getStartDate())
        );

        if (hasConflict) {
            throw new ValidationException("Product is not available for the selected dates");
        }

        // Get vendor
        Vendor vendor = vendorRepository.findById(product.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", product.getVendorId()));

        // Calculate pricing
        long basePrice = product.getPricingModel() != null ? product.getPricingModel().getBasePrice() : 0;
        long subtotal = basePrice * request.getQuantity();
        long deliveryCharge = 0;
        if (request.getLocation() != null && request.getLocation().getCity() != null) {
            deliveryCharge = calculateDeliveryCharge(vendor, request.getLocation().getCity());
        }
        long platformFee = (long) (subtotal * PLATFORM_FEE_PERCENT);
        long gst = (long) ((subtotal + deliveryCharge + platformFee) * GST_PERCENT);
        long totalAmount = subtotal + deliveryCharge + platformFee + gst;
        long securityDeposit = product.getPricingModel() != null ? product.getPricingModel().getSecurityDeposit() : 0;
        int advancePct = product.getPricingModel() != null ? product.getPricingModel().getAdvancePayment() : 50;
        long advancePayment = (long) (totalAmount * (advancePct / 100.0));

        // Build location
        Booking.BookingLocation bookingLocation = null;
        if (request.getLocation() != null) {
            var loc = request.getLocation();
            User.GeoPoint geoPoint = null;
            if (loc.getLatitude() != 0 && loc.getLongitude() != 0) {
                geoPoint = new User.GeoPoint("Point", new double[]{loc.getLongitude(), loc.getLatitude()});
            }
            bookingLocation = Booking.BookingLocation.builder()
                    .address(loc.getAddress())
                    .city(loc.getCity())
                    .state(loc.getState())
                    .location(geoPoint)
                    .deliveryRequired(true)
                    .deliveryCharge(deliveryCharge)
                    .build();
        }

        // Build event details
        Booking.EventDetails eventDetails = null;
        if (request.getEventDetails() != null) {
            var ed = request.getEventDetails();
            eventDetails = Booking.EventDetails.builder()
                    .eventName(ed.getEventName())
                    .eventType(ed.getEventType())
                    .guestCount(ed.getGuestCount())
                    .specialRequests(ed.getSpecialRequests())
                    .build();
        }

        // Determine duration
        long hours = java.time.Duration.between(request.getStartDate(), request.getEndDate()).toHours();

        Booking booking = Booking.builder()
                .bookingNumber(generateBookingNumber())
                .userId(userId)
                .vendorId(vendor.getId())
                .productId(product.getId())
                .bookingType("RENTAL")
                .eventDetails(eventDetails)
                .schedule(Booking.Schedule.builder()
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .duration((int) hours)
                        .timeSlot(determineTimeSlot(hours))
                        .build())
                .location(bookingLocation)
                .pricing(Booking.Pricing.builder()
                        .basePrice(basePrice)
                        .quantity(request.getQuantity())
                        .subtotal(subtotal)
                        .deliveryCharge(deliveryCharge)
                        .tax(Booking.Pricing.Tax.builder().gst(gst).build())
                        .platformFee(platformFee)
                        .totalAmount(totalAmount)
                        .securityDeposit(securityDeposit)
                        .advancePayment(advancePayment)
                        .currency("INR")
                        .build())
                .payment(Booking.PaymentInfo.builder()
                        .status("PENDING")
                        .build())
                .status(Booking.BookingStatus.PENDING)
                .statusHistory(new ArrayList<>(List.of(Booking.StatusHistory.builder()
                        .status(Booking.BookingStatus.PENDING)
                        .timestamp(LocalDateTime.now())
                        .note("Booking initiated")
                        .build())))
                .review(Booking.ReviewInfo.builder().reviewed(false).build())
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created: {} for user {} on product {}", booking.getBookingNumber(), userId, product.getId());
        return booking;
    }

    public Page<Booking> getUserBookings(String userId, Booking.BookingStatus status, int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return bookingRepository.findByUserId(userId, pageable);
    }

    public Page<Booking> getVendorBookings(String userId, Booking.BookingStatus status, int page, int pageSize) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "userId", userId));
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return bookingRepository.findByVendorIdAndStatus(vendor.getId(), status, pageable);
        }
        return bookingRepository.findByVendorId(vendor.getId(), pageable);
    }

    public Booking getBookingById(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check access
        Vendor vendor = vendorRepository.findByUserId(userId).orElse(null);
        boolean isOwner = booking.getUserId().equals(userId);
        boolean isVendor = vendor != null && booking.getVendorId().equals(vendor.getId());

        if (!isOwner && !isVendor) {
            throw new UnauthorizedException("Access denied");
        }

        return booking;
    }

    public Booking cancelBooking(String bookingId, String userId, String reason, boolean requestRefund) {
        Booking booking = getBookingById(bookingId, userId);

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel a completed booking");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellation(Booking.Cancellation.builder()
                .cancelledBy("USER")
                .cancelledAt(LocalDateTime.now())
                .reason(reason)
                .refundAmount(requestRefund ? booking.getPricing().getTotalAmount() : 0)
                .refundStatus(requestRefund ? "PROCESSING" : null)
                .build());
        booking.getStatusHistory().add(Booking.StatusHistory.builder()
                .status(Booking.BookingStatus.CANCELLED)
                .timestamp(LocalDateTime.now())
                .note("Cancelled: " + reason)
                .build());

        return bookingRepository.save(booking);
    }

    public Booking confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.getStatusHistory().add(Booking.StatusHistory.builder()
                .status(Booking.BookingStatus.CONFIRMED)
                .timestamp(LocalDateTime.now())
                .note("Payment successful")
                .build());
        return bookingRepository.save(booking);
    }

    private String generateBookingNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seqNum = bookingCounter.incrementAndGet();
        return String.format("BKG%s-%04d", datePart, seqNum);
    }

    private String determineTimeSlot(long hours) {
        if (hours <= 4) return "HOURLY";
        if (hours <= 8) return "HALF_DAY";
        if (hours <= 24) return "FULL_DAY";
        if (hours <= 48) return "OVERNIGHT";
        return "WEEKLY";
    }

    private long calculateDeliveryCharge(Vendor vendor, String city) {
        if (vendor.getServiceAreas() == null) return 0;
        return vendor.getServiceAreas().stream()
                .filter(sa -> city.equalsIgnoreCase(sa.getCity()))
                .findFirst()
                .map(sa -> (long) sa.getDeliveryCharge())
                .orElse(500L); // default charge
    }
}
