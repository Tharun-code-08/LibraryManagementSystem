package com.university.lms.entity;

import java.time.LocalDate;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** A single physical, barcode-tagged instance of a {@link Book} on a shelf at a {@link Branch}. */
@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false, length = 50)
    private String barcode;

    @Column(length = 30)
    private String shelf;

    @Column(length = 30)
    private String rack;

    @Column(name = "row_label", length = 30)
    private String rowLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookCopyCondition condition = BookCopyCondition.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookCopyStatus status = BookCopyStatus.AVAILABLE;

    @Column(name = "acquired_at")
    private LocalDate acquiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BookCopy() {
    }

    public BookCopy(Book book, Branch branch, String barcode, String shelf, String rack,
                     String rowLabel, BookCopyCondition condition, LocalDate acquiredAt) {
        this.book = book;
        this.branch = branch;
        this.barcode = barcode;
        this.shelf = shelf;
        this.rack = rack;
        this.rowLabel = rowLabel;
        this.condition = condition;
        this.acquiredAt = acquiredAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public BookCopyCondition getCondition() {
        return condition;
    }

    public void setCondition(BookCopyCondition condition) {
        this.condition = condition;
    }

    public BookCopyStatus getStatus() {
        return status;
    }

    public void setStatus(BookCopyStatus status) {
        this.status = status;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }
}
