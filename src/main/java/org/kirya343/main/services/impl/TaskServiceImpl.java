package org.kirya343.main.services.impl;

import org.kirya343.main.model.ModelsSettings.SearchParamType;
import org.kirya343.main.model.Task;
import org.kirya343.main.repository.TaskRepository;
import org.kirya343.main.services.TaksService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaksService {

    private final TaskRepository taskRepository;
    
    @Override 
    public Task findTask(String param, String paramType) {
        SearchParamType searchParamType = SearchParamType.valueOf(paramType);
        switch (searchParamType) {
            case ID:
                return taskRepository.findById(Long.parseLong(param)).orElse(null);
            case NAME:
                return taskRepository.findByName(param);
            default:
                throw new IllegalArgumentException("Unknown param type: " + paramType);
        }
    }

    @Override
    public void createTask() {

    }

    @Override
    public void deleteTask() {

    }

    @Override
    public void updateTask() {

    }
}
