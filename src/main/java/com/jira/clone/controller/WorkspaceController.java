package com.jira.clone.controller;

import com.jira.clone.dto.WorkspaceRequest;
import com.jira.clone.dto.AnalyticsDTO;
import com.jira.clone.model.*;
import com.jira.clone.repository.*;
import com.jira.clone.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkspaceController {

    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private EmailService emailService;
    @Autowired private IssueRepository issueRepository; // Added this

    @PostMapping("/create")
    public ResponseEntity<?> createWorkspace(@RequestBody WorkspaceRequest request) {
        if (request.getName() == null) return ResponseEntity.badRequest().body("Name required");
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            Workspace ws = new Workspace();
            ws.setName(request.getName());
            ws.setDescription(request.getDescription());
            ws.setOwner(userOpt.get());
            ws.addMember(userOpt.get(), "ADMIN"); 
            return ResponseEntity.ok(workspaceRepository.save(ws));
        }
        return ResponseEntity.status(404).body("User not found");
    }

    @PostMapping("/{workspaceId}/invite")
    public ResponseEntity<?> inviteToWorkspace(@PathVariable Long workspaceId, 
                                               @RequestParam String email, 
                                               @RequestParam String inviterEmail,
                                               @RequestParam(defaultValue = "MEMBER") String role) {
        Optional<Workspace> wsOpt = workspaceRepository.findById(workspaceId);
        if (wsOpt.isPresent()) {
            Workspace workspace = wsOpt.get();
            Invitation invite = new Invitation(email, workspaceId, role);
            invitationRepository.save(invite);
            emailService.sendProjectInvitation(email, workspace.getName(), inviterEmail, invite.getToken());
            return ResponseEntity.ok("Invitation sent");
        }
        return ResponseEntity.status(404).body("Workspace not found");
    }

    @GetMapping("/user")
    public ResponseEntity<List<Workspace>> getUserWorkspaces(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(workspaceRepository.findAllForUser(userOpt.get().getId()));
        }
        return ResponseEntity.ok(new ArrayList<>());
    }
    
    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long workspaceId, 
                                          @PathVariable Long userId, 
                                          @RequestParam String requesterEmail) {
        Optional<Workspace> wsOpt = workspaceRepository.findById(workspaceId);
        Optional<User> targetOpt = userRepository.findById(userId);
        
        if (wsOpt.isPresent() && targetOpt.isPresent()) {
            Workspace ws = wsOpt.get();
            ws.getMembers().remove(targetOpt.get());
            ws.getRoles().remove(userId);
            workspaceRepository.save(ws);
            return ResponseEntity.ok("Member removed.");
        }
        return ResponseEntity.status(404).body("Not found.");
    }

    // --- ANALYTICS ENDPOINT ---
    @GetMapping("/{workspaceId}/analytics")
    public ResponseEntity<?> getWorkspaceAnalytics(@PathVariable Long workspaceId) {
        List<Issue> issues = issueRepository.findAllByWorkspaceId(workspaceId);

        int total = issues.size();
        int completed = 0;
        int high = 0;

        Map<String, Integer> statusMap = new HashMap<>();
        Map<String, Integer> priorityMap = new HashMap<>();
        Map<String, Integer> workloadMap = new HashMap<>();

        for (Issue i : issues) {
            // Status
            String status = i.getColumn() != null ? i.getColumn().getName() : "Unknown";
            statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
            if ("Done".equalsIgnoreCase(status) || "Complete".equalsIgnoreCase(status)) completed++;

            // Priority
            String priority = i.getPriority() != null ? i.getPriority() : "MEDIUM";
            priorityMap.put(priority, priorityMap.getOrDefault(priority, 0) + 1);
            if ("HIGH".equalsIgnoreCase(priority)) high++;

            // Workload
            for (User u : i.getAssignees()) {
                String name = u.getUsername();
                workloadMap.put(name, workloadMap.getOrDefault(name, 0) + 1);
            }
        }

        return ResponseEntity.ok(new AnalyticsDTO(total, completed, high, statusMap, priorityMap, workloadMap));
    }
}