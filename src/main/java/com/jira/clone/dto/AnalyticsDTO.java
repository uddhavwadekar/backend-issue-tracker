package com.jira.clone.dto;

import java.util.Map;

public class AnalyticsDTO {
    private int totalIssues;
    private int completedIssues;
    private int highPriorityIssues;
    private Map<String, Integer> statusCounts;
    private Map<String, Integer> priorityCounts;
    private Map<String, Integer> memberWorkload;

    public AnalyticsDTO(int total, int completed, int high, Map<String, Integer> status, Map<String, Integer> priority, Map<String, Integer> workload) {
        this.totalIssues = total;
        this.completedIssues = completed;
        this.highPriorityIssues = high;
        this.statusCounts = status;
        this.priorityCounts = priority;
        this.memberWorkload = workload;
    }

    // Getters
    public int getTotalIssues() { return totalIssues; }
    public int getCompletedIssues() { return completedIssues; }
    public int getHighPriorityIssues() { return highPriorityIssues; }
    public Map<String, Integer> getStatusCounts() { return statusCounts; }
    public Map<String, Integer> getPriorityCounts() { return priorityCounts; }
    public Map<String, Integer> getMemberWorkload() { return memberWorkload; }
}