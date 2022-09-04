package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
public class Item {
    private Long id; // уникальный идентификатор вещи;
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private boolean available; // статус о том, доступна или нет вещь для аренды;
    private User owner; // владелец вещи;
    private ItemRequest request; // если вещь была создана по запросу другого пользователя;
}
