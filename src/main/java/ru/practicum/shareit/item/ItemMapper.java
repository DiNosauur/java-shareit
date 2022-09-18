package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;

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
        item.setName(itemDto.getName() == null ? itemOld.getName() : itemDto.getName());
        item.setDescription(itemDto.getDescription() == null ? itemOld.getDescription() : itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable() == null ? itemOld.getAvailable() : itemDto.getAvailable());
        return item;
    }

    public static ItemBookingDto toItemBookingDto(Item item, Optional<Booking> lastBooking, Optional<Booking> nextBooking) {
        ItemBookingDto itemBookingDto = new ItemBookingDto();
        if (item != null) {
            itemBookingDto.setId(item.getId());
            itemBookingDto.setName(item.getName());
            itemBookingDto.setDescription(item.getDescription());
            itemBookingDto.setAvailable(item.getAvailable());
            if (lastBooking.isPresent()) {
                itemBookingDto.setLastBooking(lastBooking.get());
            }
            if (nextBooking.isPresent()) {
                itemBookingDto.setNextBooking(nextBooking.get());
            }
        }
        return itemBookingDto;
    }
}
