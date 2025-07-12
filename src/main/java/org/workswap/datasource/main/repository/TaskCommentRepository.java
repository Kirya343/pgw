package org.workswap.datasource.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workswap.datasource.main.model.TaskComment;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findAllByTaskId(Long id);
}
