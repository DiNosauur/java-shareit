package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemService {
    Collection<ItemBookingDto> findUserItems(long userId);

    Item saveItem(ItemDto itemDto, long userId);

    Optional<Item> updateItem(long itemId, ItemDto itemDto, long userId);

    boolean deleteItem(long id, long userId);

    Optional<ItemBookingDto> getItem(long id, long userId);

    Collection<Item> searchItems(String text);
}
