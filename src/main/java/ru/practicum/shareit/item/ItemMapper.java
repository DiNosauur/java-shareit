package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public static Item toItem(ItemDto itemDto, Long ownerId, Long requestId) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(ownerId);
        item.setItemRequest(requestId);
        return item;
    }

    public static Item toItem(ItemDto itemDto, Item itemOld) {
        Item item = itemOld;
        item.setName(itemDto.getName().isEmpty() ? itemOld.getName() : itemDto.getName());
        item.setDescription(itemDto.getDescription().isEmpty() ? itemOld.getDescription() : itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable() == null ? itemOld.getAvailable() : itemDto.getAvailable());
        return item;
    }
}
