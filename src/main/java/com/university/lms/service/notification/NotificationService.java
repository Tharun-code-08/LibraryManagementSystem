package com.university.lms.service.notification;

import java.math.BigDecimal;
import java.util.List;

import com.university.lms.dto.response.NotificationDTO;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.Return;

public interface NotificationService {

    void sendIssueReceipt(Issue issue);

    void sendReturnReceipt(Return returnRecord, BigDecimal fineAmount);

    void sendReservationReady(Reservation reservation);

    void sendOverdueReminder(Issue issue);

    /** Scans every open-overdue issue and reminds its holder, skipping anyone already reminded
     *  today. Entry point for the nightly scheduled sweep. @return how many reminders were sent. */
    int runOverdueReminderSweep();

    List<NotificationDTO> listForUser(Long userId, int limit);

    long countUnread(Long userId);

    void markRead(Long notificationId);
}
