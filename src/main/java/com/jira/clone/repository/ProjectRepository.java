package com.jira.clone.repository;

import com.jira.clone.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    boolean existsByProjectKey(String projectKey);

    List<Project> findByOwnerId(Long ownerId);

    // ARCHITECTURE FIX: 
    // Select Project IF:
    // 1. User is the Owner of the Project
    // 2. OR User is a direct member of the Project
    // 3. OR User is a member of the WORKSPACE that owns the Project (Trello Logic)
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members pm " +
           "LEFT JOIN p.workspace w " +
           "LEFT JOIN w.members wm " +
           "WHERE p.owner.id = :userId " +
           "OR pm.id = :userId " +
           "OR wm.id = :userId")
    List<Project> findProjectsForUser(@Param("userId") Long userId);
}