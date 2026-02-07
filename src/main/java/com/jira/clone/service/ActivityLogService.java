package com.jira.clone.service;

import com.jira.clone.model.ActivityLog;
import com.jira.clone.model.Issue;
import com.jira.clone.model.User;
import com.jira.clone.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    @Autowired private ActivityLogRepository activityLogRepository;

    public void log(User actor, Issue issue, String description) {
        try {
            ActivityLog log = new ActivityLog(description, actor, issue);
            activityLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to save activity log: " + e.getMessage());
        }
    }
}