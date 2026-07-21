package com.university.lms.service.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.business.BorrowLimitValidator;
import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.business.OverdueFineStrategy;
import com.university.lms.business.ReservationQueueManager;
import com.university.lms.dto.request.IssueRequestDTO;
import com.university.lms.dto.request.ReturnRequestDTO;
import com.university.lms.dto.response.IssueResultDTO;
import com.university.lms.dto.response.ReturnResultDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Fine;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.IssueStatus;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.ReservationRepository;
import com.university.lms.repository.ReturnRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.impl.IssueServiceImpl;
import com.university.lms.service.circulation.impl.ReturnServiceImpl;

/**
 * Exercises the full Issue -> overdue Return -> Fine lifecycle against the real business-layer
 * classes (BorrowLimitValidator, OverdueFineStrategy, MembershipHolderResolver), with only the
 * repository layer mocked — the closest available substitute for a database-backed integration
 * test in an environment without a live MySQL instance.
 */
@ExtendWith(MockitoExtension.class)
class CirculationLifecycleTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AuditLogService auditLogService;

    private IssueServiceImpl issueService;
    private ReturnServiceImpl returnService;

    private MembershipType membershipType;
    private Membership membership;
    private Student student;
    private Book book;
    private BookCopy copy;
    private User librarian;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        BorrowLimitValidator borrowLimitValidator = new BorrowLimitValidator();
        issueService = new IssueServiceImpl(issueRepository, bookCopyRepository, membershipRepository,
                userRepository, holderResolver, borrowLimitValidator, auditLogService);

        OverdueFineStrategy fineStrategy = new OverdueFineStrategy();
        ReservationQueueManager queueManager = new ReservationQueueManager(reservationRepository, 3);
        returnService = new ReturnServiceImpl(issueRepository, bookCopyRepository, returnRepository,
                fineRepository, userRepository, fineStrategy, queueManager, holderResolver, auditLogService);

        membershipType = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        User studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        student = new Student(studentUser, "STU1001", "R1001", null);
        membership = new Membership(membershipType, HolderType.STUDENT, 1L, LocalDate.now().minusDays(60), LocalDate.now().plusDays(300));
        book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        copy = new BookCopy(book, null, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        librarian = new User("librarian", "lib@library.local", "hash", null);

        when(studentRepository.findByStudentId("STU1001")).thenReturn(Optional.of(student));
        when(membershipRepository.findActiveByHolder(HolderType.STUDENT, null)).thenReturn(Optional.of(membership));
        when(bookCopyRepository.findByBarcode("BC100")).thenReturn(Optional.of(copy));
        when(issueRepository.countOpenByMembershipId(any())).thenReturn(0L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(librarian));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookCopyRepository.save(any(BookCopy.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void issuingThenReturningLateCreatesAnOverdueFine() {
        IssueResultDTO issueResult = issueService.issueBook(new IssueRequestDTO("STU1001", "BC100"), 100L);

        assertEquals("Clean Code", issueResult.bookTitle());
        assertEquals(BookCopyStatus.ISSUED, copy.getStatus());

        // Simulate 10 days having passed: fabricate the now-overdue open issue directly, since
        // IssueServiceImpl always computes the due date from "now" + the membership's loan period.
        Issue overdueIssue = new Issue(copy, membership, librarian,
                LocalDateTime.now().minusDays(24), LocalDateTime.now().minusDays(10));
        when(issueRepository.findOpenByCopyId(copy.getId())).thenReturn(Optional.of(overdueIssue));
        when(returnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.findWaitingByBookId(any())).thenReturn(List.of());

        ReturnResultDTO returnResult = returnService.returnBook(
                new ReturnRequestDTO("BC100", "GOOD", null), 100L);

        assertEquals(0, BigDecimal.valueOf(45).compareTo(returnResult.fineAmount()));
        assertEquals(BookCopyStatus.AVAILABLE, copy.getStatus());
        assertEquals(IssueStatus.RETURNED, overdueIssue.getStatus());

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(fineCaptor.capture());
        assertEquals(0, BigDecimal.valueOf(45).compareTo(fineCaptor.getValue().getAmount()));
    }

    @Test
    void issuingThenReturningOnTimeCreatesNoFine() {
        issueService.issueBook(new IssueRequestDTO("STU1001", "BC100"), 100L);

        Issue onTimeIssue = new Issue(copy, membership, librarian,
                LocalDateTime.now(), LocalDateTime.now().plusDays(14));
        when(issueRepository.findOpenByCopyId(copy.getId())).thenReturn(Optional.of(onTimeIssue));
        when(returnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.findWaitingByBookId(any())).thenReturn(List.of());

        ReturnResultDTO returnResult = returnService.returnBook(
                new ReturnRequestDTO("BC100", "GOOD", null), 100L);

        assertEquals(0, BigDecimal.ZERO.compareTo(returnResult.fineAmount()));
        assertTrue(returnResult.fineAmount().compareTo(BigDecimal.ZERO) == 0);
    }
}
