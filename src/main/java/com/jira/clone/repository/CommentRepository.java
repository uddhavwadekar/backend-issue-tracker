package com.jira.clone.repository;

import com.jira.clone.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Fetch comments for a specific issue, newest first
    List<Comment> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}
