package com.university.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_audit_items")
public class InventoryAuditItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_audit_id")
    private InventoryAudit inventoryAudit;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    @Column(name = "expected_status", nullable = false, length = 30)
    private String expectedStatus;

    @Column(name = "found_status", nullable = false, length = 30)
    private String foundStatus;

    @Column(length = 255)
    private String notes;

    protected InventoryAuditItem() {
    }

    public InventoryAuditItem(InventoryAudit inventoryAudit, BookCopy bookCopy, String expectedStatus,
                               String foundStatus, String notes) {
        this.inventoryAudit = inventoryAudit;
        this.bookCopy = bookCopy;
        this.expectedStatus = expectedStatus;
        this.foundStatus = foundStatus;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public InventoryAudit getInventoryAudit() {
        return inventoryAudit;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }

    public String getFoundStatus() {
        return foundStatus;
    }

    public String getNotes() {
        return notes;
    }
}
