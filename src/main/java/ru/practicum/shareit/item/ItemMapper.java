package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
    }

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(ownerId);
        item.setRequestId(itemDto.getRequestId());
        return item;
    }

    public static Item toItem(ItemDto itemDto, Item itemOld) {
        Item item = itemOld;
        item.setName(itemDto.getName() == null ? itemOld.getName() : itemDto.getName());
        item.setDescription(itemDto.getDescription() == null ? itemOld.getDescription() : itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable() == null ? itemOld.getAvailable() : itemDto.getAvailable());
        return item;
    }

    public static ItemFullDto toItemFullDto(Item item,
                                            Optional<Booking> lastBooking,
                                            Optional<Booking> nextBooking,
                                            Collection<CommentDto> comments) {
        ItemFullDto itemFullDto = new ItemFullDto();
        if (item != null) {
            itemFullDto.setId(item.getId());
            itemFullDto.setName(item.getName());
            itemFullDto.setDescription(item.getDescription());
            itemFullDto.setAvailable(item.getAvailable());
            itemFullDto.setRequestId(item.getRequestId());
            if (lastBooking.isPresent()) {
                itemFullDto.setLastBooking(lastBooking.get());
            }
            if (nextBooking.isPresent()) {
                itemFullDto.setNextBooking(nextBooking.get());
            }
            if (comments != null && comments.size() > 0) {
                itemFullDto.setComments(comments);
            } else {
                itemFullDto.setComments(new ArrayList<>());
            }
        }
        return itemFullDto;
    }
}
