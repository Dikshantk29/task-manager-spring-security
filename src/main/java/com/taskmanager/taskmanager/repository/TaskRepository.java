package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanager.taskmanager.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);
}
