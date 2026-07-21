package com.university.lms.entity;

import java.time.LocalDate;

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

/**
 * A borrower's active membership record. {@code holderId} points at either a {@code Student} or
 * {@code Faculty} row, discriminated by {@link #holderType}, rather than a polymorphic FK — kept
 * simple since only these two holder kinds exist.
 */
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "membership_type_id")
    private MembershipType membershipType;

    @Enumerated(EnumType.STRING)
    @Column(name = "holder_type", nullable = false, length = 20)
    private HolderType holderType;

    @Column(name = "holder_id", nullable = false)
    private Long holderId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    protected Membership() {
    }

    public Membership(MembershipType membershipType, HolderType holderType, Long holderId,
                       LocalDate startDate, LocalDate expiryDate) {
        this.membershipType = membershipType;
        this.holderType = holderType;
        this.holderId = holderId;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public HolderType getHolderType() {
        return holderType;
    }

    public Long getHolderId() {
        return holderId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }
}
