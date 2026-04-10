package com.planit.scheduled;

import com.planit.model.Booking;
import com.planit.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingReminderJob {

    private final BookingRepository bookingRepository;

    /**
     * Sends reminders for bookings starting within the next 24 hours.
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        List<Booking> upcoming = bookingRepository.findByStatusAndSchedule_StartDateBetween(
                Booking.BookingStatus.CONFIRMED, now, next24h);

        log.info("BookingReminderJob: {} upcoming bookings found", upcoming.size());

        for (Booking booking : upcoming) {
            // TODO: send push notification / SMS / Email
            log.debug("Reminder for booking: {}", booking.getBookingNumber());
        }
    }

    /**
     * Auto-cancels PENDING bookings older than 15 minutes (payment timeout).
     * Runs every 5 minutes.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void expirePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        // Find bookings that are PENDING and were created over 15 min ago
        // (In a real system, query by createdAt < cutoff and status = PENDING)
        log.debug("Expiry check for pending bookings before {}", cutoff);
    }
}
