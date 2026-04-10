package com.planit.model;

import lombok.*;
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
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendors")
public class Vendor {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String businessName;
    private String businessType; // EQUIPMENT_RENTAL, SERVICE_PROVIDER, FOOD_STALL
    private String description;
    private String logo;
    private String coverImage;

    private ContactInfo contactInfo;
    private BusinessAddress businessAddress;

    @Builder.Default
    private List<ServiceArea> serviceAreas = new ArrayList<>();

    private BankDetails bankDetails;
    private TaxInfo taxInfo;
    private Ratings ratings;
    private Stats stats;
    private Verification verification;
    private Map<String, OperatingHours> operatingHours; // Day name -> hours

    @Indexed
    private VendorStatus status = VendorStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ---- Embedded classes ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfo {
        private String email;
        private String phone;
        private String alternatePhone;
        private String website;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessAddress {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
        private User.GeoPoint location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceArea {
        private String city;
        private String state;
        private double radius; // km
        private double deliveryCharge;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankDetails {
        private String accountHolderName;
        private String accountNumber;
        private String ifscCode;
        private String bankName;
        private String accountType; // SAVINGS, CURRENT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaxInfo {
        private String gstNumber;
        private String panNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Ratings {
        private double average;
        private int count;
        private Map<Integer, Integer> distribution; // star -> count
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stats {
        private int totalBookings;
        private int completedBookings;
        private int cancelledBookings;
        private long totalRevenue; // in paise
        private int responseTime;  // minutes
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Verification {
        private boolean isVerified;
        private LocalDateTime verifiedAt;
        @Builder.Default
        private List<String> verificationDocuments = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperatingHours {
        private String open;
        private String close;
        private boolean closed;
    }

    public enum VendorStatus {
        PENDING, ACTIVE, SUSPENDED, REJECTED
    }
}
