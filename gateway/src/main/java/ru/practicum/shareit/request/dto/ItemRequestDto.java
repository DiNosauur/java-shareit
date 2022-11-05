package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class ItemRequestDto {
    private long id; // уникальный идентификатор запроса;
    @NotBlank(message = "Description is required")
    private String description; // текст запроса, содержащий описание требуемой вещи;
}