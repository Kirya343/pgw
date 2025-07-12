package org.workswap.main.services.impl;

import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.Task;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.repository.TaskRepository;
import org.workswap.main.services.TaksService;

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
