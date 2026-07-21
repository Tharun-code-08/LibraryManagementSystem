package com.university.lms.service.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import com.university.lms.dto.request.ManualFineRequestDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.Fine;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.exception.FineAlreadySettledException;
import com.university.lms.entity.FineStatus;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.PaymentRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.finance.impl.FineServiceImpl;

@ExtendWith(MockitoExtension.class)
class FineServiceImplTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AuditLogService auditLogService;

    private FineServiceImpl fineService;
    private Issue issue;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        fineService = new FineServiceImpl(fineRepository, issueRepository, paymentRepository, holderResolver,
                auditLogService, new AuthContext());

        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        BookCopy copy = new BookCopy(book, null, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        User librarian = new User("librarian", "lib@library.local", "hash", null);
        issue = new Issue(copy, membership, librarian, java.time.LocalDateTime.now().minusDays(20), java.time.LocalDateTime.now().minusDays(6));

        User studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        Student student = new Student(studentUser, "STU1001", "R1001", null);
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        lenient().when(paymentRepository.sumAmountByFineId(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsManualFineAgainstAnExistingIssue() {
        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));

        FineDTO result = fineService.createManualFine(new ManualFineRequestDTO(5L, BigDecimal.valueOf(20), "Lost library card replacement"));

        assertEquals("Clean Code", result.bookTitle());
        assertEquals("jdoe", result.memberName());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(result.amount()));
        assertEquals("MANUAL", result.reason());
    }

    @Test
    void waivingAPendingFineMarksItWaived() {
        Fine fine = new Fine(issue, BigDecimal.valueOf(45), com.university.lms.entity.FineReason.OVERDUE);
        when(fineRepository.findById(9L)).thenReturn(Optional.of(fine));

        FineDTO result = fineService.waive(9L, 100L);

        assertEquals("WAIVED", result.status());
    }

    @Test
    void cannotWaiveAnAlreadyPaidFine() {
        Fine fine = new Fine(issue, BigDecimal.valueOf(45), com.university.lms.entity.FineReason.OVERDUE);
        fine.setStatus(FineStatus.PAID);
        when(fineRepository.findById(9L)).thenReturn(Optional.of(fine));

        assertThrows(FineAlreadySettledException.class, () -> fineService.waive(9L, 100L));
    }
}
