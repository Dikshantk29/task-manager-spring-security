package com.taskmanager.taskmanager.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor           // lombok → generates constructor with all fields
public class LoginResponse {
    private String token;     // the JWT token sent back after login
    private String username;
    private String role;
}