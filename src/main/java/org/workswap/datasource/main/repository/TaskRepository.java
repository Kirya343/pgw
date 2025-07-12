package org.workswap.datasource.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.main.model.Task;
import org.workswap.datasource.main.model.Task.Status;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(Status status);
    Task findByName(String name);
}
