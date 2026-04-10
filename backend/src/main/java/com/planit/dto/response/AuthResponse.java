package com.planit.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String userId;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private String id;
        private String email;
        private String phone;
        private String role;
        private String firstName;
        private String lastName;
        private String avatar;
    }
}
