package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id; // уникальный идентификатор комментария;
    private String text; // содержимое комментария;
    private String authorName; // автор комментария;
    private LocalDateTime created; // дата создания комментария;
}
