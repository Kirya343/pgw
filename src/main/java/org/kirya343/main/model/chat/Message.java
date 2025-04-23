package org.kirya343.main.model.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.kirya343.main.model.User;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // Или убедитесь, что всегда работаете в транзакции
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "is_read")
    private boolean read = false;

    @Transient
    private boolean isOwn;

    public boolean isOwn(User user) {
        return sender != null && sender.equals(user);
    }

    public void setIsOwn(boolean isOwn) {
        this.isOwn = isOwn;
    }

}
