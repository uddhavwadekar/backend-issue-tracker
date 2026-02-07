package com.jira.clone.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type; // "FILE" or "LINK"

    // For LINKS, we store the URL here.
    // For FILES, we store the API endpoint here (or generate it dynamically).
    private String linkUrl; 

    @JsonIgnore
    @Column(length = 10000000) // 10MB limit
    private byte[] data;
    
    @ManyToOne
    @JoinColumn(name = "issue_id")
    @JsonIgnore
    private Issue issue;

    private LocalDateTime uploadedAt;

    public Attachment() { this.uploadedAt = LocalDateTime.now(); }

    public Attachment(String name, String type, Issue issue) {
        this();
        this.name = name;
        this.type = type;
        this.issue = issue;
    }

    // --- VIRTUAL URL GENERATOR ---
    // This tells the frontend where to find the image
    @JsonProperty("url")
    public String getUrl() {
        if ("FILE".equals(type)) {
            // Points to the controller endpoint that serves the bytes
            return "http://localhost:8081/api/issues/attachments/" + id;
        }
        return linkUrl;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}