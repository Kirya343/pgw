package org.kirya343.main.model.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;

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

    @Transient
    private long unreadCount;

    @Transient
    private String lastMessagePreview;

    @Transient
    private String lastMessageTime;

    @Transient
    private transient User interlocutor; // Временное поле для хранения собеседника

    public User getInterlocutor(User user) {
        if (user == null || user1 == null || user2 == null) {
            return null;
        }
        if (user1.equals(user)) {
            interlocutor = user2;
        } else if (user2.equals(user)) {
            interlocutor = user1;
        }
        return interlocutor;
    }

    // Получить последнее сообщение
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
}

