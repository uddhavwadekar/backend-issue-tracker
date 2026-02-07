package com.jira.clone.controller;

import com.jira.clone.dto.ProjectRequest;
import com.jira.clone.model.*;
import com.jira.clone.repository.*;
import com.jira.clone.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ColumnRepository columnRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private EmailService emailService;
    @Autowired private WorkspaceRepository workspaceRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest request) {
        if (request.getWorkspaceId() == null) {
            return ResponseEntity.badRequest().body("Error: Workspace ID is required.");
        }

        if (projectRepository.existsByProjectKey(request.getProjectKey().toUpperCase())) {
            return ResponseEntity.badRequest().body("Project key already exists.");
        }

        Optional<User> ownerOpt = userRepository.findByEmail(request.getEmail());
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(request.getWorkspaceId());

        if (ownerOpt.isPresent() && workspaceOpt.isPresent()) {
            User owner = ownerOpt.get();
            Workspace workspace = workspaceOpt.get();

            Project project = new Project();
            project.setName(request.getName());
            project.setProjectKey(request.getProjectKey().toUpperCase());
            project.setDescription(request.getDescription());
            project.setOwner(owner);
            project.setWorkspace(workspace);
            project.addMember(owner);

            Project savedProject = projectRepository.save(project);
            try {
                columnRepository.save(new ColumnEntity("To Do", 1, savedProject));
                columnRepository.save(new ColumnEntity("In Progress", 2, savedProject));
                columnRepository.save(new ColumnEntity("Done", 3, savedProject));
            } catch (Exception e) {
                System.err.println("Error creating default columns: " + e.getMessage());
            }

            
            return ResponseEntity.ok(savedProject);
        } else {
            return ResponseEntity.status(404).body("User or Workspace not found.");
        }
    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable Long projectId, 
                                          @RequestParam String email, 
                                          @RequestParam String inviterEmail) {
        // Invitations should ideally go to the Workspace, not just the Project.
        // But for backward compatibility or direct invites:
        return ResponseEntity.status(400).body("Please use the Workspace Invite button instead.");
    }

    @GetMapping("/user")
    public ResponseEntity<List<Project>> getUserProjects(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(projectRepository.findProjectsForUser(userOpt.get().getId()));
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    
    /**
     * FIX: Using if/else to avoid Optional/Lambda Type Mismatch errors.
     * This combines Workspace Members + Project Members so they appear in the dropdown.
     */
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<User>> getProjectMembers(@PathVariable Long projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            Set<User> teamMembers = new HashSet<>();
            
            // 1. Add direct project members
            if (project.getMembers() != null) {
                teamMembers.addAll(project.getMembers());
            }
            
            // 2. Add WORKSPACE members (Everyone in the team can be assigned)
            if (project.getWorkspace() != null && project.getWorkspace().getMembers() != null) {
                teamMembers.addAll(project.getWorkspace().getMembers());
            }
            
            return ResponseEntity.ok(new ArrayList<>(teamMembers));
        }
        
        return ResponseEntity.ok(new ArrayList<>());
    }
}