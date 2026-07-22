package com.university.lms.service.notification.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.response.NotificationDTO;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.Notification;
import com.university.lms.entity.NotificationCategory;
import com.university.lms.entity.NotificationChannel;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.Return;
import com.university.lms.entity.User;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.NotificationRepository;
import com.university.lms.service.notification.NotificationFactory;
import com.university.lms.service.notification.NotificationService;

public final class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final IssueRepository issueRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final NotificationFactory notificationFactory;

    public NotificationServiceImpl(NotificationRepository notificationRepository, IssueRepository issueRepository,
                                    MembershipHolderResolver membershipHolderResolver,
                                    NotificationFactory notificationFactory) {
        this.notificationRepository = notificationRepository;
        this.issueRepository = issueRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.notificationFactory = notificationFactory;
    }

    @Override
    public void sendIssueReceipt(Issue issue) {
        String title = issue.getBookCopy().getBook().getTitle();
        dispatchToHolder(issue.getMembership(), NotificationCategory.GENERAL,
                "Book Issued: " + title,
                "You have borrowed \"" + title + "\". It is due back on " + issue.getDueDate().toLocalDate() + ".");
    }

    @Override
    public void sendReturnReceipt(Return returnRecord, BigDecimal fineAmount) {
        Issue issue = returnRecord.getIssue();
        String title = issue.getBookCopy().getBook().getTitle();
        boolean fined = fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) > 0;
        String message = "You returned \"" + title + "\" on " + returnRecord.getReturnDate().toLocalDate() + "."
                + (fined ? " A fine of " + fineAmount + " was applied to your account." : "");
        dispatchToHolder(issue.getMembership(), fined ? NotificationCategory.FINE : NotificationCategory.GENERAL,
                "Book Returned: " + title, message);
    }

    @Override
    public void sendReservationReady(Reservation reservation) {
        String title = reservation.getBook().getTitle();
        dispatchToHolder(reservation.getMembership(), NotificationCategory.RESERVATION_READY,
                "Reservation Ready: " + title,
                "Your reserved copy of \"" + title + "\" is now available. Please collect it by "
                        + reservation.getExpiresAt().toLocalDate() + ".");
    }

    @Override
    public void sendOverdueReminder(Issue issue) {
        String title = issue.getBookCopy().getBook().getTitle();
        dispatchToHolder(issue.getMembership(), NotificationCategory.OVERDUE,
                "Overdue: " + title,
                "\"" + title + "\" was due on " + issue.getDueDate().toLocalDate()
                        + " and is now overdue. Please return it as soon as possible.");
    }

    @Override
    public int runOverdueReminderSweep() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<Issue> overdueIssues = issueRepository.findOverdueOpenIssues(now);
        int sentCount = 0;
        for (Issue issue : overdueIssues) {
            Membership membership = issue.getMembership();
            Optional<User> recipient = membershipHolderResolver.resolveUser(membership.getHolderType(), membership.getHolderId());
            if (recipient.isEmpty()) {
                continue;
            }
            if (notificationRepository.existsByUserIdAndCategorySince(
                    recipient.get().getId(), NotificationCategory.OVERDUE, todayStart)) {
                continue;
            }
            sendOverdueReminder(issue);
            sentCount++;
        }
        return sentCount;
    }

    @Override
    public List<NotificationDTO> listForUser(Long userId, int limit) {
        return notificationRepository.findByUserId(userId, limit).stream().map(this::toDto).toList();
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public void markRead(Long notificationId) {
        notificationRepository.markRead(notificationId);
    }

    private void dispatchToHolder(Membership membership, NotificationCategory category, String subject, String message) {
        Optional<User> recipient = membershipHolderResolver.resolveUser(membership.getHolderType(), membership.getHolderId());
        if (recipient.isEmpty()) {
            return;
        }
        User user = recipient.get();
        dispatch(user, category, NotificationChannel.EMAIL, subject, message);
        dispatch(user, category, NotificationChannel.DESKTOP, subject, message);
    }

    private void dispatch(User user, NotificationCategory category, NotificationChannel channel,
                           String subject, String message) {
        Notification notification = notificationRepository.save(new Notification(user, channel, category, message));
        try {
            notificationFactory.notifierFor(channel).notify(user, subject, message);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.warn("Failed to dispatch {} notification to user {}: {}", channel, user.getId(), e.getMessage());
        }
    }

    private NotificationDTO toDto(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getCategory().name(),
                notification.getType().name(), notification.getMessage(), notification.isRead(),
                notification.getCreatedAt(), notification.getSentAt());
    }
}
