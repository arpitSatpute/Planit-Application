package com.planit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/event_rental_test",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "app.jwt.secret=test-secret-key-for-unit-tests-only-32chars",
        "app.payment.razorpay.key-id=test",
        "app.payment.razorpay.key-secret=test",
        "app.payment.razorpay.webhook-secret=test",
        "app.payment.stripe.api-key=sk_test_placeholder",
        "app.payment.stripe.webhook-secret=test"
})
class PlanItApplicationTests {

    @Test
    void contextLoads() {
    }
}
