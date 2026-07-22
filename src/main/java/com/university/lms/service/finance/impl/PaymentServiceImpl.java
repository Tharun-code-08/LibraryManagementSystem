package com.university.lms.service.finance.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.PaymentRequestDTO;
import com.university.lms.dto.response.PaymentDTO;
import com.university.lms.entity.Fine;
import com.university.lms.entity.FineStatus;
import com.university.lms.entity.Payment;
import com.university.lms.entity.PaymentMethod;
import com.university.lms.entity.User;
import com.university.lms.exception.FineAlreadySettledException;
import com.university.lms.exception.InvalidPaymentAmountException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.PaymentRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.finance.PaymentService;
import com.university.lms.util.ReceiptGenerator;

public final class PaymentServiceImpl implements PaymentService {

    private final FineRepository fineRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final ReceiptGenerator receiptGenerator;
    private final AuditLogService auditLogService;

    public PaymentServiceImpl(FineRepository fineRepository, PaymentRepository paymentRepository,
                               UserRepository userRepository, MembershipHolderResolver membershipHolderResolver,
                               ReceiptGenerator receiptGenerator, AuditLogService auditLogService) {
        this.fineRepository = fineRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.receiptGenerator = receiptGenerator;
        this.auditLogService = auditLogService;
    }

    @Override
    public PaymentDTO collectPayment(PaymentRequestDTO request, Long receivedByUserId) {
        Fine fine = fineRepository.findById(request.fineId())
                .orElseThrow(() -> new ResourceNotFoundException("Fine", request.fineId()));
        if (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED) {
            throw new FineAlreadySettledException();
        }

        BigDecimal alreadyPaid = paymentRepository.sumAmountByFineId(fine.getId());
        BigDecimal remaining = fine.getAmount().subtract(alreadyPaid);
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("Payment amount must be positive.");
        }
        if (request.amount().compareTo(remaining) > 0) {
            throw new InvalidPaymentAmountException("Payment amount exceeds the remaining balance of " + remaining + ".");
        }

        User receivedBy = userRepository.findById(receivedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", receivedByUserId));

        LocalDateTime paidAt = LocalDateTime.now();
        String receiptNumber = "RCPT-" + fine.getId() + "-" + paidAt.toLocalDate().toString().replace("-", "")
                + "-" + System.currentTimeMillis();
        Payment payment = new Payment(fine, request.amount(), PaymentMethod.valueOf(request.method()),
                paidAt, receiptNumber, receivedBy);
        Payment saved = paymentRepository.save(payment);

        BigDecimal newTotalPaid = alreadyPaid.add(request.amount());
        fine.setStatus(newTotalPaid.compareTo(fine.getAmount()) >= 0 ? FineStatus.PAID : FineStatus.PARTIAL);
        fineRepository.save(fine);

        String memberName = membershipHolderResolver.resolveDisplayName(
                fine.getIssue().getMembership().getHolderType(), fine.getIssue().getMembership().getHolderId());
        String bookTitle = fine.getIssue().getBookCopy().getBook().getTitle();
        String receiptPath = receiptGenerator.generate(receiptNumber, memberName, bookTitle,
                request.amount(), request.method(), paidAt);

        auditLogService.log(receivedByUserId, "PAYMENT_COLLECTED", "Fine", fine.getId());

        return new PaymentDTO(saved.getId(), fine.getId(), saved.getAmount(), saved.getMethod().name(),
                saved.getPaidAt(), saved.getReceiptNumber(), receivedBy.getUsername(), receiptPath);
    }

    @Override
    public List<PaymentDTO> listPaymentsForFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ResourceNotFoundException("Fine", fineId));
        String memberName = membershipHolderResolver.resolveDisplayName(
                fine.getIssue().getMembership().getHolderType(), fine.getIssue().getMembership().getHolderId());
        String bookTitle = fine.getIssue().getBookCopy().getBook().getTitle();

        return paymentRepository.findByFineId(fineId).stream()
                .map(payment -> {
                    String receiptPath = receiptGenerator.generate(payment.getReceiptNumber(), memberName, bookTitle,
                            payment.getAmount(), payment.getMethod().name(), payment.getPaidAt());
                    return new PaymentDTO(payment.getId(), fineId, payment.getAmount(), payment.getMethod().name(),
                            payment.getPaidAt(), payment.getReceiptNumber(), payment.getReceivedBy().getUsername(), receiptPath);
                })
                .toList();
    }
}
