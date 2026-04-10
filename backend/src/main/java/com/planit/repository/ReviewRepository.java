package com.planit.repository;

import com.planit.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByProductIdAndStatus(String productId, Review.ReviewStatus status, Pageable pageable);

    Page<Review> findByVendorIdAndStatus(String vendorId, Review.ReviewStatus status, Pageable pageable);

    Page<Review> findByUserId(String userId, Pageable pageable);

    Optional<Review> findByBookingId(String bookingId);

    boolean existsByBookingId(String bookingId);
}
