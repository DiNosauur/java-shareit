package ru.practicum.shareit.request;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests", schema = "public")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор запроса;
    @Column
    private String description; // текст запроса, содержащий описание требуемой вещи;
    @Column(name = "requestor_id")
    private Long requestor; // пользователь, создавший запрос;
    @Column
    private LocalDateTime created; // дата и время создания запроса;
}
