package org.workswap.main.services;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.workswap.datasource.main.model.Resume;
import org.workswap.datasource.main.model.User;

public interface ResumeService {
    // Определяем методы для работы с резюме
    List<Resume> findPublishedResumes(Pageable pageable);
    Resume getResumeByIdWithUser(Long id);
    Resume getResumeByUser(User user);
}