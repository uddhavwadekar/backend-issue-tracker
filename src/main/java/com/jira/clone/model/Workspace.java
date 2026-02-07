package com.jira.clone.model;

import jakarta.persistence.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "workspaces")
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // EXISTING: Controls "Who can see this workspace"
    @ManyToMany
    @JoinTable(
        name = "workspace_members",
        joinColumns = @JoinColumn(name = "workspace_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // NEW: Controls "What permissions they have" (UserID -> "ADMIN" / "MEMBER")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workspace_roles", joinColumns = @JoinColumn(name = "workspace_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "role_name")
    private Map<Long, String> roles = new HashMap<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Project> projects = new ArrayList<>();

    public Workspace() {}

    // Helper to add member with role
    public void addMember(User user, String role) {
        this.members.add(user);
        this.roles.put(user.getId(), role);
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
    public Map<Long, String> getRoles() { return roles; }
    public void setRoles(Map<Long, String> roles) { this.roles = roles; }
    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }
}