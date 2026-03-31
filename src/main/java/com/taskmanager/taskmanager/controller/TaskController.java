package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskRequest;
import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ─── POST /tasks ──────────────────────────────────────────────────
    @PostMapping
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Task> createTask(
            @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        //   ↑
        // Spring pulls the logged-in user OUT of SecurityContext
        // No need to pass userId in request body
        // Lesson 7 in action ✅

        Task task = taskService.createTask(request, userDetails.getUsername());
        return ResponseEntity.ok(task);
    }

    // ─── GET /tasks ───────────────────────────────────────────────────
    @GetMapping
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Task>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Task> tasks = taskService.getMyTasks(userDetails.getUsername());
        return ResponseEntity.ok(tasks);
    }

    // ─── PUT /tasks/{id} ──────────────────────────────────────────────
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Task updated = taskService.updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // ─── DELETE /tasks/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String message = taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok(message);
    }

    // ─── GET /tasks/all (ADMIN ONLY) ──────────────────────────────────
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    //             ↑
    // Double protection:
    // SecurityConfig already blocks non-admins at URL level
    // @PreAuthorize adds method-level check on top
    // Lesson 6 in action ✅
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
}