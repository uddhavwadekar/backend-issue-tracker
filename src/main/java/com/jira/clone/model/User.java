package com.jira.clone.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    // Profile Fields
    private String jobTitle;
    private String organization;

    // Images stored as Binary Large Objects (BLOBs)
    @JsonIgnore
    @Column(length = 10000000) // Max 10MB
    private byte[] profileImage;

    @JsonIgnore
    @Column(length = 10000000)
    private byte[] headerImage;

    public User() {}

    // --- Virtual Properties for Frontend ---
    @JsonProperty("hasProfileImage")
    public boolean hasProfileImage() {
        return profileImage != null && profileImage.length > 0;
    }

    @JsonProperty("hasHeaderImage")
    public boolean hasHeaderImage() {
        return headerImage != null && headerImage.length > 0;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public byte[] getHeaderImage() { return headerImage; }
    public void setHeaderImage(byte[] headerImage) { this.headerImage = headerImage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}