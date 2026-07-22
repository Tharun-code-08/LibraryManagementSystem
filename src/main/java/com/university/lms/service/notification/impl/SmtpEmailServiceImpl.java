package com.university.lms.service.notification.impl;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.university.lms.service.notification.EmailService;

/** Sends plain-text email over SMTP via Jakarta Mail. */
public final class SmtpEmailServiceImpl implements EmailService {

    private final String fromAddress;
    private final Session session;

    public SmtpEmailServiceImpl(String host, int port, String username, String password,
                                 String fromAddress, boolean authEnabled, boolean starttlsEnabled) {
        this.fromAddress = fromAddress;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", String.valueOf(authEnabled));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));

        this.session = authEnabled
                ? Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                })
                : Session.getInstance(props);
    }

    @Override
    public void send(String toAddress, String subject, String body) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email to " + toAddress, e);
        }
    }
}
