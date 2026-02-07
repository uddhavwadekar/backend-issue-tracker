package com.jira.clone.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "checklist_items")
public class ChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private boolean isChecked;

    @ManyToOne
    @JoinColumn(name = "checklist_id")
    @JsonIgnore
    private Checklist checklist;

    public ChecklistItem() {}

    public ChecklistItem(String text, Checklist checklist) {
        this.text = text;
        this.checklist = checklist;
        this.isChecked = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean getIsChecked() { return isChecked; }
    public void setIsChecked(boolean checked) { isChecked = checked; }
    public Checklist getChecklist() { return checklist; }
    public void setChecklist(Checklist checklist) { this.checklist = checklist; }
}