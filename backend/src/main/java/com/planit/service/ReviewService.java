package com.planit.service;

import com.planit.exception.ResourceNotFoundException;
import com.planit.exception.ValidationException;
import com.planit.model.Booking;
import com.planit.model.Review;
import com.planit.model.Vendor;
import com.planit.repository.BookingRepository;
import com.planit.repository.ReviewRepository;
import com.planit.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final VendorRepository vendorRepository;

    public Review createReview(String userId, String bookingId, int rating,
                               String title, String comment,
                               Review.AspectRatings aspectRatings) {
        // Validate booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ValidationException("You can only review your own bookings");
        }

        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new ValidationException("You can only review completed bookings");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ValidationException("You have already reviewed this booking");
        }

        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .bookingId(bookingId)
                .userId(userId)
                .vendorId(booking.getVendorId())
                .productId(booking.getProductId())
                .rating(rating)
                .title(title)
                .comment(comment)
                .aspectRatings(aspectRatings)
                .status(Review.ReviewStatus.PUBLISHED)
                .helpful(Review.Helpful.builder().count(0).build())
                .build();

        review = reviewRepository.save(review);

        // Update vendor average rating
        updateVendorRating(booking.getVendorId());

        // Mark booking as reviewed
        if (booking.getReview() != null) {
            booking.getReview().setReviewed(true);
            booking.getReview().setReviewId(review.getId());
            bookingRepository.save(booking);
        }

        log.info("Review created for booking: {}", bookingId);
        return review;
    }

    public Page<Review> getProductReviews(String productId, int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByProductIdAndStatus(productId, Review.ReviewStatus.PUBLISHED, pageable);
    }

    public Review addVendorResponse(String vendorUserId, String reviewId, String responseComment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Vendor vendor = vendorRepository.findByUserId(vendorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "userId", vendorUserId));

        if (!review.getVendorId().equals(vendor.getId())) {
            throw new ValidationException("You can only respond to reviews on your business");
        }

        review.setVendorResponse(Review.VendorResponse.builder()
                .comment(responseComment)
                .respondedAt(LocalDateTime.now())
                .build());

        return reviewRepository.save(review);
    }

    private void updateVendorRating(String vendorId) {
        vendorRepository.findById(vendorId).ifPresent(vendor -> {
            // Get all published reviews for the vendor
            var allReviews = reviewRepository.findByVendorIdAndStatus(
                    vendorId, Review.ReviewStatus.PUBLISHED,
                    PageRequest.of(0, 10000, Sort.by("createdAt")));

            List<Review> reviews = allReviews.getContent();
            int count = reviews.size();
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

            if (vendor.getRatings() == null) {
                vendor.setRatings(Vendor.Ratings.builder().build());
            }
            vendor.getRatings().setAverage(avg);
            vendor.getRatings().setCount(count);
            vendorRepository.save(vendor);
        });
    }
}
