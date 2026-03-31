package com.taskmanager.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Task> tasks;
    /*
     * One user owns many tasks.
     * mappedBy = "user" → tells JPA the
     * relationship is managed by the user
     * field inside Task.java.
     * CascadeType.ALL → if you delete a user,
     * all their tasks get deleted too
     * automatically.
     * */


}
