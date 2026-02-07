package com.jira.clone.dto;

public class SignupRequest {
    private String username;
    private String email;
    private String password;

    // Manual Getters and Setters - This fixes the "Undefined" errors in the Service
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}