package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;
import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Collection<Item> findUserItems(long userId);
    Item save(Item item);
    Optional<Item> update(Item item);
    boolean delete(long id, long userId);
    Optional<Item> get(long id);
    Collection<Item> search(String text);
    boolean checkItem(Item item);
}
