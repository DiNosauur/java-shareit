package ru.practicum.shareit.item.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "items", schema = "public")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный идентификатор вещи;
    @Column
    private String name; // краткое название;
    @Column
    private String description; // развёрнутое описание;
    @Column(name = "is_available")
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    @Column(name = "owner_id")
    private Long owner; // владелец вещи;
    @Column(name = "request_id")
    private Long requestId; // если вещь была создана по запросу другого пользователя;
}
