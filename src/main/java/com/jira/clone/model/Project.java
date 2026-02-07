package com.jira.clone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import this

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String projectKey; 
    private String description;

    // --- FIX: Add @JsonIgnore here ---
    @ManyToOne
    @JoinColumn(name = "workspace_id")
    @JsonIgnore 
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    private LocalDateTime createdAt;

    public Project() { this.createdAt = LocalDateTime.now(); }

    public void addMember(User user) { this.members.add(user); }

    // --- CRITICAL: Manually expose ID so Frontend knows where this project belongs ---
    public Long getWorkspaceId() {
        return workspace != null ? workspace.getId() : null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProjectKey() { return projectKey; }
    public void setProjectKey(String projectKey) { this.projectKey = projectKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
}