package org.kirya343.main.model.chat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InterlocutorInfoDTO {
    private String interlocutorName;
    private String interlocutorAvatar;

    public InterlocutorInfoDTO() {}

    public InterlocutorInfoDTO(String interlocutorName, String interlocutorAvatar) {
        this.interlocutorName = interlocutorName;
        this.interlocutorAvatar = interlocutorAvatar;
    }

}
