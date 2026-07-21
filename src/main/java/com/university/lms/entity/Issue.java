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
import jakarta.persistence.Table;

/** A single book-copy loan. At most one {@code ISSUED}/{@code OVERDUE} row exists per copy at a
 *  time — enforced in the database by a generated-column unique index (see V4 migration). */
@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "membership_id")
    private Membership membership;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status = IssueStatus.ISSUED;

    protected Issue() {
    }

    public Issue(BookCopy bookCopy, Membership membership, User issuedBy, LocalDateTime issueDate, LocalDateTime dueDate) {
        this.bookCopy = bookCopy;
        this.membership = membership;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public Membership getMembership() {
        return membership;
    }

    public User getIssuedBy() {
        return issuedBy;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }
}
