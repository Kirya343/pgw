package org.kirya343.main.services;

import org.kirya343.main.model.DTOs.NotificationDTO;
import org.kirya343.main.model.chat.PersistentNotification;
import org.kirya343.main.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepo;

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void saveOfflineNotification(String userId, NotificationDTO notification) {

        long count = notificationRepo.countByUserId(userId);
        if (count >= 100) { // Лимит на пользователя
            notificationRepo.deleteOldestByUserId(userId);
        }

        PersistentNotification entity = new PersistentNotification();
        entity.setUserId(userId);
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setUrl(notification.getUrl());
        entity.setConversationId(notification.getConversationId());
        notificationRepo.save(entity);
    }

    public List<NotificationDTO> loadPendingNotifications(String userId) {
        return notificationRepo.findByUserId(userId).stream()
                .map(n -> new NotificationDTO(n.getTitle(), n.getMessage(), n.getUrl(), n.getConversationId()))
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 3600000) // Каждый час
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        notificationRepo.deleteByCreatedBefore(cutoff);
        log.info("Cleaned up notifications older than {}", cutoff);
    }

    @Transactional
    public void clearNotifications(String userId) {
        notificationRepo.deleteByUserId(userId);
    }
}
