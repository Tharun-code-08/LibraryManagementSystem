package com.university.lms.service.notification.impl;

import com.university.lms.entity.User;
import com.university.lms.service.notification.EmailService;
import com.university.lms.service.notification.Notifier;

public final class EmailNotifier implements Notifier {

    private final EmailService emailService;

    public EmailNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void notify(User recipient, String subject, String message) {
        emailService.send(recipient.getEmail(), subject, message);
    }
}
