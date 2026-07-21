package com.university.lms.repository;

import java.math.BigDecimal;
import java.util.List;

import com.university.lms.entity.Payment;

public interface PaymentRepository {

    List<Payment> findByFineId(Long fineId);

    BigDecimal sumAmountByFineId(Long fineId);

    Payment save(Payment payment);
}
