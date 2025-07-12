package org.kirya343.main.repository;

import java.util.List;

import org.kirya343.main.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findAllByTaskId(Long id);
}
