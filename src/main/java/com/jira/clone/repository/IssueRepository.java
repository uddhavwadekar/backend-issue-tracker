package com.jira.clone.repository;

import com.jira.clone.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectId(Long projectId);
    
    // FETCH ALL ISSUES IN WORKSPACE (Access via Issue -> Project -> Workspace)
    @Query("SELECT i FROM Issue i WHERE i.project.workspace.id = :workspaceId")
    List<Issue> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}