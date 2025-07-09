package org.kirya343.main.controller.components;

import java.util.List;
import java.util.stream.Collectors;

import org.kirya343.main.model.ModelsSettings.SearchParamType;
import org.kirya343.main.model.User;
import org.kirya343.main.model.DTOs.FullNotificationDTO;
import org.kirya343.main.repository.NotificationRepository;
import org.kirya343.main.services.NotificationService;
import org.kirya343.main.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

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
