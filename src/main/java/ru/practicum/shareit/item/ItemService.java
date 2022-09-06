package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemService {
    Collection<Item> findUserItems(long userId);

    Item saveItem(Item item);

    Optional<Item> updateItem(Item item);

    boolean deleteItem(long id, long userId);

    Optional<Item> getItem(long id);

    Collection<Item> searchItems(String text);

    boolean checkItem(Item item);
}
