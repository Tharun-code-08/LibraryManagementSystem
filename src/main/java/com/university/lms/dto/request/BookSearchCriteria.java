package com.university.lms.dto.request;

/** Input to {@code BookService.search} — every field optional except pagination. */
public final class BookSearchCriteria {

    private final String keyword;
    private final Long categoryId;
    private final String status;
    private final boolean includeDeleted;
    private final int pageNumber;
    private final int pageSize;

    private BookSearchCriteria(Builder builder) {
        this.keyword = builder.keyword;
        this.categoryId = builder.categoryId;
        this.status = builder.status;
        this.includeDeleted = builder.includeDeleted;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isIncludeDeleted() {
        return includeDeleted;
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
        private String keyword;
        private Long categoryId;
        private String status;
        private boolean includeDeleted;
        private int pageNumber = 0;
        private int pageSize = 25;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder includeDeleted(boolean includeDeleted) {
            this.includeDeleted = includeDeleted;
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

        public BookSearchCriteria build() {
            return new BookSearchCriteria(this);
        }
    }
}
