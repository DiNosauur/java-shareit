package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.Booking;

import java.util.Collection;

@Data
public class ItemFullDto {
    private long id; // уникальный идентификатор вещи;
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    private Long requestId; // если вещь была создана по запросу другого пользователя;
    private Booking lastBooking; // последнее бронирование
    private Booking nextBooking; // ближайшее следующее бронирование
    private Collection<CommentDto> comments; // отзывы о вещи
}
