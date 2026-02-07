package com.jira.clone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String type; // "ASSIGN", "COMMENT", "STATUS"
    
    @Column(name = "is_read")
    private boolean read = false;
    
    private Long relatedIssueId; 
    
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private LocalDateTime createdAt;

    public Notification() { 
        this.createdAt = LocalDateTime.now(); 
    }

    public Notification(String message, String type, User recipient, User sender, Long relatedIssueId) {
        this();
        this.message = message;
        this.type = type;
        this.recipient = recipient;
        this.sender = sender;
        this.relatedIssueId = relatedIssueId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    // FIX: Added this setter to resolve the error in NotificationService
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getRelatedIssueId() { return relatedIssueId; }
    public void setRelatedIssueId(Long relatedIssueId) { this.relatedIssueId = relatedIssueId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}