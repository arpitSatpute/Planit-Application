package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.model.Vendor;
import com.planit.security.UserPrincipal;
import com.planit.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Vendor>> registerVendor(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, String> body) {
        Vendor vendor = vendorService.registerVendor(
                currentUser.getId(),
                body.get("businessName"),
                body.get("businessType"),
                body.get("description")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vendor, "Vendor registered successfully"));
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Vendor>> getVendor(@PathVariable String vendorId) {
        Vendor vendor = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Vendor>> getMyVendorProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Vendor vendor = vendorService.getVendorByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Vendor>> updateVendorProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Vendor vendorUpdate) {
        Vendor vendor = vendorService.updateVendor(currentUser.getId(), vendorUpdate);
        return ResponseEntity.ok(ApiResponse.success(vendor, "Vendor profile updated"));
    }
}
