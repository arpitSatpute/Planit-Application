package com.planit.scheduled;

import com.planit.model.Payment;
import com.planit.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSettlementJob {

    private final PaymentRepository paymentRepository;

    /**
     * Processes vendor payouts for successful payments (7 days after booking completion).
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processVendorPayouts() {
        List<Payment> pendingPayouts = paymentRepository.findByVendorIdAndPayout_PayoutStatus(
                null, "PENDING"); // null vendorId means all vendors

        log.info("PaymentSettlementJob: {} pending payouts", pendingPayouts.size());

        for (Payment payment : pendingPayouts) {
            if (payment.getPayout() != null &&
                    payment.getPayout().getPayoutDate() != null &&
                    payment.getPayout().getPayoutDate().isBefore(LocalDateTime.now())) {
                // TODO: initiate actual payout via Razorpay/bank transfer
                Payment.Payout payout = payment.getPayout();
                payout.setPayoutStatus("PROCESSING");
                payout.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("Payout initiated for payment: {}", payment.getId());
            }
        }
    }
}
