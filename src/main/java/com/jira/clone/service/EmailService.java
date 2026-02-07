package com.jira.clone.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // required = false means app won't crash if SMTP settings are missing
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendProjectInvitation(String toEmail, String projectName, String inviter, String token) {
        String joinUrl = frontendUrl + "/join?token=" + token;
        
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Join " + projectName);
                message.setText("Invited by " + inviter + ".\nAccept here: " + joinUrl);
                
                mailSender.send(message);
                System.out.println("LOG: Real email sent to " + toEmail);
            } else {
                // Force fallback if mailSender bean is missing
                throw new Exception("SMTP not configured");
            }
        } catch (Exception e) {
            // --- FALLBACK: Print to Console so you can still dev without Mailtrap ---
            System.out.println("====================================================");
            System.out.println("⚠️  EMAIL FAILED (Development Mode Active)");
            System.out.println("   Reason: " + e.getMessage());
            System.out.println("   MAGIC LINK FOR " + toEmail + ":");
            System.out.println("   " + joinUrl);
            System.out.println("====================================================");
        }
    }
}