package org.workswap.main.services;

import org.workswap.datasource.main.model.Task;

public interface TaksService {

    Task findTask(String param, String paramType);

    void createTask();
    void deleteTask();
    void updateTask();
}
