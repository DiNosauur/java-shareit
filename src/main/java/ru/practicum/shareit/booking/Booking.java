package ru.practicum.shareit.booking;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", schema = "public")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный идентификатор вещи;
    @Column(name = "start_date")
    private LocalDateTime start; // дата и время начала бронирования
    @Column(name = "end_date")
    private LocalDateTime end; // дата и время конца бронирования
    @Column(name = "item_id")
    private Long itemId; // вещь, которую пользователь бронирует
    @Column(name = "booker_id")
    private Long bookerId; // пользователь, который осуществляет бронирование
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // статус бронирования
}