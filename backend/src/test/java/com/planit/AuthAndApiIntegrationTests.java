package com.planit;

import com.planit.repository.BookingRepository;
import com.planit.repository.ChatMessageRepository;
import com.planit.repository.ProductRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VendorRepository;
import com.planit.service.auth.AuthStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "app.seed.enabled=false",
        "app.auth.store.fallback-enabled=true"
})
class AuthAndApiIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AuthStateStore authStateStore;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        chatMessageRepository.deleteAll();
        bookingRepository.deleteAll();
        productRepository.deleteAll();
        vendorRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authLifecycle_worksWithFallbackStore() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "test+" + unique + "@planit.local";
        String phone = "+9199" + (long) (Math.random() * 1_0000_0000L);

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                Map.of(
                        "email", email,
                        "phone", phone,
                        "password", "Password@123",
                        "role", "USER",
                        "firstName", "Test",
                        "lastName", "User"
                ),
                Map.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                Map.of("identifier", email, "password", "Password@123"),
                Map.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> loginData = (Map<String, Object>) loginResponse.getBody().get("data");
        String accessToken = (String) loginData.get("accessToken");
        assertThat(accessToken).isNotBlank();

        ResponseEntity<Map> otpResponse = restTemplate.postForEntity(
                baseUrl + "/auth/send-otp",
                Map.of("phone", phone, "purpose", "LOGIN"),
                Map.class
        );
        assertThat(otpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> otpData = (Map<String, Object>) otpResponse.getBody().get("data");
        String otpId = (String) otpData.get("otpId");
        assertThat(otpId).isNotBlank();

        String otpValue = authStateStore.findOtp(otpId).orElseThrow().otp();

        ResponseEntity<Map> verifyOtpResponse = restTemplate.postForEntity(
                baseUrl + "/auth/verify-otp",
                Map.of("otpId", otpId, "otp", otpValue),
                Map.class
        );
        assertThat(verifyOtpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> logoutResponse = restTemplate.exchange(
                baseUrl + "/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                Map.class
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> meAfterLogout = restTemplate.exchange(
                baseUrl + "/users/me",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                Map.class
        );
        assertThat(meAfterLogout.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void chatConversations_returnsSummaryForAuthenticatedUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String userAEmail = "sender+" + unique + "@planit.local";
        String userBEmail = "receiver+" + unique + "@planit.local";

        String userAId = registerAndGetUserId(userAEmail, "+9188" + (long) (Math.random() * 1_0000_0000L));
        String userBId = registerAndGetUserId(userBEmail, "+9177" + (long) (Math.random() * 1_0000_0000L));

        String senderToken = loginAndGetToken(userAEmail);
        String receiverToken = loginAndGetToken(userBEmail);

        HttpHeaders senderHeaders = new HttpHeaders();
        senderHeaders.setBearerAuth(senderToken);
        ResponseEntity<Map> sendResponse = restTemplate.exchange(
                baseUrl + "/chat/send",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "receiverId", userBId,
                        "content", "Hello from integration test",
                        "type", "TEXT"
                ), senderHeaders),
                Map.class
        );
        assertThat(sendResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpHeaders receiverHeaders = new HttpHeaders();
        receiverHeaders.setBearerAuth(receiverToken);
        ResponseEntity<Map> conversationsResponse = restTemplate.exchange(
                baseUrl + "/chat/conversations",
                HttpMethod.GET,
                new HttpEntity<>(null, receiverHeaders),
                Map.class
        );

        assertThat(conversationsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(conversationsResponse.getBody()).containsKey("data");
        assertThat((java.util.List<?>) conversationsResponse.getBody().get("data")).isNotEmpty();
        Map<String, Object> firstConversation = (Map<String, Object>) ((java.util.List<?>) conversationsResponse.getBody().get("data")).get(0);
        assertThat(firstConversation.get("participantId")).isEqualTo(userAId);
    }

    @Test
    void missingRoute_returnsStructured404() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "missing+" + unique + "@planit.local";
        registerAndGetUserId(email, "+9166" + (long) (Math.random() * 1_0000_0000L));
        String token = loginAndGetToken(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/route/does-not-exist",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(String.valueOf(response.getBody().get("message"))).contains("Endpoint not found");
    }

    private String registerAndGetUserId(String email, String phone) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                Map.of(
                        "email", email,
                        "phone", phone,
                        "password", "Password@123",
                        "role", "USER",
                        "firstName", "First",
                        "lastName", "Last"
                ),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return String.valueOf(data.get("userId"));
    }

    private String loginAndGetToken(String email) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                Map.of("identifier", email, "password", "Password@123"),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return String.valueOf(data.get("accessToken"));
    }
}
