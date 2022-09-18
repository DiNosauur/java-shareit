package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.Booking;

@Data
public class ItemBookingDto {
    private long id; // уникальный идентификатор вещи;
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    private Booking lastBooking; // последнее бронирование
    private Booking nextBooking; // ближайшее следующее бронирование
}
