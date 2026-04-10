package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.PagedResponse;
import com.planit.model.Review;
import com.planit.security.UserPrincipal;
import com.planit.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, Object> body) {
        String bookingId = (String) body.get("bookingId");
        int rating = (int) body.get("rating");
        String title = (String) body.get("title");
        String comment = (String) body.get("comment");
        Review review = reviewService.createReview(currentUser.getId(), bookingId, rating, title, comment, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(review, "Review submitted"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<Review>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Review> reviews = reviewService.getProductReviews(productId, page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(reviews.getContent(), page, pageSize, reviews.getTotalElements()));
    }

    @PostMapping("/{reviewId}/vendor-response")
    public ResponseEntity<ApiResponse<Review>> respondToReview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        Review review = reviewService.addVendorResponse(currentUser.getId(), reviewId, body.get("comment"));
        return ResponseEntity.ok(ApiResponse.success(review, "Response added"));
    }
}
