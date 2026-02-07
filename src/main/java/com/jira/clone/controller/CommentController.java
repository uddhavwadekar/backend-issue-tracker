package com.jira.clone.controller;

import com.jira.clone.dto.CommentRequest;
import com.jira.clone.model.*;
import com.jira.clone.repository.*;
import com.jira.clone.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private IssueRepository issueRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request) {
        Optional<Issue> issueOpt = issueRepository.findById(request.getIssueId());
        Optional<User> userOpt = userRepository.findByEmail(request.getUserEmail());

        if (issueOpt.isPresent() && userOpt.isPresent()) {
            Issue issue = issueOpt.get();
            User author = userOpt.get();

            Comment comment = new Comment();
            comment.setText(request.getText());
            comment.setAuthor(author);
            comment.setIssue(issue);
            
            Comment saved = commentRepository.save(comment);
            
            String msg = author.getUsername() + " commented: " + (request.getText().length() > 20 ? request.getText().substring(0, 20) + "..." : request.getText());

            // 1. Notify Assignees
            notificationService.notifyAssignees(issue, author, "COMMENT", msg);

            // 2. Notify Reporter (NEW LOGIC)
            // If the reporter is NOT the one commenting, and NOT already an assignee (to avoid double notification)
            if (issue.getReporter() != null 
                && !issue.getReporter().getId().equals(author.getId()) 
                && !issue.getAssignees().contains(issue.getReporter())) {
                
                notificationService.createNotification(
                    issue.getReporter(), 
                    author, 
                    "COMMENT", 
                    msg, 
                    issue.getId()
                );
            }

            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.status(404).body("Issue or User not found.");
    }

    @GetMapping("/issue/{issueId}")
    public List<Comment> getCommentsByIssue(@PathVariable Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtDesc(issueId);
    }
}