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

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    private JavaMailSender javaMailSender;

    @Async
    public void sendMail(String to,String subject, String body){
        log.info("inside mail service");
       try {
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

    @Async
    public CompletableFuture<String> sendEmailAsync(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            return CompletableFuture.completedFuture("Email sent to: " + to);
        } catch (MailException ex) {
            // Wrap and complete exceptionally
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new MailSendException("Failed to send email to: " + to, ex)
            );
            return future;
        }
    }

}
