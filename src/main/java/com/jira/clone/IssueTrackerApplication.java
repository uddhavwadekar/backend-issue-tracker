package com.jira.clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the main entry point for your Spring Boot application.
 * Because it is in 'com.jira.clone', it will automatically scan:
 * - com.jira.clone.controller
 * - com.jira.clone.service
 * - com.jira.clone.model
 * - com.jira.clone.repository
 */
@SpringBootApplication
public class IssueTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssueTrackerApplication.class, args);
    }
}