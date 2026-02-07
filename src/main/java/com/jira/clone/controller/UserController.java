package com.jira.clone.controller;

import com.jira.clone.dto.ChangePasswordRequest;
import com.jira.clone.dto.UpdateProfileRequest;
import com.jira.clone.model.User;
import com.jira.clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(404).body("User not found");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Update Text Fields
            if(request.getUsername() != null && !request.getUsername().isEmpty()) {
                user.setUsername(request.getUsername());
            }
            user.setJobTitle(request.getJobTitle());
            user.setOrganization(request.getOrganization());
            
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(404).body("User not found.");
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("email") String email, @RequestParam("file") MultipartFile file) {
        return uploadImageToDB(email, file, "PHOTO");
    }

    @PostMapping("/profile/header")
    public ResponseEntity<?> uploadHeader(@RequestParam("email") String email, @RequestParam("file") MultipartFile file) {
        return uploadImageToDB(email, file, "HEADER");
    }

    private ResponseEntity<?> uploadImageToDB(String email, MultipartFile file, String type) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if ("PHOTO".equals(type)) user.setProfileImage(file.getBytes());
                else user.setHeaderImage(file.getBytes());
                
                userRepository.save(user);
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("DB Upload Error: " + e.getMessage());
        }
    }

    // --- Serve Images for Frontend tags <img src="..." /> ---
    @GetMapping(value = "/{id}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfilePhoto(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(User::getProfileImage).orElse(null);
    }

    @GetMapping(value = "/{id}/header", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getHeaderImage(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(User::getHeaderImage).orElse(null);
    }

    @PutMapping("/security/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Current password is incorrect.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok("Password changed successfully.");
        }
        return ResponseEntity.status(404).body("User not found.");
    }
}