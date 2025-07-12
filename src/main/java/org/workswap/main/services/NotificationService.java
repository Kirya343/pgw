package org.workswap.main.services;

import org.workswap.datasource.main.model.Notification;
import org.workswap.datasource.main.model.DTOs.FullNotificationDTO;
import org.workswap.datasource.main.model.DTOs.NotificationDTO;

public interface NotificationService {

    void saveOfflineChatNotification(String userId, NotificationDTO notification);
    void markAsRead(Notification notification);

    FullNotificationDTO toDTO(Notification notification);
}
