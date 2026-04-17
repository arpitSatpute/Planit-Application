package com.planit.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planit.model.Booking;
import com.planit.model.ChatMessage;
import com.planit.model.Product;
import com.planit.model.User;
import com.planit.model.Vendor;
import com.planit.repository.BookingRepository;
import com.planit.repository.ChatMessageRepository;
import com.planit.repository.ProductRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VendorRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.force:false}")
    private boolean seedForce;

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            log.info("Local seed skipped (app.seed.enabled=false)");
            return;
        }

        if (seedForce) {
            log.info("Force reseed enabled. Clearing existing local collections.");
            chatMessageRepository.deleteAll();
            bookingRepository.deleteAll();
            productRepository.deleteAll();
            vendorRepository.deleteAll();
            userRepository.deleteAll();
        }

        seedUsers();
        seedVendors();
        seedProducts();
        seedBookings();
        seedChatMessages();
    }

    private void seedUsers() throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        List<UserSeed> items = readList("data/users.json", new TypeReference<>() {});
        List<User> users = items.stream().map(item -> User.builder()
                .id(item.getId())
                .email(item.getEmail())
                .phone(item.getPhone())
                .passwordHash(passwordEncoder.encode(item.getPassword()))
                .role(User.UserRole.valueOf(item.getRole()))
                .profile(User.Profile.builder()
                        .firstName(item.getFirstName())
                        .lastName(item.getLastName())
                        .build())
                .preferences(User.Preferences.builder()
                        .language("en")
                        .currency("INR")
                        .notificationSettings(User.Preferences.NotificationSettings.builder()
                                .email(true)
                                .sms(true)
                                .push(true)
                                .marketing(false)
                                .build())
                        .build())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()).collect(Collectors.toList());

        userRepository.saveAll(users);
        log.info("Seeded {} users", users.size());
    }

    private void seedVendors() throws Exception {
        if (vendorRepository.count() > 0) {
            return;
        }

        List<VendorSeed> items = readList("data/vendors.json", new TypeReference<>() {});
        List<Vendor> vendors = items.stream().map(item -> Vendor.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .businessName(item.getBusinessName())
                .businessType(item.getBusinessType())
                .description(item.getDescription())
                .serviceAreas(item.getServiceAreas() != null
                        ? item.getServiceAreas().stream()
                        .map(sa -> Vendor.ServiceArea.builder()
                                .city(sa.getCity())
                                .state(sa.getState())
                                .radius(sa.getRadius())
                                .deliveryCharge(sa.getDeliveryCharge())
                                .build())
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .ratings(Vendor.Ratings.builder().average(4.7).count(38).distribution(Map.of(5, 28, 4, 8, 3, 2)).build())
                .stats(Vendor.Stats.builder().totalBookings(120).completedBookings(110).cancelledBookings(4).build())
                .verification(Vendor.Verification.builder().isVerified(true).verifiedAt(LocalDateTime.now().minusDays(15)).build())
                .status(Vendor.VendorStatus.valueOf(item.getStatus()))
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now())
                .build()).collect(Collectors.toList());

        vendorRepository.saveAll(vendors);
        log.info("Seeded {} vendors", vendors.size());
    }

    private void seedProducts() throws Exception {
        if (productRepository.count() > 0) {
            return;
        }

        List<ProductSeed> items = readList("data/products.json", new TypeReference<>() {});
        List<Product> products = items.stream().map(item -> Product.builder()
                .id(item.getId())
                .vendorId(item.getVendorId())
                .name(item.getName())
                .slug(item.getSlug())
                .category(item.getCategory())
                .subcategory(item.getSubcategory())
                .description(item.getDescription())
                .images(List.of(Product.ProductImage.builder()
                        .url(item.getImageUrl())
                        .thumbnail(item.getImageUrl())
                        .isPrimary(true)
                        .order(0)
                        .build()))
                .pricingModel(Product.PricingModel.builder()
                        .type("DURATION_BASED")
                        .basePrice(item.getBasePrice())
                        .currency(item.getCurrency())
                        .tiers(List.of(Product.PricingModel.PricingTier.builder()
                                .duration("HOURLY")
                                .price(item.getBasePrice())
                                .minHours(1)
                                .build()))
                        .securityDeposit(item.getSecurityDeposit())
                        .advancePayment(item.getAdvancePayment())
                        .build())
                .inventory(Product.Inventory.builder()
                        .totalQuantity(10)
                        .availableQuantity(10)
                        .trackInventory(true)
                        .build())
                .availability(Product.AvailabilityConfig.builder()
                        .isAvailable(true)
                        .bufferTime(30)
                        .advanceBookingDays(180)
                        .minBookingDays(1)
                        .build())
                .location(Product.ProductLocation.builder()
                        .address(item.getAddress())
                        .city(item.getCity())
                        .state(item.getState())
                        .location(new User.GeoPoint("Point", new double[]{item.getLongitude(), item.getLatitude()}))
                        .build())
                .tags(item.getTags() != null ? item.getTags() : new ArrayList<>())
                .ratings(Product.Ratings.builder().average(item.getRating()).count(item.getRatingCount()).build())
                .stats(Product.Stats.builder().totalBookings(item.getBookings()).viewCount(300 + item.getBookings()).favoriteCount(60).build())
                .status(Product.ProductStatus.PUBLISHED)
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build()).collect(Collectors.toList());

        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
    }

    private void seedBookings() throws Exception {
        if (bookingRepository.count() > 0) {
            return;
        }

        List<BookingSeed> items = readList("data/bookings.json", new TypeReference<>() {});
        List<Booking> bookings = items.stream().map(item -> Booking.builder()
                .id(item.getId())
                .bookingNumber(item.getBookingNumber())
                .userId(item.getUserId())
                .vendorId(item.getVendorId())
                .productId(item.getProductId())
                .bookingType("RENTAL")
                .eventDetails(Booking.EventDetails.builder()
                        .eventName(item.getEventName())
                        .eventType(item.getEventType())
                        .guestCount(item.getGuestCount())
                        .specialRequests(item.getSpecialRequests())
                        .build())
                .schedule(Booking.Schedule.builder()
                        .startDate(item.getStartDate())
                        .endDate(item.getEndDate())
                        .duration(item.getDurationHours())
                        .timeSlot(item.getTimeSlot())
                        .build())
                .location(Booking.BookingLocation.builder()
                        .address(item.getAddress())
                        .city(item.getCity())
                        .state(item.getState())
                        .location(new User.GeoPoint("Point", new double[]{item.getLongitude(), item.getLatitude()}))
                        .deliveryRequired(true)
                        .deliveryCharge(item.getDeliveryCharge())
                        .build())
                .pricing(Booking.Pricing.builder()
                        .basePrice(item.getBasePrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .deliveryCharge(item.getDeliveryCharge())
                        .tax(Booking.Pricing.Tax.builder().gst(item.getGst()).serviceTax(0).build())
                        .platformFee(item.getPlatformFee())
                        .totalAmount(item.getTotalAmount())
                        .securityDeposit(item.getSecurityDeposit())
                        .advancePayment(item.getAdvancePayment())
                        .currency("INR")
                        .build())
                .payment(Booking.PaymentInfo.builder()
                        .status(item.getPaymentStatus())
                        .method("RAZORPAY")
                        .build())
                .status(Booking.BookingStatus.valueOf(item.getStatus()))
                .statusHistory(new ArrayList<>(List.of(Booking.StatusHistory.builder()
                        .status(Booking.BookingStatus.valueOf(item.getStatus()))
                        .timestamp(LocalDateTime.now().minusDays(2))
                        .note("Seeded booking state")
                        .build())))
                .communication(Booking.Communication.builder()
                        .conversationId(item.getConversationId())
                        .lastMessageAt(LocalDateTime.now().minusHours(6))
                        .build())
                .review(Booking.ReviewInfo.builder().reviewed(false).build())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build()).collect(Collectors.toList());

        bookingRepository.saveAll(bookings);
        log.info("Seeded {} bookings", bookings.size());
    }

    private void seedChatMessages() throws Exception {
        if (chatMessageRepository.count() > 0) {
            return;
        }

        List<ChatSeed> items = readList("data/chat_messages.json", new TypeReference<>() {});
        List<ChatMessage> messages = items.stream().map(item -> ChatMessage.builder()
                .id(item.getId())
                .conversationId(item.getConversationId())
                .senderId(item.getSenderId())
                .receiverId(item.getReceiverId())
                .bookingId(item.getBookingId())
                .type(item.getType())
                .content(item.getContent())
                .read(item.isRead())
                .readAt(item.getReadAt())
                .createdAt(item.getCreatedAt())
                .build()).collect(Collectors.toList());

        chatMessageRepository.saveAll(messages);
        log.info("Seeded {} chat messages", messages.size());
    }

    private <T> List<T> readList(String path, TypeReference<List<T>> type) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }

    @Data
    private static class UserSeed {
        private String id;
        private String email;
        private String phone;
        private String password;
        private String role;
        private String firstName;
        private String lastName;
    }

    @Data
    private static class VendorSeed {
        private String id;
        private String userId;
        private String businessName;
        private String businessType;
        private String description;
        private String status;
        private List<ServiceAreaSeed> serviceAreas;
    }

    @Data
    private static class ServiceAreaSeed {
        private String city;
        private String state;
        private double radius;
        private double deliveryCharge;
    }

    @Data
    private static class ProductSeed {
        private String id;
        private String vendorId;
        private String name;
        private String slug;
        private String category;
        private String subcategory;
        private String description;
        private String imageUrl;
        private long basePrice;
        private String currency;
        private long securityDeposit;
        private int advancePayment;
        private String address;
        private String city;
        private String state;
        private double latitude;
        private double longitude;
        private double rating;
        private int ratingCount;
        private int bookings;
        private List<String> tags;
    }

    @Data
    private static class BookingSeed {
        private String id;
        private String bookingNumber;
        private String userId;
        private String vendorId;
        private String productId;
        private String eventName;
        private String eventType;
        private int guestCount;
        private String specialRequests;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int durationHours;
        private String timeSlot;
        private String address;
        private String city;
        private String state;
        private double latitude;
        private double longitude;
        private int quantity;
        private long basePrice;
        private long subtotal;
        private long deliveryCharge;
        private long gst;
        private long platformFee;
        private long totalAmount;
        private long securityDeposit;
        private long advancePayment;
        private String paymentStatus;
        private String status;
        private String conversationId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    private static class ChatSeed {
        private String id;
        private String conversationId;
        private String senderId;
        private String receiverId;
        private String bookingId;
        private String type;
        private String content;
        private boolean read;
        private LocalDateTime readAt;
        private LocalDateTime createdAt;
    }
}

