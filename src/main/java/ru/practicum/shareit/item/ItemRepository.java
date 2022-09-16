package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByUserId(long userId);

    Collection<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String text);
}
