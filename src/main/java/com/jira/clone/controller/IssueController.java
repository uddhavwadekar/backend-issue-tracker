package com.jira.clone.controller;

import com.jira.clone.dto.IssueRequest;
import com.jira.clone.model.*;
import com.jira.clone.repository.*;
import com.jira.clone.service.ActivityLogService;
import com.jira.clone.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:3000")
public class IssueController {

    @Autowired private IssueRepository issueRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ColumnRepository columnRepository;
    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private ChecklistItemRepository checklistItemRepository;
    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private ActivityLogService activityLogService;
    @Autowired private ActivityLogRepository activityLogRepository;

    @GetMapping("/project/{projectId}")
    public List<Issue> getIssuesByProject(@PathVariable Long projectId) {
        return issueRepository.findByProjectId(projectId);
    }

    @PostMapping("/create/{projectId}")
    public ResponseEntity<?> createIssue(@RequestBody IssueRequest request, @PathVariable Long projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            Issue issue = new Issue();
            issue.setSummary(request.getSummary());
            issue.setDescription(request.getDescription());
            issue.setPriority(request.getPriority());
            issue.setProject(project);

            List<ColumnEntity> cols = columnRepository.findByProjectIdOrderByPositionAsc(projectId);
            if (!cols.isEmpty()) {
                issue.setColumn(cols.get(0));
            }

            // Determine Creator
            User creator = null;
            if (request.getCreatorEmail() != null) {
                creator = userRepository.findByEmail(request.getCreatorEmail()).orElse(project.getOwner());
            } else {
                creator = project.getOwner();
            }
            issue.setReporter(creator);
            issue.addWatcher(creator);

            // Handle Assignee
            User assignee = null;
            if (request.getAssigneeEmail() != null && !request.getAssigneeEmail().isEmpty()) {
                Optional<User> userOpt = userRepository.findByEmail(request.getAssigneeEmail());
                if (userOpt.isPresent()) {
                    assignee = userOpt.get();
                    issue.addAssignee(assignee);
                    issue.addWatcher(assignee);
                }
            }

            Issue savedIssue = issueRepository.save(issue);

            // --- LOG CREATION ---
            activityLogService.log(creator, savedIssue, "created this issue");

            if (assignee != null) {
                notificationService.createNotification(assignee, creator, "ASSIGN", creator.getUsername() + " assigned you", savedIssue.getId());
                // --- LOG ASSIGNMENT ---
                activityLogService.log(creator, savedIssue, "assigned to " + assignee.getUsername());
            }

            return ResponseEntity.ok(savedIssue);
        }
        return ResponseEntity.status(404).body("Project not found");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, 
                                          @RequestParam String status,
                                          @RequestParam(required = false) String moverEmail) {
        Optional<Issue> issueOpt = issueRepository.findById(id);
        
        if (issueOpt.isPresent()) {
            Issue issue = issueOpt.get();
            String oldStatus = issue.getColumn() != null ? issue.getColumn().getName() : "None";
            
            List<ColumnEntity> cols = columnRepository.findByProjectIdOrderByPositionAsc(issue.getProject().getId());
            Optional<ColumnEntity> target = cols.stream().filter(c -> c.getName().equalsIgnoreCase(status)).findFirst();
                
            if (target.isPresent()) {
                issue.setColumn(target.get());
                Issue saved = issueRepository.save(issue);
                
                User mover = null;
                if (moverEmail != null) {
                    mover = userRepository.findByEmail(moverEmail).orElse(issue.getProject().getOwner());
                } else {
                    mover = issue.getProject().getOwner();
                }

                // --- LOG MOVEMENT ---
                activityLogService.log(mover, saved, "moved from " + oldStatus + " to " + status);
                
                // Notifications
                String message = mover.getUsername() + " moved issue '" + saved.getSummary() + "' to " + status;
                notificationService.notifyAssignees(saved, mover, "STATUS", message);

                if (issue.getReporter() != null) {
                    notificationService.createNotification(issue.getReporter(), mover, "STATUS", message, saved.getId());
                }
                
                return ResponseEntity.ok(saved);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ActivityLog>> getIssueActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityLogRepository.findByIssueIdOrderByTimestampDesc(id));
    }
    
    
    @PutMapping("/{id}/description")
    public ResponseEntity<?> updateDescription(@PathVariable Long id, @RequestBody String description) {
        return issueRepository.findById(id).map(issue -> {
            issue.setDescription(description);
            return ResponseEntity.ok(issueRepository.save(issue));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/duedate")
    public ResponseEntity<?> updateDueDate(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Optional<Issue> issueOpt = issueRepository.findById(id);
            if (issueOpt.isPresent()) {
                Issue issue = issueOpt.get();
                String dateStr = payload.get("date"); // e.g. "2023-10-25"
                
                // Handle different date formats safely
                LocalDateTime date = null;
                if (dateStr != null && !dateStr.isEmpty()) {
                    // Append time if it's just a date string
                    if (dateStr.length() == 10) { 
                        dateStr += "T00:00:00"; 
                    }
                    date = LocalDateTime.parse(dateStr);
                }
                
                issue.setDueDate(date);
                Issue saved = issueRepository.save(issue);
                return ResponseEntity.ok(saved);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/labels")
    public ResponseEntity<?> addLabel(@PathVariable Long id, @RequestParam String label) {
        return issueRepository.findById(id).map(issue -> {
            if (!issue.getLabels().contains(label)) issue.getLabels().add(label);
            return ResponseEntity.ok(issueRepository.save(issue));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- FIX: Corrected Attachment Constructor Usage ---
    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> addAttachment(@PathVariable Long id, @RequestParam String url) {
        Optional<Issue> issueOpt = issueRepository.findById(id);
        if (issueOpt.isPresent()) {
            Issue issue = issueOpt.get();
            // FIX: Use 3-arg constructor, then set URL
            Attachment att = new Attachment("Link", "LINK", issue);
            att.setLinkUrl(url);
            attachmentRepository.save(att);
            return ResponseEntity.ok(issueRepository.save(issue));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/attachments/upload")
    public ResponseEntity<?> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return issueRepository.findById(id).map(issue -> {
            try {
                Attachment attachment = new Attachment(file.getOriginalFilename(), "FILE", issue);
                attachment.setData(file.getBytes());
                attachmentRepository.save(attachment);
                return ResponseEntity.ok(issueRepository.save(issue));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Upload failed");
            }
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping(value = "/attachments/{attachmentId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getAttachmentContent(@PathVariable Long attachmentId) {
        return attachmentRepository.findById(attachmentId).map(Attachment::getData).orElse(null);
    }

    @PostMapping("/{id}/attachments/link")
    public ResponseEntity<?> addLink(@PathVariable Long id, @RequestParam String url, @RequestParam String name) {
        return issueRepository.findById(id).map(issue -> {
            Attachment attachment = new Attachment(name, "LINK", issue);
            attachment.setLinkUrl(url);
            attachmentRepository.save(attachment);
            return ResponseEntity.ok(issueRepository.save(issue));
        }).orElse(ResponseEntity.notFound().build());
    }

@DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        if (attachmentRepository.existsById(attachmentId)) {
            attachmentRepository.deleteById(attachmentId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/checklists")
    public ResponseEntity<?> addChecklist(@PathVariable Long id, @RequestParam String name) {
        return issueRepository.findById(id).map(issue -> {
            checklistRepository.save(new Checklist(name, issue));
            return ResponseEntity.ok(issueRepository.save(issue));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/checklists/{checklistId}")
    public ResponseEntity<?> deleteChecklist(@PathVariable Long checklistId) {
        checklistRepository.deleteById(checklistId);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/checklists/{checklistId}/items")
    public ResponseEntity<?> addChecklistItem(@PathVariable Long checklistId, @RequestParam String text) {
        return checklistRepository.findById(checklistId).map(list -> {
            checklistItemRepository.save(new ChecklistItem(text, list));
            return ResponseEntity.ok(list);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/checklists/items/{itemId}")
    public ResponseEntity<?> deleteChecklistItem(@PathVariable Long itemId) {
        checklistItemRepository.deleteById(itemId);
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping("/checklists/items/{itemId}")
    public ResponseEntity<?> toggleItem(@PathVariable Long itemId, @RequestParam boolean checked) {
        return checklistItemRepository.findById(itemId).map(item -> {
            item.setIsChecked(checked);
            checklistItemRepository.save(item);
            return ResponseEntity.ok("Updated");
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/assignees")
    public ResponseEntity<?> addAssignee(@PathVariable Long id, 
                                         @RequestParam String email, 
                                         @RequestParam(required = false) String requesterEmail) {
        Optional<Issue> issueOpt = issueRepository.findById(id);
        Optional<User> targetUserOpt = userRepository.findByEmail(email);
        
        if (issueOpt.isPresent() && targetUserOpt.isPresent()) {
            Issue issue = issueOpt.get();
            User targetUser = targetUserOpt.get();
            
            if (!issue.getAssignees().contains(targetUser)) {
                issue.addAssignee(targetUser);
                issue.addWatcher(targetUser);
                issueRepository.save(issue);
                
                User sender = (requesterEmail != null) 
                    ? userRepository.findByEmail(requesterEmail).orElse(issue.getProject().getOwner())
                    : issue.getProject().getOwner();
                
                issue.addWatcher(sender);
                
                // --- LOG ASSIGNMENT ---
                activityLogService.log(sender, issue, "assigned " + targetUser.getUsername());

                notificationService.createNotification(
                    targetUser, sender, "ASSIGN", sender.getUsername() + " assigned you", issue.getId()
                );
            }
            return ResponseEntity.ok(issueRepository.save(issue));
        }
        return ResponseEntity.badRequest().body("Issue or User not found");
    }
    
    @DeleteMapping("/{id}/assignees")
    public ResponseEntity<?> removeAssignee(@PathVariable Long id, 
                                            @RequestParam String email, 
                                            @RequestParam(required = false) String requesterEmail) {
        Optional<Issue> issueOpt = issueRepository.findById(id);
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (issueOpt.isPresent() && userOpt.isPresent()) {
            Issue issue = issueOpt.get();
            issue.removeAssignee(userOpt.get());
            issueRepository.save(issue);
            
            // --- LOG REMOVAL ---
            User sender = (requesterEmail != null) 
                    ? userRepository.findByEmail(requesterEmail).orElse(issue.getProject().getOwner())
                    : issue.getProject().getOwner();
            
            activityLogService.log(sender, issue, "removed " + userOpt.get().getUsername());

            return ResponseEntity.ok(issueRepository.save(issue));
        }
        return ResponseEntity.badRequest().build();
    }

}

