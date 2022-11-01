package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingFullDto {
    private Long id; // уникальный идентификатор вещи
    private LocalDateTime start; // дата и время начала бронирования
    private LocalDateTime end; // дата и время конца бронирования
    private BookingStatus status; // статус бронирования
    private User booker; // пользователь, который осуществляет бронирование
    private Item item; // вещь, которую пользователь бронирует
}
