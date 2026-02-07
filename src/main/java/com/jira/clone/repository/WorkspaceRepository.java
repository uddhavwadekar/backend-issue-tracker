package com.jira.clone.repository;

import com.jira.clone.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    // Finds workspaces I own OR workspaces I have been invited to
    @Query("SELECT w FROM Workspace w LEFT JOIN w.members m WHERE w.owner.id = :userId OR m.id = :userId")
    List<Workspace> findAllForUser(@Param("userId") Long userId);
}