package com.jira.clone.controller;

import com.jira.clone.dto.*;
import com.jira.clone.model.*;
import com.jira.clone.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) return ResponseEntity.badRequest().body("Email exists");
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        processPendingInvites(savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .map(user -> {
                if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    processPendingInvites(user);
                    return ResponseEntity.ok(user);
                }
                return ResponseEntity.status(401).body("Invalid credentials");
            }).orElse(ResponseEntity.status(404).body("User not found"));
    }

    private void processPendingInvites(User user) {
        List<Invitation> invites = invitationRepository.findByEmailAndAccepted(user.getEmail(), false);
        for (Invitation invite : invites) {
            workspaceRepository.findById(invite.getWorkspaceId()).ifPresent(workspace -> {
                if (!workspace.getMembers().contains(user)) {
                    // APPLY ROLE FROM INVITATION
                    String roleToAssign = invite.getRole() != null ? invite.getRole() : "MEMBER";
                    workspace.addMember(user, roleToAssign);
                    workspaceRepository.save(workspace);
                }
                invite.setAccepted(true);
                invitationRepository.save(invite);
            });
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() { return ResponseEntity.ok(userRepository.findAll()); }
}