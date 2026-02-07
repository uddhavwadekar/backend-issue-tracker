package com.jira.clone.dto;

public class CommentRequest {
    private String text;
    private String userEmail;
    private Long issueId;

    public CommentRequest() {}
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
}