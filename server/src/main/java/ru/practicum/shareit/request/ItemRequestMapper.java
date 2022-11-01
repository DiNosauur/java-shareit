package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(userId);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    public static ItemRequestFullDto toItemRequestFullDto(ItemRequest itemRequest,
                                                          Collection<Item> items) {
        ItemRequestFullDto itemRequestFullDto = new ItemRequestFullDto();
        if (itemRequest != null) {
            itemRequestFullDto.setId(itemRequest.getId());
            itemRequestFullDto.setDescription(itemRequest.getDescription());
            itemRequestFullDto.setCreated(itemRequest.getCreated());
            if (items != null && items.size() > 0) {
                itemRequestFullDto.setItems(items);
            } else {
                itemRequestFullDto.setItems(new ArrayList<>());
            }
        }
        return itemRequestFullDto;
    }
}
