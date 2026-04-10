package com.planit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;

    private String passwordHash;

    @Indexed
    private UserRole role;

    private Profile profile;
    private Address address;
    private Preferences preferences;
    private KYC kyc;

    @Indexed
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private List<LoginHistory> loginHistory = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ---- Embedded classes ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Profile {
        private String firstName;
        private String lastName;
        private String avatar;
        private Gender gender;
        private LocalDateTime dateOfBirth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
        private GeoPoint location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeoPoint {
        private String type = "Point";
        private double[] coordinates; // [longitude, latitude]
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Preferences {
        private String language = "en";
        private String currency = "INR";
        private NotificationSettings notificationSettings;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class NotificationSettings {
            private boolean email = true;
            private boolean sms = true;
            private boolean push = true;
            private boolean marketing = false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KYC {
        private boolean verified = false;
        private String documentType; // AADHAAR, PAN, DRIVING_LICENSE
        private String documentNumber;
        private String documentUrl;
        private LocalDateTime verifiedAt;
        private String verifiedBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginHistory {
        private LocalDateTime timestamp;
        private String ipAddress;
        private String deviceInfo;
    }

    // ---- Enums ----

    public enum UserRole {
        USER, VENDOR, PLANNER, ADMIN
    }

    public enum UserStatus {
        ACTIVE, SUSPENDED, DELETED
    }

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }
}
