package com.university.lms.service.notification.impl;

import com.university.lms.entity.User;
import com.university.lms.service.notification.Notifier;
import com.university.lms.util.DesktopNotifier;

public final class DesktopNotifierChannel implements Notifier {

    private final DesktopNotifier desktopNotifier;

    public DesktopNotifierChannel(DesktopNotifier desktopNotifier) {
        this.desktopNotifier = desktopNotifier;
    }

    @Override
    public void notify(User recipient, String subject, String message) {
        desktopNotifier.show(subject, message);
    }
}
