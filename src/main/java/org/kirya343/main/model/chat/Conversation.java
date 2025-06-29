package org.kirya343.main.model.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Message> messages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;


    public User getOtherParticipant(User user) {
        if (user == null || user1 == null || user2 == null) {
            return null;
        }
        if (user1.equals(user)) {
            return user2;
        } else if (user2.equals(user)) {
            return user1;
        }
        return null;
    }

    @Transient
    private long unreadCount;

    @Transient
    private String lastMessagePreview;

    @Transient
    private String lastMessageTime;

    @Transient
    private transient User interlocutor; // Временное поле для хранения собеседника

    // Метод для получения актуальных данных собеседника
    public User getInterlocutor(User currentUser, UserService userService) {
        if (interlocutor == null) {
            interlocutor = userService.findById(
                    user1.equals(currentUser) ? user2.getId() : user1.getId()
            );
        }
        return interlocutor;
    }

    // Метод для получения имени с актуальными данными
    public String getInterlocutorName(User currentUser, UserService userService) {
        return getInterlocutor(currentUser, userService).getName();
    }

    // Метод для получения аватарки через сервис
    public String getInterlocutorAvatar(User currentUser, UserService userService, AvatarService avatarService) {
        return avatarService.resolveAvatarPath(getInterlocutor(currentUser, userService));
    }


    // Получить собеседника
    public User getInterlocutor(User currentUser) {
        return user1.equals(currentUser) ? user2 : user1;
    }

    // Получить последнее сообщение
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
}

