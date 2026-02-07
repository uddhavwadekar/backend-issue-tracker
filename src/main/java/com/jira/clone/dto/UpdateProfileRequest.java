package com.jira.clone.dto;

public class UpdateProfileRequest {
    private String email;
    private String username;
    private String jobTitle;
    private String organization;

    public UpdateProfileRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
}