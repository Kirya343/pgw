package org.kirya343.main.services;

import java.util.List;

import org.kirya343.main.model.Resume;
import org.springframework.data.domain.Pageable;

public interface ResumeService {
    // Определяем методы для работы с резюме
    List<Resume> findPublishedResumes(Pageable pageable);
    Resume getResumeByIdWithUser(Long id);
}