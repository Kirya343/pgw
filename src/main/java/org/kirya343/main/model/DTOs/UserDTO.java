package org.kirya343.main.model.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.transaction.Transactional;
import org.kirya343.main.model.User;

@Transactional
public class UserDTO {


    @JsonProperty("interlocutorName")
    private String interlocutorName;

    @JsonProperty("interlocutorAvatar")
    private String interlocutorAvatar;


    public UserDTO(User user, String avatarUrl) {
        this.interlocutorName = user.getName() != null ? user.getName() : "Собеседник";
        this.interlocutorAvatar = avatarUrl;
    }
}
