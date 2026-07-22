package com.university.lms.dto.request;

/** Input to {@code StudentService.register}/{@code update} — {@code id == null} means register. */
public final class StudentRegistrationRequestDTO {

    private final Long id;
    private final String username;
    private final String email;
    private final String temporaryPassword;
    private final String studentId;
    private final String rollNumber;
    private final String department;
    private final Integer year;
    private final Integer semester;
    private final String phone;
    private final String address;
    private final String guardianName;
    private final String guardianPhone;
    private final Long branchId;
    private final String photoPath;
    private final Long membershipTypeId;

    private StudentRegistrationRequestDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.temporaryPassword = builder.temporaryPassword;
        this.studentId = builder.studentId;
        this.rollNumber = builder.rollNumber;
        this.department = builder.department;
        this.year = builder.year;
        this.semester = builder.semester;
        this.phone = builder.phone;
        this.address = builder.address;
        this.guardianName = builder.guardianName;
        this.guardianPhone = builder.guardianPhone;
        this.branchId = builder.branchId;
        this.photoPath = builder.photoPath;
        this.membershipTypeId = builder.membershipTypeId;
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

    public String getTemporaryPassword() {
        return temporaryPassword;
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

    public Long getBranchId() {
        return branchId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public Long getMembershipTypeId() {
        return membershipTypeId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String username;
        private String email;
        private String temporaryPassword;
        private String studentId;
        private String rollNumber;
        private String department;
        private Integer year;
        private Integer semester;
        private String phone;
        private String address;
        private String guardianName;
        private String guardianPhone;
        private Long branchId;
        private String photoPath;
        private Long membershipTypeId;

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

        public Builder temporaryPassword(String temporaryPassword) {
            this.temporaryPassword = temporaryPassword;
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

        public Builder branchId(Long branchId) {
            this.branchId = branchId;
            return this;
        }

        public Builder photoPath(String photoPath) {
            this.photoPath = photoPath;
            return this;
        }

        public Builder membershipTypeId(Long membershipTypeId) {
            this.membershipTypeId = membershipTypeId;
            return this;
        }

        public StudentRegistrationRequestDTO build() {
            return new StudentRegistrationRequestDTO(this);
        }
    }
}
