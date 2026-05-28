package com.sifap.payment.infrastructure;

import com.sifap.payment.domain.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiscountRepository extends JpaRepository<DiscountEntity, UUID> {

    List<DiscountEntity> findByPaymentId(UUID paymentId);
}