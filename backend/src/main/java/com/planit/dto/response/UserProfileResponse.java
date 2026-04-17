package com.planit.dto.response;

import com.planit.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private String id;
    private String email;
    private String phone;
    private String role;
    private String status;
    private User.Profile profile;
    private User.Address address;
    private User.Preferences preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .profile(user.getProfile())
                .address(user.getAddress())
                .preferences(user.getPreferences())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

