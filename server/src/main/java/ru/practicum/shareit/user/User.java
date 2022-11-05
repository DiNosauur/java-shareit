package ru.practicum.shareit.user;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "users", schema = "public")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный идентификатор пользователя;
    private String email; // адрес электронной почты;
    private String name; // имя или логин пользователя;
}
