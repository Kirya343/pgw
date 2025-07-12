package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    private User author;

    @ManyToOne
    private Task task;
}
