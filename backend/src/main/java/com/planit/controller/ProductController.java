package com.planit.controller;

import com.planit.dto.request.CreateProductRequest;
import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.PagedResponse;
import com.planit.model.Product;
import com.planit.security.UserPrincipal;
import com.planit.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<Product>> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "rating") String sortBy) {

        Page<Product> products = productService.searchProducts(
                search, category, city, lat, lng, radius,
                minPrice, maxPrice, rating, available,
                page, pageSize, sortBy);

        return ResponseEntity.ok(PagedResponse.of(
                products.getContent(), page, pageSize, products.getTotalElements()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Product>> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String productId,
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.updateProduct(currentUser.getId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated"));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String productId) {
        productService.deleteProduct(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Product archived"));
    }
}
