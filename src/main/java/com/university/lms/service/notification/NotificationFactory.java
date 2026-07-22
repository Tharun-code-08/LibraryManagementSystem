package com.university.lms.service.notification;

import com.university.lms.entity.NotificationChannel;

/** Selects the concrete {@link Notifier} for a requested {@link NotificationChannel}. */
public final class NotificationFactory {

    private final Notifier emailNotifier;
    private final Notifier desktopNotifier;

    public NotificationFactory(Notifier emailNotifier, Notifier desktopNotifier) {
        this.emailNotifier = emailNotifier;
        this.desktopNotifier = desktopNotifier;
    }

    public Notifier notifierFor(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailNotifier;
            case DESKTOP -> desktopNotifier;
        };
    }
}
