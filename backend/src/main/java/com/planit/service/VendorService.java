package com.planit.service;

import com.planit.exception.ResourceNotFoundException;
import com.planit.exception.ValidationException;
import com.planit.model.Vendor;
import com.planit.repository.UserRepository;
import com.planit.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public Vendor registerVendor(String userId, String businessName, String businessType, String description) {
        if (vendorRepository.existsByUserId(userId)) {
            throw new ValidationException("User is already registered as a vendor");
        }

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Vendor vendor = Vendor.builder()
                .userId(userId)
                .businessName(businessName)
                .businessType(businessType)
                .description(description)
                .ratings(Vendor.Ratings.builder().average(0.0).count(0).build())
                .stats(Vendor.Stats.builder().build())
                .verification(Vendor.Verification.builder().isVerified(false).build())
                .status(Vendor.VendorStatus.PENDING)
                .build();

        vendor = vendorRepository.save(vendor);
        log.info("Vendor registered: {} for user {}", vendor.getId(), userId);
        return vendor;
    }

    public Vendor getVendorById(String vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
    }

    public Vendor getVendorByUserId(String userId) {
        return vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "userId", userId));
    }

    public Page<Vendor> getAllVendors(int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "ratings.average"));
        return vendorRepository.findAll(pageable);
    }

    public Vendor updateVendor(String userId, Vendor vendor) {
        Vendor existing = getVendorByUserId(userId);
        if (vendor.getBusinessName() != null) existing.setBusinessName(vendor.getBusinessName());
        if (vendor.getDescription() != null) existing.setDescription(vendor.getDescription());
        if (vendor.getContactInfo() != null) existing.setContactInfo(vendor.getContactInfo());
        if (vendor.getServiceAreas() != null) existing.setServiceAreas(vendor.getServiceAreas());
        if (vendor.getOperatingHours() != null) existing.setOperatingHours(vendor.getOperatingHours());
        return vendorRepository.save(existing);
    }

    public Vendor verifyVendor(String vendorId, String adminId) {
        Vendor vendor = getVendorById(vendorId);
        if (vendor.getVerification() == null) {
            vendor.setVerification(Vendor.Verification.builder().build());
        }
        vendor.getVerification().setVerified(true);
        vendor.getVerification().setVerifiedAt(java.time.LocalDateTime.now());
        vendor.setStatus(Vendor.VendorStatus.ACTIVE);
        log.info("Vendor verified: {} by admin {}", vendorId, adminId);
        return vendorRepository.save(vendor);
    }
}
