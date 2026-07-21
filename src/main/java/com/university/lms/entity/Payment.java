package com.university.lms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One payment against a {@link Fine} — full or partial; a fine may have several. */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fine_id")
    private Fine fine;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    protected Payment() {
    }

    public Payment(Fine fine, BigDecimal amount, PaymentMethod method, LocalDateTime paidAt,
                    String receiptNumber, User receivedBy) {
        this.fine = fine;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
        this.receiptNumber = receiptNumber;
        this.receivedBy = receivedBy;
    }

    public Long getId() {
        return id;
    }

    public Fine getFine() {
        return fine;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public User getReceivedBy() {
        return receivedBy;
    }
}
