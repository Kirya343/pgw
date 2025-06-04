package org.kirya343.main.services.components;

import org.kirya343.main.model.User;

import java.util.Locale;
import java.util.Map;

public interface StatService {
    int getMonthlyViews(User user);
    int getMonthlyResponses(User user);
    int getMonthlyDeals(User user);
    int getTotalViews(User user);
    int getTotalResponses(User user); // Можно под фейковые данные
    int getCompletedDeals(User user); // Тоже можно имитировать
    double getAverageRating(User user); 
    Map<String, Object> getSiteStats(Locale locale);
    Map<String, Object> getUserStats(User user, Locale locale);
}
