package com.taskmanager.taskmanager.dto;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String status;      // "PENDING", "IN_PROGRESS", "DONE"
}