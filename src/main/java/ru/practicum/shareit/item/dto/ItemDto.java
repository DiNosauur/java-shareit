package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDto {
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private boolean available; // статус о том, доступна или нет вещь для аренды;
    private Long request; // если вещь была создана по запросу другого пользователя;
}
