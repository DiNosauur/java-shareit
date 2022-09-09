package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class ItemDto {
    private long id; // уникальный идентификатор вещи;
    @NotBlank(message = "Name is required")
    private String name; // краткое название;
    @NotBlank(message = "Description is required")
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
}
