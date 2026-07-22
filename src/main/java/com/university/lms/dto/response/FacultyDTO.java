package com.university.lms.dto.response;

import java.time.LocalDate;

/** Presentation-safe projection of {@code Faculty} (+ its linked {@code User} and membership). */
public final class FacultyDTO {

    private final Long id;
    private final String username;
    private final String email;
    private final String facultyId;
    private final String department;
    private final String designation;
    private final String phone;
    private final String office;
    private final String membershipTypeName;
    private final String membershipStatus;
    private final LocalDate membershipExpiryDate;

    private FacultyDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.facultyId = builder.facultyId;
        this.department = builder.department;
        this.designation = builder.designation;
        this.phone = builder.phone;
        this.office = builder.office;
        this.membershipTypeName = builder.membershipTypeName;
        this.membershipStatus = builder.membershipStatus;
        this.membershipExpiryDate = builder.membershipExpiryDate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public String getPhone() {
        return phone;
    }

    public String getOffice() {
        return office;
    }

    public String getMembershipTypeName() {
        return membershipTypeName;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public LocalDate getMembershipExpiryDate() {
        return membershipExpiryDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String username;
        private String email;
        private String facultyId;
        private String department;
        private String designation;
        private String phone;
        private String office;
        private String membershipTypeName;
        private String membershipStatus;
        private LocalDate membershipExpiryDate;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder facultyId(String facultyId) {
            this.facultyId = facultyId;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder designation(String designation) {
            this.designation = designation;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder office(String office) {
            this.office = office;
            return this;
        }

        public Builder membershipTypeName(String membershipTypeName) {
            this.membershipTypeName = membershipTypeName;
            return this;
        }

        public Builder membershipStatus(String membershipStatus) {
            this.membershipStatus = membershipStatus;
            return this;
        }

        public Builder membershipExpiryDate(LocalDate membershipExpiryDate) {
            this.membershipExpiryDate = membershipExpiryDate;
            return this;
        }

        public FacultyDTO build() {
            return new FacultyDTO(this);
        }
    }
}
