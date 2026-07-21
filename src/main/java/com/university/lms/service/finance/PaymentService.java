package com.university.lms.service.finance;

import java.util.List;

import com.university.lms.dto.request.PaymentRequestDTO;
import com.university.lms.dto.response.PaymentDTO;

public interface PaymentService {

    PaymentDTO collectPayment(PaymentRequestDTO request, Long receivedByUserId);

    List<PaymentDTO> listPaymentsForFine(Long fineId);
}
