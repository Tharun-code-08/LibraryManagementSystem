package com.university.lms.dto.response;

import java.time.LocalDate;

/** Presentation-safe projection of {@code Student} (+ its linked {@code User} and membership). */
public final class StudentDTO {

    private final Long id;
    private final String username;
    private final String email;
    private final String studentId;
    private final String rollNumber;
    private final String department;
    private final Integer year;
    private final Integer semester;
    private final String phone;
    private final String address;
    private final String guardianName;
    private final String guardianPhone;
    private final String status;
    private final String branchName;
    private final String photoPath;
    private final String membershipTypeName;
    private final String membershipStatus;
    private final LocalDate membershipExpiryDate;

    private StudentDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.studentId = builder.studentId;
        this.rollNumber = builder.rollNumber;
        this.department = builder.department;
        this.year = builder.year;
        this.semester = builder.semester;
        this.phone = builder.phone;
        this.address = builder.address;
        this.guardianName = builder.guardianName;
        this.guardianPhone = builder.guardianPhone;
        this.status = builder.status;
        this.branchName = builder.branchName;
        this.photoPath = builder.photoPath;
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

    public String getStudentId() {
        return studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getSemester() {
        return semester;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public String getStatus() {
        return status;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getPhotoPath() {
        return photoPath;
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
        private String studentId;
        private String rollNumber;
        private String department;
        private Integer year;
        private Integer semester;
        private String phone;
        private String address;
        private String guardianName;
        private String guardianPhone;
        private String status;
        private String branchName;
        private String photoPath;
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

        public Builder studentId(String studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder rollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
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

        public Builder semester(Integer semester) {
            this.semester = semester;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder guardianName(String guardianName) {
            this.guardianName = guardianName;
            return this;
        }

        public Builder guardianPhone(String guardianPhone) {
            this.guardianPhone = guardianPhone;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder branchName(String branchName) {
            this.branchName = branchName;
            return this;
        }

        public Builder photoPath(String photoPath) {
            this.photoPath = photoPath;
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

        public StudentDTO build() {
            return new StudentDTO(this);
        }
    }
}
