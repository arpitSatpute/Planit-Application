package com.planit.repository;

import com.planit.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByGatewayResponse_PaymentId(String gatewayPaymentId);

    Page<Payment> findByUserId(String userId, Pageable pageable);

    Page<Payment> findByVendorId(String vendorId, Pageable pageable);

    List<Payment> findByVendorIdAndPayout_PayoutStatus(String vendorId, String payoutStatus);
}
