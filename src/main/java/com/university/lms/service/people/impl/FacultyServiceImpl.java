package com.university.lms.service.people.impl;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.FacultyRegistrationRequestDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.MembershipDTO;
import com.university.lms.entity.Faculty;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Role;
import com.university.lms.entity.User;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.exception.ValidationException;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.MembershipService;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;
import com.university.lms.validation.impl.FacultyValidator;

public final class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipService membershipService;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final int defaultMembershipValidityDays;

    private final Validator<FacultyRegistrationRequestDTO> facultyValidator = new FacultyValidator();

    public FacultyServiceImpl(FacultyRepository facultyRepository, UserRepository userRepository,
                              RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                              MembershipService membershipService, AuditLogService auditLogService,
                              AuthContext authContext, int defaultMembershipValidityDays) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.membershipService = membershipService;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.defaultMembershipValidityDays = defaultMembershipValidityDays;
    }

    @Override
    public FacultyDTO register(FacultyRegistrationRequestDTO request) {
        validate(request);
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered.");
        });
        facultyRepository.findByFacultyId(request.getFacultyId()).ifPresent(existing -> {
            throw new DuplicateResourceException("A faculty member with ID " + request.getFacultyId() + " already exists.");
        });

        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseThrow(() -> new IllegalStateException("FACULTY role is not seeded."));

        User user = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getTemporaryPassword()), null);
        user.getRoles().add(facultyRole);
        User savedUser = userRepository.save(user);

        Faculty faculty = new Faculty(savedUser, request.getFacultyId());
        applyOptionalFields(faculty, request);

        Faculty saved = facultyRepository.save(faculty);
        if (request.getMembershipTypeId() != null) {
            membershipService.assignOrRenew(HolderType.FACULTY, saved.getId(), request.getMembershipTypeId(), defaultMembershipValidityDays);
        }

        auditLogService.log(currentUserId(), "FACULTY_REGISTERED", "Faculty", saved.getId());
        return toDto(saved, membershipService.getActiveMembership(HolderType.FACULTY, saved.getId()).orElse(null));
    }

    @Override
    public FacultyDTO update(FacultyRegistrationRequestDTO request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Faculty id is required for an update.");
        }
        Faculty faculty = facultyRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", request.getId()));

        facultyRepository.findByFacultyId(request.getFacultyId())
                .filter(other -> !other.getId().equals(request.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A faculty member with ID " + request.getFacultyId() + " already exists.");
                });

        faculty.setFacultyId(request.getFacultyId());
        applyOptionalFields(faculty, request);

        Faculty saved = facultyRepository.save(faculty);
        auditLogService.log(currentUserId(), "FACULTY_UPDATED", "Faculty", saved.getId());
        return toDto(saved, membershipService.getActiveMembership(HolderType.FACULTY, saved.getId()).orElse(null));
    }

    @Override
    public Optional<FacultyDTO> getById(Long id) {
        return facultyRepository.findById(id)
                .map(faculty -> toDto(faculty, membershipService.getActiveMembership(HolderType.FACULTY, faculty.getId()).orElse(null)));
    }

    @Override
    public List<FacultyDTO> listAll() {
        return facultyRepository.findAll().stream()
                .map(faculty -> toDto(faculty, membershipService.getActiveMembership(HolderType.FACULTY, faculty.getId()).orElse(null)))
                .toList();
    }

    private void applyOptionalFields(Faculty faculty, FacultyRegistrationRequestDTO request) {
        faculty.setDepartment(request.getDepartment());
        faculty.setDesignation(request.getDesignation());
        faculty.setPhone(request.getPhone());
        faculty.setOffice(request.getOffice());
    }

    private void validate(FacultyRegistrationRequestDTO request) {
        ValidationResult result = facultyValidator.validate(request);
        if (!result.isValid()) {
            throw new ValidationException(result.getErrors());
        }
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private FacultyDTO toDto(Faculty faculty, MembershipDTO membership) {
        return FacultyDTO.builder()
                .id(faculty.getId())
                .username(faculty.getUser().getUsername())
                .email(faculty.getUser().getEmail())
                .facultyId(faculty.getFacultyId())
                .department(faculty.getDepartment())
                .designation(faculty.getDesignation())
                .phone(faculty.getPhone())
                .office(faculty.getOffice())
                .membershipTypeName(membership != null ? membership.membershipTypeName() : null)
                .membershipStatus(membership != null ? membership.status() : null)
                .membershipExpiryDate(membership != null ? membership.expiryDate() : null)
                .build();
    }
}
