package com.university.lms.dto.request;

/** Input to {@code StudentService.search} — every field optional except pagination. */
public final class StudentSearchCriteria {

    private final String keyword;
    private final String department;
    private final Integer year;
    private final String status;
    private final int pageNumber;
    private final int pageSize;

    private StudentSearchCriteria(Builder builder) {
        this.keyword = builder.keyword;
        this.department = builder.department;
        this.year = builder.year;
        this.status = builder.status;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getYear() {
        return year;
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
        private String keyword;
        private String department;
        private Integer year;
        private String status;
        private int pageNumber = 0;
        private int pageSize = 25;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

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

        public StudentSearchCriteria build() {
            return new StudentSearchCriteria(this);
        }
    }
}
