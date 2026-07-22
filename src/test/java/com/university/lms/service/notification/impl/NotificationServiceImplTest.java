package com.university.lms.service.notification.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Notification;
import com.university.lms.entity.NotificationCategory;
import com.university.lms.entity.NotificationChannel;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.Return;
import com.university.lms.entity.ReturnCondition;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.NotificationRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.service.notification.NotificationFactory;
import com.university.lms.service.notification.Notifier;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private Notifier notifier;

    private NotificationServiceImpl notificationService;
    private Issue issue;
    private User studentUser;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        notificationService = new NotificationServiceImpl(
                notificationRepository, issueRepository, holderResolver, notificationFactory);

        lenient().when(notificationFactory.notifierFor(any())).thenReturn(notifier);
        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        BookCopy copy = new BookCopy(book, null, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        User librarian = new User("librarian", "lib@library.local", "hash", null);
        issue = new Issue(copy, membership, librarian, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6));

        studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        Student student = new Student(studentUser, "STU1001", "R1001", null);
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
    }

    @Test
    void sendIssueReceiptDispatchesBothChannelsAndPersists() {
        notificationService.sendIssueReceipt(issue);

        verify(notifier, times(2)).notify(eq(studentUser), any(), any());
        verify(notificationRepository, times(4)).save(any(Notification.class));
    }

    @Test
    void sendReturnReceiptUsesFineCategoryWhenFined() {
        Return returnRecord = new Return(issue, issue.getIssuedBy(), LocalDateTime.now(), ReturnCondition.GOOD, null);

        notificationService.sendReturnReceipt(returnRecord, BigDecimal.valueOf(20));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifier, times(2)).notify(eq(studentUser), any(), messageCaptor.capture());
        assertEquals(true, messageCaptor.getValue().contains("fine"));
    }

    @Test
    void sendReservationReadyDispatchesToHolder() {
        Book book = new Book("978-2", "Refactoring", null, null, null, null, null, null, BigDecimal.valueOf(40), null, null);
        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Reservation reservation = new Reservation(book, membership, LocalDateTime.now(), 1);
        reservation.setExpiresAt(LocalDateTime.now().plusDays(3));

        notificationService.sendReservationReady(reservation);

        verify(notifier, times(2)).notify(eq(studentUser), any(), any());
    }

    @Test
    void overdueSweepSkipsHoldersAlreadyRemindedToday() {
        when(issueRepository.findOverdueOpenIssues(any())).thenReturn(List.of(issue));
        when(notificationRepository.existsByUserIdAndCategorySince(
                eq(studentUser.getId()), eq(NotificationCategory.OVERDUE), any())).thenReturn(true);

        int sent = notificationService.runOverdueReminderSweep();

        assertEquals(0, sent);
        verify(notifier, never()).notify(any(), any(), any());
    }

    @Test
    void overdueSweepSendsForHoldersNotYetReminded() {
        when(issueRepository.findOverdueOpenIssues(any())).thenReturn(List.of(issue));
        when(notificationRepository.existsByUserIdAndCategorySince(
                eq(studentUser.getId()), eq(NotificationCategory.OVERDUE), any())).thenReturn(false);

        int sent = notificationService.runOverdueReminderSweep();

        assertEquals(1, sent);
        verify(notifier, times(2)).notify(eq(studentUser), any(), any());
    }

    @Test
    void markReadDelegatesToRepository() {
        notificationService.markRead(42L);

        verify(notificationRepository).markRead(42L);
    }
}
