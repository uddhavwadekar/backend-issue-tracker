package com.jira.clone.controller;

import com.jira.clone.model.ColumnEntity;
import com.jira.clone.model.Project;
import com.jira.clone.repository.ColumnRepository;
import com.jira.clone.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ColumnController {

    @Autowired private ColumnRepository columnRepository;
    @Autowired private ProjectRepository projectRepository;

    // --- 1. GET COLUMNS (Required for Board to load) ---
    @GetMapping("/{projectId}/columns")
    public ResponseEntity<List<ColumnEntity>> getColumns(@PathVariable Long projectId) {
        return ResponseEntity.ok(columnRepository.findByProjectIdOrderByPositionAsc(projectId));
    }

    // --- 2. CREATE DYNAMIC COLUMN (Required for "Add List" button) ---
    @PostMapping("/{projectId}/columns")
    public ResponseEntity<?> createColumn(@PathVariable Long projectId, @RequestBody Map<String, String> payload) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isPresent()) {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Column name is required");
            }

            // Logic: Find current number of columns and add 1 to position so it appears at the end
            List<ColumnEntity> existingColumns = columnRepository.findByProjectIdOrderByPositionAsc(projectId);
            int newPosition = existingColumns.size() + 1;

            ColumnEntity newCol = new ColumnEntity();
            newCol.setName(name);
            newCol.setPosition(newPosition);
            newCol.setProject(projectOpt.get());

            ColumnEntity savedCol = columnRepository.save(newCol);
            return ResponseEntity.ok(savedCol);
        }
        return ResponseEntity.notFound().build();
    }
}