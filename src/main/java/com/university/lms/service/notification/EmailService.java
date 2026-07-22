package com.university.lms.service.notification;

/** Outbound SMTP email — a thin, swappable seam over the transport (see {@code SmtpEmailServiceImpl}). */
public interface EmailService {

    void send(String toAddress, String subject, String body);
}
