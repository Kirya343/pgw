package org.kirya343.main.repository;

import java.util.List;

import org.kirya343.main.model.Task;
import org.kirya343.main.model.Task.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(Status status);
}
