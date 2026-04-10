package com.planit.service;

import com.planit.dto.request.CreateProductRequest;
import com.planit.exception.ResourceNotFoundException;
import com.planit.exception.UnauthorizedException;
import com.planit.exception.ValidationException;
import com.planit.model.Product;
import com.planit.model.Vendor;
import com.planit.repository.ProductRepository;
import com.planit.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final MongoTemplate mongoTemplate;

    public Page<Product> searchProducts(String search, String category, String city,
                                        Double lat, Double lng, Double radius,
                                        Long minPrice, Long maxPrice,
                                        Double minRating, Boolean available,
                                        int page, int pageSize, String sortBy) {
        Pageable pageable = buildPageable(page, pageSize, sortBy);

        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Product.ProductStatus.PUBLISHED));

        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (city != null && !city.isBlank()) {
            query.addCriteria(Criteria.where("location.city").regex(city, "i"));
        }
        if (minPrice != null) {
            query.addCriteria(Criteria.where("pricingModel.basePrice").gte(minPrice));
        }
        if (maxPrice != null) {
            query.addCriteria(Criteria.where("pricingModel.basePrice").lte(maxPrice));
        }
        if (minRating != null) {
            query.addCriteria(Criteria.where("ratings.average").gte(minRating));
        }
        if (available != null && available) {
            query.addCriteria(Criteria.where("availability.isAvailable").is(true));
        }
        if (search != null && !search.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                Criteria.where("name").regex(search, "i"),
                Criteria.where("description").regex(search, "i"),
                Criteria.where("tags").regex(search, "i")
            ));
        }

        query.with(pageable);
        long total = mongoTemplate.count(query, Product.class);
        List<Product> products = mongoTemplate.find(query, Product.class);

        return new org.springframework.data.domain.PageImpl<>(products, pageable, total);
    }

    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
    }

    public Product createProduct(String userId, CreateProductRequest request) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("User is not a registered vendor"));

        if (vendor.getStatus() != Vendor.VendorStatus.ACTIVE) {
            throw new ValidationException("Vendor account is not active");
        }

        String slug = generateSlug(request.getName());

        List<Product.ProductImage> images = new ArrayList<>();
        if (request.getImages() != null) {
            images = request.getImages().stream()
                    .map(img -> Product.ProductImage.builder()
                            .url(img.getUrl())
                            .isPrimary(img.isPrimary())
                            .order(img.getOrder())
                            .build())
                    .collect(Collectors.toList());
        }

        Product.PricingModel pricingModel = null;
        if (request.getPricingModel() != null) {
            var pm = request.getPricingModel();
            List<Product.PricingModel.PricingTier> tiers = new ArrayList<>();
            if (pm.getTiers() != null) {
                tiers = pm.getTiers().stream()
                        .map(t -> Product.PricingModel.PricingTier.builder()
                                .duration(t.getDuration())
                                .price(t.getPrice())
                                .minHours(t.getMinHours())
                                .build())
                        .collect(Collectors.toList());
            }
            pricingModel = Product.PricingModel.builder()
                    .type(pm.getType())
                    .basePrice(pm.getBasePrice())
                    .currency(pm.getCurrency() != null ? pm.getCurrency() : "INR")
                    .tiers(tiers)
                    .securityDeposit(pm.getSecurityDeposit())
                    .advancePayment(pm.getAdvancePayment())
                    .build();
        }

        Product.Inventory inventory = null;
        if (request.getInventory() != null) {
            inventory = Product.Inventory.builder()
                    .totalQuantity(request.getInventory().getTotalQuantity())
                    .availableQuantity(request.getInventory().getTotalQuantity())
                    .trackInventory(request.getInventory().isTrackInventory())
                    .build();
        }

        Product product = Product.builder()
                .vendorId(vendor.getId())
                .name(request.getName())
                .slug(slug)
                .category(request.getCategory())
                .subcategory(request.getSubcategory())
                .description(request.getDescription())
                .images(images)
                .pricingModel(pricingModel)
                .inventory(inventory)
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .ratings(Product.Ratings.builder().average(0.0).count(0).build())
                .stats(Product.Stats.builder().build())
                .status(Product.ProductStatus.DRAFT)
                .availability(Product.AvailabilityConfig.builder()
                        .isAvailable(true)
                        .bufferTime(60)
                        .advanceBookingDays(180)
                        .minBookingDays(1)
                        .build())
                .build();

        product = productRepository.save(product);
        log.info("Product created: {} by vendor {}", product.getId(), vendor.getId());
        return product;
    }

    public Product updateProduct(String userId, String productId, CreateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("User is not a registered vendor"));

        if (!product.getVendorId().equals(vendor.getId())) {
            throw new UnauthorizedException("You can only update your own products");
        }

        // Update fields
        if (request.getName() != null) {
            product.setName(request.getName());
            product.setSlug(generateSlug(request.getName()));
        }
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getTags() != null) product.setTags(request.getTags());

        return productRepository.save(product);
    }

    public void deleteProduct(String userId, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("User is not a vendor"));

        if (!product.getVendorId().equals(vendor.getId())) {
            throw new UnauthorizedException("You can only delete your own products");
        }

        product.setStatus(Product.ProductStatus.ARCHIVED);
        productRepository.save(product);
        log.info("Product archived: {}", productId);
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Ensure unique slug
        String baseSlug = slug;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private Pageable buildPageable(int page, int pageSize, String sortBy) {
        Sort sort = switch (sortBy != null ? sortBy : "rating") {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "pricingModel.basePrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "pricingModel.basePrice");
            case "distance" -> Sort.unsorted(); // handled by geo query
            default -> Sort.by(Sort.Direction.DESC, "ratings.average");
        };
        return PageRequest.of(Math.max(page - 1, 0), pageSize, sort);
    }
}
