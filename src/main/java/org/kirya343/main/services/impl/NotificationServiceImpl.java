package org.kirya343.main.services.impl;

import org.kirya343.main.model.DTOs.NotificationDTO;
import org.kirya343.main.model.chat.PersistentNotification;
import org.kirya343.main.repository.NotificationRepository;
import org.kirya343.main.services.NotificationService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.someClasses.WebhookSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Override
    public void saveOfflineNotification(String userId, NotificationDTO notification) {

        long count = notificationRepo.countByUserId(userId);
        if (count >= 100) { // Лимит на пользователя
            notificationRepo.deleteOldestByUserId(userId);
        }

        String email = userService.findBySub(userId).getEmail();

        PersistentNotification entity = new PersistentNotification();
        entity.setUserId(userId);
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setUrl(notification.getUrl());
        entity.setConversationId(notification.getConversationId());
        notificationRepo.save(entity);

        // Отправляем на внешний сервер
        try {
            // Строим JSON вручную или через ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("messageId", notification.getConversationId() != null ? notification.getConversationId() : UUID.randomUUID().toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", email);
            requestBody.put("message", notification.getMessage());
            requestBody.put("type", "info");
            requestBody.put("metadata", metadata);

            String json = objectMapper.writeValueAsString(requestBody);
            String signature = WebhookSigner.generateSignature(json); // Подпись

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://s1.qwer-host.xyz:25079/api/notifications/send"))
                .header("Content-Type", "application/json")
                .header("X-Webhook-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("Notification sent. Status: " + response.statusCode());
                    System.out.println("Response: " + response.body());
                });

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send notification to external server: " + e.getMessage());
        }
    }

    @Override
    public List<NotificationDTO> loadPendingNotifications(String userId) {
        return notificationRepo.findByUserId(userId).stream()
                .map(n -> new NotificationDTO(n.getTitle(), n.getMessage(), n.getUrl(), n.getConversationId()))
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedRate = 7200000) // Каждые 2 часа
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        notificationRepo.deleteByCreatedBefore(cutoff);
        log.info("Cleaned up notifications older than {}", cutoff);
    }

    @Override
    @Transactional
    public void clearNotifications(String userId) {
        notificationRepo.deleteByUserId(userId);
    }
}
