package org.kirya343.main.services;

import org.kirya343.main.model.Task;

public interface TaksService {

    Task findTask(String param, String paramType);

    void createTask();
    void deleteTask();
    void updateTask();
}
