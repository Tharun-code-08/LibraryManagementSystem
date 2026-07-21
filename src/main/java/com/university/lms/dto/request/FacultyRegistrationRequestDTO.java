package com.university.lms.dto.request;

/** Input to {@code FacultyService.register}/{@code update} — {@code id == null} means register. */
public final class FacultyRegistrationRequestDTO {

    private final Long id;
    private final String username;
    private final String email;
    private final String temporaryPassword;
    private final String facultyId;
    private final String department;
    private final String designation;
    private final String phone;
    private final String office;
    private final Long membershipTypeId;

    private FacultyRegistrationRequestDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.temporaryPassword = builder.temporaryPassword;
        this.facultyId = builder.facultyId;
        this.department = builder.department;
        this.designation = builder.designation;
        this.phone = builder.phone;
        this.office = builder.office;
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
        private String facultyId;
        private String department;
        private String designation;
        private String phone;
        private String office;
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

        public Builder membershipTypeId(Long membershipTypeId) {
            this.membershipTypeId = membershipTypeId;
            return this;
        }

        public FacultyRegistrationRequestDTO build() {
            return new FacultyRegistrationRequestDTO(this);
        }
    }
}
