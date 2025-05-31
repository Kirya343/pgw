package org.kirya343.main.services;

import java.util.List;

import org.kirya343.main.model.DTOs.NotificationDTO;

public interface NotificationService {

    public void saveOfflineNotification(String userId, NotificationDTO notification);
    public List<NotificationDTO> loadPendingNotifications(String userId);
    public void cleanupOldNotifications();
    public void clearNotifications(String userId);
}
