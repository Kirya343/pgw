package org.kirya343.main.model.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.transaction.Transactional;
import org.kirya343.main.model.User;
import org.kirya343.main.services.components.AvatarService;

@Transactional
public class UserDTO {
    @JsonProperty("interlocutorName")
    private String interlocutorName;

    @JsonProperty("interlocutorAvatar")
    private String interlocutorAvatar;

    public UserDTO(User user, AvatarService avatarService) {
        this.interlocutorName = user.getName() != null ? user.getName() : "Собеседник";
        this.interlocutorAvatar = avatarService.resolveAvatarPath(user);
    }

    // Геттеры и сеттеры
}
