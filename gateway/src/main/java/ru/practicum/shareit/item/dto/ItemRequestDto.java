package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private long id; // уникальный идентификатор вещи;
    @NotBlank(message = "Name is required")
    private String name; // краткое название;
    @NotBlank(message = "Description is required")
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    private Long requestId; // если вещь была создана по запросу другого пользователя;
}
