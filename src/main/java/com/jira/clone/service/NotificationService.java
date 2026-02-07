package com.jira.clone.service;

import com.jira.clone.model.*;
import com.jira.clone.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    public void createNotification(User recipient, User sender, String type, String message, Long issueId) {
        System.out.println("--- DEBUG NOTIFICATION START ---");
        
        if (recipient == null || sender == null) {
            System.out.println("ERROR: Recipient or Sender is null. Cannot send.");
            return;
        }

        System.out.println("Sender: " + sender.getEmail());
        System.out.println("Recipient: " + recipient.getEmail());

        // LOGIC: Don't notify if I assign myself
        if (recipient.getId().equals(sender.getId())) {
            System.out.println("SKIPPED: User assigned themselves. No notification needed.");
            return;
        }
        
        try {
            Notification n = new Notification();
            n.setMessage(message);
            n.setType(type);
            n.setRecipient(recipient);
            n.setSender(sender);
            n.setRelatedIssueId(issueId);
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(n);
            System.out.println("SUCCESS: Notification saved to Database! ID: " + n.getId());
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR SAVING NOTIFICATION: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("--- DEBUG NOTIFICATION END ---");
    }

    public void notifyAssignees(Issue issue, User sender, String type, String message) {
        if (issue.getAssignees() == null || issue.getAssignees().isEmpty()) return;

        for (User assignee : issue.getAssignees()) {
            createNotification(assignee, sender, type, message, issue.getId());
        }
    }
}