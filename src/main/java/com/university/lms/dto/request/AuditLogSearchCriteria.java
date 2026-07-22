package com.university.lms.dto.request;

import java.time.LocalDateTime;

/** Input to the audit log viewer — every field optional except pagination. */
public final class AuditLogSearchCriteria {

    private final Long actorUserId;
    private final String entityType;
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;
    private final int pageNumber;
    private final int pageSize;

    private AuditLogSearchCriteria(Builder builder) {
        this.actorUserId = builder.actorUserId;
        this.entityType = builder.entityType;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getEntityType() {
        return entityType;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
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
        private Long actorUserId;
        private String entityType;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private int pageNumber = 0;
        private int pageSize = 25;

        public Builder actorUserId(Long actorUserId) {
            this.actorUserId = actorUserId;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder fromDate(LocalDateTime fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(LocalDateTime toDate) {
            this.toDate = toDate;
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

        public AuditLogSearchCriteria build() {
            return new AuditLogSearchCriteria(this);
        }
    }
}
