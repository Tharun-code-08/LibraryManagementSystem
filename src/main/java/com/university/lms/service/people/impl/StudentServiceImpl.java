package com.university.lms.service.people.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.StudentImportRowDTO;
import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.dto.response.MembershipDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.entity.Branch;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Role;
import com.university.lms.entity.Student;
import com.university.lms.entity.StudentStatus;
import com.university.lms.entity.User;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.exception.ValidationException;
import com.university.lms.model.Page;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.MembershipService;
import com.university.lms.service.people.StudentService;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;
import com.university.lms.validation.impl.StudentValidator;

public final class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipService membershipService;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;
    private final int defaultMembershipValidityDays;

    private final Validator<StudentRegistrationRequestDTO> studentValidator = new StudentValidator();

    public StudentServiceImpl(StudentRepository studentRepository, UserRepository userRepository,
                               RoleRepository roleRepository, BranchRepository branchRepository,
                               PasswordEncoder passwordEncoder, MembershipService membershipService,
                               AuditLogService auditLogService, AuthContext authContext,
                               PermissionEvaluator permissionEvaluator, int defaultMembershipValidityDays) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.membershipService = membershipService;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
        this.defaultMembershipValidityDays = defaultMembershipValidityDays;
    }

    @Override
    public StudentDTO register(StudentRegistrationRequestDTO request) {
        permissionEvaluator.requirePermission("PEOPLE_MANAGE");
        validate(request);
        ensureUsernameAndEmailAvailable(request.getUsername(), request.getEmail());
        studentRepository.findByStudentId(request.getStudentId()).ifPresent(existing -> {
            throw new DuplicateResourceException("A student with ID " + request.getStudentId() + " already exists.");
        });
        studentRepository.findByRollNumber(request.getRollNumber()).ifPresent(existing -> {
            throw new DuplicateResourceException("A student with roll number " + request.getRollNumber() + " already exists.");
        });

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("STUDENT role is not seeded."));

        User user = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getTemporaryPassword()), branch);
        user.getRoles().add(studentRole);
        User savedUser = userRepository.save(user);

        Student student = new Student(savedUser, request.getStudentId(), request.getRollNumber(), branch);
        applyOptionalFields(student, request);

        Student saved = studentRepository.save(student);
        assignInitialMembership(request, saved);

        auditLogService.log(currentUserId(), "STUDENT_REGISTERED", "Student", saved.getId());
        return toDto(saved, membershipService.getActiveMembership(HolderType.STUDENT, saved.getId()).orElse(null));
    }

    @Override
    public StudentDTO update(StudentRegistrationRequestDTO request) {
        permissionEvaluator.requirePermission("PEOPLE_MANAGE");
        if (request.getId() == null) {
            throw new IllegalArgumentException("Student id is required for an update.");
        }
        Student student = studentRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getId()));

        studentRepository.findByStudentId(request.getStudentId())
                .filter(other -> !other.getId().equals(request.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A student with ID " + request.getStudentId() + " already exists.");
                });

        student.setStudentId(request.getStudentId());
        student.setRollNumber(request.getRollNumber());
        applyOptionalFields(student, request);

        Student saved = studentRepository.save(student);
        auditLogService.log(currentUserId(), "STUDENT_UPDATED", "Student", saved.getId());
        return toDto(saved, membershipService.getActiveMembership(HolderType.STUDENT, saved.getId()).orElse(null));
    }

    @Override
    public void changeStatus(Long studentId, String status) {
        permissionEvaluator.requirePermission("PEOPLE_MANAGE");
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        student.setStatus(StudentStatus.valueOf(status));
        studentRepository.save(student);
        auditLogService.log(currentUserId(), "STUDENT_STATUS_CHANGED", "Student", studentId);
    }

    @Override
    public Optional<StudentDTO> getById(Long id) {
        return studentRepository.findById(id)
                .map(student -> toDto(student, membershipService.getActiveMembership(HolderType.STUDENT, student.getId()).orElse(null)));
    }

    @Override
    public Page<StudentDTO> search(StudentSearchCriteria criteria) {
        List<StudentDTO> content = studentRepository.search(criteria).stream()
                .map(student -> toDto(student, membershipService.getActiveMembership(HolderType.STUDENT, student.getId()).orElse(null)))
                .toList();
        long total = studentRepository.countSearchResults(criteria);
        return new Page<>(content, criteria.getPageNumber(), criteria.getPageSize(), total);
    }

    @Override
    public ImportResultDTO bulkImport(List<StudentImportRowDTO> rows) {
        permissionEvaluator.requirePermission("PEOPLE_MANAGE");
        int successCount = 0;
        List<ImportResultDTO.RejectedRow> rejectedRows = new ArrayList<>();

        for (StudentImportRowDTO row : rows) {
            try {
                importRow(row);
                successCount++;
            } catch (Exception e) {
                rejectedRows.add(new ImportResultDTO.RejectedRow(row.rowNumber(), e.getMessage()));
            }
        }
        return new ImportResultDTO(successCount, rejectedRows);
    }

    private void importRow(StudentImportRowDTO row) {
        if (row.username() == null || row.username().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (row.studentId() == null || row.studentId().isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (row.rollNumber() == null || row.rollNumber().isBlank()) {
            throw new IllegalArgumentException("Roll number is required.");
        }

        List<Branch> branches = branchRepository.findAll();
        if (branches.isEmpty()) {
            throw new IllegalStateException("No branch configured to assign imported students to.");
        }

        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username(row.username())
                .email(row.email())
                .temporaryPassword("Temp@" + row.studentId())
                .studentId(row.studentId())
                .rollNumber(row.rollNumber())
                .department(row.department())
                .year(row.year())
                .semester(row.semester())
                .phone(row.phone())
                .branchId(branches.get(0).getId())
                .build();

        register(request);
    }

    private void assignInitialMembership(StudentRegistrationRequestDTO request, Student student) {
        Long membershipTypeId = request.getMembershipTypeId();
        if (membershipTypeId == null) {
            return;
        }
        membershipService.assignOrRenew(HolderType.STUDENT, student.getId(), membershipTypeId, defaultMembershipValidityDays);
    }

    private void applyOptionalFields(Student student, StudentRegistrationRequestDTO request) {
        student.setDepartment(request.getDepartment());
        student.setYear(request.getYear());
        student.setSemester(request.getSemester());
        student.setPhone(request.getPhone());
        student.setAddress(request.getAddress());
        student.setGuardianName(request.getGuardianName());
        student.setGuardianPhone(request.getGuardianPhone());
        if (request.getPhotoPath() != null) {
            student.setPhotoPath(request.getPhotoPath());
        }
    }

    private void ensureUsernameAndEmailAvailable(String username, String email) {
        userRepository.findByUsername(username).ifPresent(existing -> {
            throw new DuplicateResourceException("Username '" + username + "' is already taken.");
        });
        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new DuplicateResourceException("Email '" + email + "' is already registered.");
        });
    }

    private void validate(StudentRegistrationRequestDTO request) {
        ValidationResult result = studentValidator.validate(request);
        if (!result.isValid()) {
            throw new ValidationException(result.getErrors());
        }
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private StudentDTO toDto(Student student, MembershipDTO membership) {
        return StudentDTO.builder()
                .id(student.getId())
                .username(student.getUser().getUsername())
                .email(student.getUser().getEmail())
                .studentId(student.getStudentId())
                .rollNumber(student.getRollNumber())
                .department(student.getDepartment())
                .year(student.getYear())
                .semester(student.getSemester())
                .phone(student.getPhone())
                .address(student.getAddress())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .status(student.getStatus().name())
                .branchName(student.getBranch().getName())
                .photoPath(student.getPhotoPath())
                .membershipTypeName(membership != null ? membership.membershipTypeName() : null)
                .membershipStatus(membership != null ? membership.status() : null)
                .membershipExpiryDate(membership != null ? membership.expiryDate() : null)
                .build();
    }
}
