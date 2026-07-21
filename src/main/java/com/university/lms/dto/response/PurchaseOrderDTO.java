package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Presentation-safe projection of {@code PurchaseOrder}, including its line items. */
public final class PurchaseOrderDTO {

    private final Long id;
    private final String supplierName;
    private final String orderedByName;
    private final LocalDate orderDate;
    private final String status;
    private final BigDecimal budgetAmount;
    private final String approvedByName;
    private final List<PurchaseOrderItemDTO> items;
    private final BigDecimal totalCost;

    private PurchaseOrderDTO(Builder builder) {
        this.id = builder.id;
        this.supplierName = builder.supplierName;
        this.orderedByName = builder.orderedByName;
        this.orderDate = builder.orderDate;
        this.status = builder.status;
        this.budgetAmount = builder.budgetAmount;
        this.approvedByName = builder.approvedByName;
        this.items = builder.items;
        this.totalCost = builder.totalCost;
    }

    public Long getId() {
        return id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getOrderedByName() {
        return orderedByName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public List<PurchaseOrderItemDTO> getItems() {
        return items;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String supplierName;
        private String orderedByName;
        private LocalDate orderDate;
        private String status;
        private BigDecimal budgetAmount;
        private String approvedByName;
        private List<PurchaseOrderItemDTO> items;
        private BigDecimal totalCost;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder supplierName(String supplierName) {
            this.supplierName = supplierName;
            return this;
        }

        public Builder orderedByName(String orderedByName) {
            this.orderedByName = orderedByName;
            return this;
        }

        public Builder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder budgetAmount(BigDecimal budgetAmount) {
            this.budgetAmount = budgetAmount;
            return this;
        }

        public Builder approvedByName(String approvedByName) {
            this.approvedByName = approvedByName;
            return this;
        }

        public Builder items(List<PurchaseOrderItemDTO> items) {
            this.items = items;
            return this;
        }

        public Builder totalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public PurchaseOrderDTO build() {
            return new PurchaseOrderDTO(this);
        }
    }
}
