package com.planit.repository;

import com.planit.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    Page<Product> findByVendorId(String vendorId, Pageable pageable);

    Page<Product> findByCategoryAndStatus(String category, Product.ProductStatus status, Pageable pageable);

    List<Product> findByVendorIdAndStatus(String vendorId, Product.ProductStatus status);

    boolean existsBySlug(String slug);
}
