package com.planit.repository;

import com.planit.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    Page<Booking> findByUserId(String userId, Pageable pageable);

    Page<Booking> findByVendorId(String vendorId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(String userId, Booking.BookingStatus status, Pageable pageable);

    Page<Booking> findByVendorIdAndStatus(String vendorId, Booking.BookingStatus status, Pageable pageable);

    List<Booking> findByProductIdAndStatusNot(String productId, Booking.BookingStatus status);

    List<Booking> findByProductIdAndStatusIn(String productId, List<Booking.BookingStatus> statuses);

    List<Booking> findByStatusAndSchedule_StartDateBetween(
            Booking.BookingStatus status, LocalDateTime start, LocalDateTime end);

    long countByVendorIdAndStatus(String vendorId, Booking.BookingStatus status);
}
