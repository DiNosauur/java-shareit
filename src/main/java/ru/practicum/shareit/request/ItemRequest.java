package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id; // уникальный идентификатор запроса;
    private String description; // текст запроса, содержащий описание требуемой вещи;
    private User requestor; // пользователь, создавший запрос;
    private LocalDateTime created; // дата и время создания запроса.

}
