package com.university.lms.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Closes exactly one {@link Issue}; {@code issue_id} is unique at the database level. */
@Entity
@Table(name = "returns")
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "issue_id", unique = true)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_return", nullable = false, length = 20)
    private ReturnCondition conditionOnReturn = ReturnCondition.GOOD;

    @Column(length = 255)
    private String notes;

    protected Return() {
    }

    public Return(Issue issue, User receivedBy, LocalDateTime returnDate, ReturnCondition conditionOnReturn, String notes) {
        this.issue = issue;
        this.receivedBy = receivedBy;
        this.returnDate = returnDate;
        this.conditionOnReturn = conditionOnReturn;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public User getReceivedBy() {
        return receivedBy;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public ReturnCondition getConditionOnReturn() {
        return conditionOnReturn;
    }

    public String getNotes() {
        return notes;
    }
}
