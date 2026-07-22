package com.university.lms.dto.request;

import java.time.LocalDate;

import com.university.lms.service.report.ReportType;

/** Input to {@code ReportService.generate} — every filter field optional besides the report type. */
public final class ReportCriteriaDTO {

    private final ReportType reportType;
    private final String department;
    private final Integer year;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    private ReportCriteriaDTO(Builder builder) {
        this.reportType = builder.reportType;
        this.department = builder.department;
        this.year = builder.year;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getYear() {
        return year;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ReportType reportType;
        private String department;
        private Integer year;
        private LocalDate fromDate;
        private LocalDate toDate;

        public Builder reportType(ReportType reportType) {
            this.reportType = reportType;
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

        public Builder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public ReportCriteriaDTO build() {
            return new ReportCriteriaDTO(this);
        }
    }
}
