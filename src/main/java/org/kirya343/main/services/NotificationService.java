package org.kirya343.main.services;

import org.kirya343.main.model.Notification;
import org.kirya343.main.model.DTOs.FullNotificationDTO;
import org.kirya343.main.model.DTOs.NotificationDTO;

public interface NotificationService {

    void saveOfflineChatNotification(String userId, NotificationDTO notification);
    void markAsRead(Notification notification);

    FullNotificationDTO toDTO(Notification notification);
}
