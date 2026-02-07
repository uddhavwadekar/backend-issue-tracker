package com.jira.clone.repository;

import com.jira.clone.model.ColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, Long> {
    // Logic: Fetch columns for a specific project, ordered by their board position
    List<ColumnEntity> findByProjectIdOrderByPositionAsc(Long projectId);
}