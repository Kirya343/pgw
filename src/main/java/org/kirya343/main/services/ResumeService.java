package org.kirya343.main.services;

import org.kirya343.main.model.Resume;
import org.kirya343.main.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResumeService {
    @Autowired
    private ResumeRepository resumeRepository;

    public List<Resume> findPublishedResumes(Pageable pageable) {
        return resumeRepository.findByPublishedTrue(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Resume getResumeByIdWithUser(Long id) {
        return resumeRepository.findByIdWithUser(id).orElse(null);
    }
}
