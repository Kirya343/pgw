package org.workswap.main.controller.components;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.FullNotificationDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.repository.NotificationRepository;
import org.workswap.main.services.NotificationService;
import org.workswap.main.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/for-user/{id}")
    public List<FullNotificationDTO> getNotification(@PathVariable Long id, @AuthenticationPrincipal OAuth2User oauth2User) {
        User user = userService.findUser(id.toString(), SearchParamType.ID);
        if (oauth2User != null) {
            User principal = userService.findUserFromOAuth2(oauth2User);

            if(user == principal) {
                return notificationRepository.findByRecipient(user).stream()
                    .map(notification -> notificationService.toDTO(notification))
                    .collect(Collectors.toList());
            } 
        }
        return null;
    }
}
