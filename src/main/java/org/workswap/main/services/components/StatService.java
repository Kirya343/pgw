package org.workswap.main.services.components;

import java.util.Locale;
import java.util.Map;

import org.workswap.datasource.main.model.User;

public interface StatService {
    int getMonthlyViews(User user);
    int getMonthlyResponses(User user);
    int getMonthlyDeals(User user);
    int getTotalViews(User user);
    int getTotalResponses(User user); 
    int getCompletedDeals(User user); 
    double getAverageRating(User user); 
    Map<String, Object> getSiteStats(Locale locale);
    Map<String, Object> getUserStats(User user, Locale locale);
}
