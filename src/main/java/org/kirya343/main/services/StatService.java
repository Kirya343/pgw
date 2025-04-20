package org.kirya343.main.services;

import org.kirya343.main.model.User;

public interface StatService {
    int getMonthlyViews(User user);
    int getMonthlyResponses(User user);
    int getMonthlyDeals(User user);
    int getTotalViews(User user);
    int getTotalResponses(User user); // Можно под фейковые данные
    int getCompletedDeals(User user); // Тоже можно имитировать
    double getAverageRating(User user);
}
