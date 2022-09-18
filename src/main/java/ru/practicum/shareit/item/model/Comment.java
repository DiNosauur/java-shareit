package ru.practicum.shareit.item.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", schema = "public")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный идентификатор комментария;
    @Column
    private String text; // содержимое комментария;
    @Column(name = "item_id")
    private Long itemId; // вещь, к которой относится комментарий;
    @Column(name = "author_id")
    private Long authorId; // автор комментария;
    @Column
    private LocalDateTime created; // дата создания комментария;
}
