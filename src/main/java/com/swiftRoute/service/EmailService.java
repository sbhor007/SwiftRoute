package com.swiftRoute.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


/**
 * Email Service to send emails asynchronously.
 */
@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    /**
     * Java Mail Sender to send emails.
     */
    private JavaMailSender javaMailSender;

    /**
     * Sends an email asynchronously.
     *
     * @param to      the recipient's email address
     * @param subject the subject of the email
     * @param body    the body of the email
     * @return a CompletableFuture that completes when the email is sent
     */
    @Async
    public void sendMail(String to,String subject, String body){
        log.info("inside mail service");
        try {
            log.info("Sending email to: {} with subject: {}", to, subject);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);

       }catch (Exception e){
           e.printStackTrace();
           log.error(e.getMessage());
           throw new MailSendException("failld to send mail" + to, e);
       }
       log.info("Mail Service Completed");
    }

    /**
     * Sends an email asynchronously.
     *
     * @param to      the recipient's email address
     * @param subject the subject of the email
     * @param body    the body of the email
     * @return a {@link CompletableFuture} that completes when the email is sent,
     * with the result being a {@link String} indicating the success of the operation
     */
    @Async
    public CompletableFuture<String> sendEmailAsync(String to, String subject, String body) {
        try {
            log.info("Sending email to: {} with subject: {}", to, subject);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Email sent to: {}", to);
            return CompletableFuture.completedFuture("Email sent to: " + to);
        } catch (MailException ex) {
            // Wrap and complete exceptionally
            log.error("Failed to send email to: {}", to, ex);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new MailSendException("Failed to send email to: " + to, ex)
            );
            return future;
        }
    }

}
