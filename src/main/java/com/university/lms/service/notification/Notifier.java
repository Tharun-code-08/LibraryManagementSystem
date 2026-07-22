package com.university.lms.service.notification;

import com.university.lms.entity.User;

/** A single delivery channel (email, desktop, ...) for a notification. */
public interface Notifier {

    void notify(User recipient, String subject, String message);
}
