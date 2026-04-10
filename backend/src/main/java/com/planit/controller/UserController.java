package com.planit.controller;

import com.planit.dto.response.ApiResponse;
import com.planit.model.User;
import com.planit.security.UserPrincipal;
import com.planit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody User.Profile profile) {
        User user = userService.updateProfile(currentUser.getId(), profile);
        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated"));
    }

    @PutMapping("/me/address")
    public ResponseEntity<ApiResponse<User>> updateAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody User.Address address) {
        User user = userService.updateAddress(currentUser.getId(), address);
        return ResponseEntity.ok(ApiResponse.success(user, "Address updated"));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<ApiResponse<User>> updatePreferences(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody User.Preferences preferences) {
        User user = userService.updatePreferences(currentUser.getId(), preferences);
        return ResponseEntity.ok(ApiResponse.success(user, "Preferences updated"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
