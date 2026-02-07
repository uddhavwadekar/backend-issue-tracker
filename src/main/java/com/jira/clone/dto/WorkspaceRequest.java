package com.jira.clone.dto;

public class WorkspaceRequest {
    private String name;
    private String description;
    private String email;

    // 1. Default Constructor (Crucial for JSON)
    public WorkspaceRequest() {}

    // 2. Getters and Setters (Crucial for Data Access)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}