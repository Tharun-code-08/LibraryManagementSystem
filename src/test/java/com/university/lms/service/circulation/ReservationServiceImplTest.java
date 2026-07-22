package com.university.lms.service.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.business.ReservationQueueManager;
import com.university.lms.dto.request.ReservationRequestDTO;
import com.university.lms.dto.response.ReservationDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.Faculty;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.User;
import com.university.lms.exception.NoActiveMembershipException;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.ReservationRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.impl.ReservationServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        ReservationQueueManager queueManager = new ReservationQueueManager(reservationRepository, 3);
        reservationService = new ReservationServiceImpl(reservationRepository, bookRepository, membershipRepository,
                holderResolver, queueManager, auditLogService, new AuthContext(), permissionEvaluator);
    }

    @Test
    void reservesBookForActiveFacultyMember() {
        User facultyUser = new User("profx", "profx@university.edu", "hash", null);
        Faculty faculty = new Faculty(facultyUser, "FAC1001");
        MembershipType type = new MembershipType("FACULTY_STANDARD", 10, 30, BigDecimal.valueOf(5), 3, 5);
        Membership membership = new Membership(type, HolderType.FACULTY, null, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Design Patterns", null, null, null, null, null, null, BigDecimal.TEN, null, null);

        when(facultyRepository.findByFacultyId("FAC1001")).thenReturn(Optional.of(faculty));
        when(membershipRepository.findActiveByHolder(HolderType.FACULTY, null)).thenReturn(Optional.of(membership));
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(reservationRepository.countWaitingByBookId(any())).thenReturn(0L);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationDTO result = reservationService.reserve(new ReservationRequestDTO("FAC1001", 7L));

        assertEquals("Design Patterns", result.bookTitle());
        assertEquals(1, result.queuePosition());
        assertEquals("profx", result.memberName());
    }

    @Test
    void rejectsReservationForUnknownMember() {
        when(studentRepository.findByStudentId("GHOST")).thenReturn(Optional.empty());
        when(studentRepository.findByRollNumber("GHOST")).thenReturn(Optional.empty());
        when(facultyRepository.findByFacultyId("GHOST")).thenReturn(Optional.empty());

        assertThrows(NoActiveMembershipException.class,
                () -> reservationService.reserve(new ReservationRequestDTO("GHOST", 7L)));
    }
}
