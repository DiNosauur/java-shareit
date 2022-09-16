package ru.practicum.shareit.user;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users", schema = "public")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный идентификатор пользователя;
    @NotBlank(message = "Email is required")
    @Email(regexp = "\\w+@\\w+\\.(ru|com)",
            message = "Email should be valid")
    private String email; // адрес электронной почты;
    private String name; // имя или логин пользователя;
}
