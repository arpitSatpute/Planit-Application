package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.PagedResponse;
import com.planit.model.User;
import com.planit.model.Vendor;
import com.planit.security.UserPrincipal;
import com.planit.service.UserService;
import com.planit.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final VendorService vendorService;

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<User>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<User> users = userService.getAllUsers(page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(users.getContent(), page, pageSize, users.getTotalElements()));
    }

    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<ApiResponse<User>> suspendUser(@PathVariable String userId) {
        User user = userService.suspendUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User suspended"));
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable String userId) {
        User user = userService.activateUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User activated"));
    }

    @GetMapping("/vendors")
    public ResponseEntity<PagedResponse<Vendor>> getAllVendors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Vendor> vendors = vendorService.getAllVendors(page, pageSize);
        return ResponseEntity.ok(PagedResponse.of(vendors.getContent(), page, pageSize, vendors.getTotalElements()));
    }

    @PatchMapping("/vendors/{vendorId}/verify")
    public ResponseEntity<ApiResponse<Vendor>> verifyVendor(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String vendorId) {
        Vendor vendor = vendorService.verifyVendor(vendorId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(vendor, "Vendor verified"));
    }
}
