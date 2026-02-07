package com.jira.clone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String priority; 
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private boolean dueDateReminder;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "issue_watchers",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> watchers = new HashSet<>();
    
    public void addWatcher(User user) {
        this.watchers.add(user);
    }
    
    public Set<User> getWatchers() {
        return watchers;
    }
    
    public void setWatchers(Set<User> watchers) {
        this.watchers = watchers;
    }

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    // --- NEW FIELD: The person who created the ticket ---
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "issue_assignees",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Attachment> attachments = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "issue_labels", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Checklist> checklists = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "column_id")
    @JsonIgnore 
    private ColumnEntity column;

    private LocalDateTime createdAt;

    public Issue() { this.createdAt = LocalDateTime.now(); }

    public void addAssignee(User user) { this.assignees.add(user); }
    public void removeAssignee(User user) { this.assignees.remove(user); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    // Getter/Setter for Reporter
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    
    public Set<User> getAssignees() { return assignees; }
    public void setAssignees(Set<User> assignees) { this.assignees = assignees; }
    public ColumnEntity getColumn() { return column; }
    public void setColumn(ColumnEntity column) { this.column = column; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public boolean isDueDateReminder() { return dueDateReminder; }
    public void setDueDateReminder(boolean dueDateReminder) { this.dueDateReminder = dueDateReminder; }
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    public List<Checklist> getChecklists() { return checklists; }
    public void setChecklists(List<Checklist> checklists) { this.checklists = checklists; }
}