package com.university.lms.service.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.entity.Branch;
import com.university.lms.entity.Role;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ValidationException;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.impl.StudentServiceImpl;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MembershipService membershipService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    private StudentServiceImpl studentService;
    private Branch branch;

    @BeforeEach
    void setUp() {
        AuthContext authContext = new AuthContext();
        studentService = new StudentServiceImpl(
                studentRepository, userRepository, roleRepository, branchRepository,
                passwordEncoder, membershipService, auditLogService, authContext, permissionEvaluator, 365);

        branch = new Branch("Main Campus", "MAIN", null, null);

        lenient().when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        lenient().when(studentRepository.findByStudentId(any())).thenReturn(Optional.empty());
        lenient().when(studentRepository.findByRollNumber(any())).thenReturn(Optional.empty());
        lenient().when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        lenient().when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(new Role("STUDENT", "Student borrower")));
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(membershipService.getActiveMembership(any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void registerSucceedsForValidNewStudent() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .email("jdoe@university.edu")
                .temporaryPassword("Temp@1234")
                .studentId("STU1001")
                .rollNumber("R1001")
                .branchId(1L)
                .build();

        StudentDTO result = studentService.register(request);

        assertEquals("jdoe", result.getUsername());
        assertEquals("STU1001", result.getStudentId());
        assertEquals("Main Campus", result.getBranchName());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(
                new User("jdoe", "existing@university.edu", "hash", branch)));

        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .email("jdoe@university.edu")
                .temporaryPassword("Temp@1234")
                .studentId("STU1001")
                .rollNumber("R1001")
                .branchId(1L)
                .build();

        assertThrows(DuplicateResourceException.class, () -> studentService.register(request));
    }

    @Test
    void registerRejectsMissingRequiredFields() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .build();

        assertThrows(ValidationException.class, () -> studentService.register(request));
    }
}
