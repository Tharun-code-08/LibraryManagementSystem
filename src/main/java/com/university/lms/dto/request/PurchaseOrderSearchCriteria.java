package com.university.lms.dto.request;

/** Input to {@code PurchaseOrderService.search} — every field optional except pagination. */
public final class PurchaseOrderSearchCriteria {

    private final String status;
    private final int pageNumber;
    private final int pageSize;

    private PurchaseOrderSearchCriteria(Builder builder) {
        this.status = builder.status;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    public String getStatus() {
        return status;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String status;
        private int pageNumber = 0;
        private int pageSize = 25;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PurchaseOrderSearchCriteria build() {
            return new PurchaseOrderSearchCriteria(this);
        }
    }
}
