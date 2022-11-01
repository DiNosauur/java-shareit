package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    private Long id; // уникальный идентификатор комментария;
    @NotBlank(message = "Text is required")
    private String text; // содержимое комментария;
    private String authorName; // автор комментария;
    private LocalDateTime created; // дата создания комментария;
}
