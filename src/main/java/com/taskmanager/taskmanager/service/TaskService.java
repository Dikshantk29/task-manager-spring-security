package com.taskmanager.taskmanager.service;
import com.taskmanager.taskmanager.dto.TaskRequest;
import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // ─── helper: get User entity from username ────────────────────────
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    // ─── CREATE TASK ──────────────────────────────────────────────────
    public Task createTask(TaskRequest request, String username) {
        User user = getUser(username);
        //                  ↑
        // username comes from SecurityContext (logged-in user)
        // NOT from request body — user can't fake this

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setUser(user);               // link task to its owner

        return taskRepository.save(task);
    }

    // ─── GET MY TASKS (only logged-in user's tasks) ───────────────────
    public List<Task> getMyTasks(String username) {
        User user = getUser(username);
        return taskRepository.findByUser(user);
        //                    ↑
        // WHERE user_id = ? → only THIS user's tasks
        // user A can NEVER see user B's tasks
    }

    // ─── UPDATE TASK ──────────────────────────────────────────────────
    public Task updateTask(Long taskId, TaskRequest request, String username) {
        User user = getUser(username);

        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException(
                        "Task not found or does not belong to you"));
        //                  ↑
        // WHERE id = ? AND user_id = ?
        // Even if user knows another task's ID → they can't update it
        // because it won't match their user_id → throws exception

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        return taskRepository.save(task);
    }

    // ─── DELETE TASK ──────────────────────────────────────────────────
    public String deleteTask(Long taskId, String username) {
        User user = getUser(username);

        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException(
                        "Task not found or does not belong to you"));
        //
        // same protection as update —
        // can only delete YOUR OWN tasks
        //

        taskRepository.delete(task);
        return "Task deleted successfully!";
    }

    // ─── GET ALL TASKS (admin only) ───────────────────────────────────
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
        // no filter — returns every task from every user
        // only ADMIN should call this (enforced in controller)
    }
}