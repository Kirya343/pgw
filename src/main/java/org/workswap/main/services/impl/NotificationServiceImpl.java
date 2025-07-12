package org.workswap.main.services.impl;

import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.Notification;
import org.workswap.datasource.main.model.DTOs.FullNotificationDTO;
import org.workswap.datasource.main.model.DTOs.NotificationDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.Notification.Importance;
import org.workswap.datasource.main.model.Notification.NotificationType;
import org.workswap.datasource.main.repository.NotificationRepository;
import org.workswap.main.services.NotificationService;
import org.workswap.main.services.UserService;
import org.workswap.main.someClasses.WebhookSigner;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Override
    public void saveOfflineChatNotification(String sub, NotificationDTO dto) {

        String email = userService.findUser(sub, SearchParamType.ID).getEmail();

        Notification notification = new Notification();
        notification.setRecipient(userService.findUser(sub, SearchParamType.ID));
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getMessage());
        notification.setLink(dto.getLink());
        notification.setType(NotificationType.CHAT);
        notification.setImportance(Importance.INFO);
        notificationRepository.save(notification);

        // Отправляем в телеграмм
        try {
            // Строим JSON вручную или через ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("messageId", UUID.randomUUID().toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", email);
            requestBody.put("message", dto.getMessage());
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
            System.err.println("Failed to send notification to Telegramm: " + e.getMessage());
        }
    }

    @Override
    public void markAsRead(Notification notification) {
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public FullNotificationDTO toDTO(Notification notification) {
        
        return new FullNotificationDTO(notification.getId(), 
                                       notification.getRecipient().getId(), 
                                       notification.isRead(), 
                                       notification.getTitle(),
                                       notification.getContent(), 
                                       notification.getLink(), 
                                       notification.getType().toString(), 
                                       notification.getImportance().toString(), 
                                       notification.getCreatedAt());
    }
}
