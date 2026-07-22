package com.university.lms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_types")
public class MembershipType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "max_borrow_limit", nullable = false)
    private int maxBorrowLimit;

    @Column(name = "loan_period_days", nullable = false)
    private int loanPeriodDays;

    @Column(name = "fine_per_day", nullable = false)
    private BigDecimal finePerDay = BigDecimal.ZERO;

    @Column(name = "grace_period_days", nullable = false)
    private int gracePeriodDays;

    @Column(name = "renewal_limit", nullable = false)
    private int renewalLimit;

    protected MembershipType() {
    }

    public MembershipType(String name, int maxBorrowLimit, int loanPeriodDays, BigDecimal finePerDay,
                           int gracePeriodDays, int renewalLimit) {
        this.name = name;
        this.maxBorrowLimit = maxBorrowLimit;
        this.loanPeriodDays = loanPeriodDays;
        this.finePerDay = finePerDay;
        this.gracePeriodDays = gracePeriodDays;
        this.renewalLimit = renewalLimit;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public void setLoanPeriodDays(int loanPeriodDays) {
        this.loanPeriodDays = loanPeriodDays;
    }

    public BigDecimal getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public int getRenewalLimit() {
        return renewalLimit;
    }

    public void setRenewalLimit(int renewalLimit) {
        this.renewalLimit = renewalLimit;
    }
}
