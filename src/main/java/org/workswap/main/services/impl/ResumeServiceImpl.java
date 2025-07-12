package org.workswap.main.services.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.workswap.datasource.main.model.Resume;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.repository.ResumeRepository;
import org.workswap.main.services.ResumeService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    public List<Resume> findPublishedResumes(Pageable pageable) {
        return resumeRepository.findByPublishedTrue(pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Resume getResumeByIdWithUser(Long id) {
        return resumeRepository.findByIdWithUser(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Resume getResumeByUser(User user) {
        return resumeRepository.findByUser(user).orElse(null);
    }
}
