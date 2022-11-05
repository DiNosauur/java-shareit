package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class ItemRequestFullDto {
    private Long id; // уникальный идентификатор запроса;
    private String description; // текст запроса, содержащий описание требуемой вещи;
    private LocalDateTime created; // дата и время создания запроса;
    private Collection<Item> items; // вещи, которые добавлены по запросу
}
