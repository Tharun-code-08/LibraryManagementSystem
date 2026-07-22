package com.university.lms.service.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.PaymentRequestDTO;
import com.university.lms.dto.response.PaymentDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.Fine;
import com.university.lms.entity.FineReason;
import com.university.lms.entity.FineStatus;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Payment;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.exception.InvalidPaymentAmountException;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.PaymentRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.finance.impl.PaymentServiceImpl;
import com.university.lms.util.ReceiptGenerator;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AuditLogService auditLogService;

    @TempDir
    private Path tempDir;

    private PaymentServiceImpl paymentService;
    private Fine fine;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        ReceiptGenerator receiptGenerator = new ReceiptGenerator(tempDir.resolve("receipts"));
        paymentService = new PaymentServiceImpl(fineRepository, paymentRepository, userRepository,
                holderResolver, receiptGenerator, auditLogService);

        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        BookCopy copy = new BookCopy(book, null, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        User librarian = new User("librarian", "lib@library.local", "hash", null);
        Issue issue = new Issue(copy, membership, librarian, java.time.LocalDateTime.now().minusDays(20), java.time.LocalDateTime.now().minusDays(6));
        fine = new Fine(issue, BigDecimal.valueOf(45), FineReason.OVERDUE);

        User studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        Student student = new Student(studentUser, "STU1001", "R1001", null);
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        lenient().when(userRepository.findById(100L)).thenReturn(Optional.of(librarian));
        lenient().when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void fullPaymentMarksFineAsPaidAndGeneratesReceipt() {
        when(fineRepository.findById(9L)).thenReturn(Optional.of(fine));
        when(paymentRepository.sumAmountByFineId(any())).thenReturn(BigDecimal.ZERO);

        PaymentDTO result = paymentService.collectPayment(new PaymentRequestDTO(9L, BigDecimal.valueOf(45), "CASH"), 100L);

        assertEquals(FineStatus.PAID, fine.getStatus());
        assertTrue(result.receiptFilePath().endsWith(".pdf"));
        assertTrue(java.nio.file.Files.exists(Path.of(result.receiptFilePath())));
    }

    @Test
    void partialPaymentMarksFineAsPartial() {
        when(fineRepository.findById(9L)).thenReturn(Optional.of(fine));
        when(paymentRepository.sumAmountByFineId(any())).thenReturn(BigDecimal.ZERO);

        paymentService.collectPayment(new PaymentRequestDTO(9L, BigDecimal.valueOf(20), "CASH"), 100L);

        assertEquals(FineStatus.PARTIAL, fine.getStatus());
    }

    @Test
    void rejectsPaymentExceedingRemainingBalance() {
        when(fineRepository.findById(9L)).thenReturn(Optional.of(fine));
        when(paymentRepository.sumAmountByFineId(any())).thenReturn(BigDecimal.ZERO);

        assertThrows(InvalidPaymentAmountException.class,
                () -> paymentService.collectPayment(new PaymentRequestDTO(9L, BigDecimal.valueOf(100), "CASH"), 100L));
    }
}
