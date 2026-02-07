package com.jira.clone.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Long workspaceId;

    @Column(nullable = false, unique = true)
    private String token;
    
    // NEW FEATURE: Role Assignment
    private String role; // e.g. "ADMIN", "MEMBER", "VIEWER"

    private boolean accepted = false;
    private LocalDateTime createdAt;

    public Invitation() {
        this.token = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Invitation(String email, Long workspaceId, String role) {
        this();
        this.email = email;
        this.workspaceId = workspaceId;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}