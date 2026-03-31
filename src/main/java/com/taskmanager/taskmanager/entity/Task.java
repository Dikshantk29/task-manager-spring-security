package com.taskmanager.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status; //PENDING, IN_PROGRESS, COMPLETED

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    /*many tasks belong to one user
     // creates user_id column in tasks table
        // which user owns this task*/


}
