package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByOwnerOrderById(long userId);

    @Query(" select i from Item i " +
            " where i.available = true" +
            "   and (upper(i.name) like upper(concat('%', ?1, '%')) or " +
            "        upper(i.description) like upper(concat('%', ?1, '%')))")
    Collection<Item> search(String text);

    Collection<Item> findByRequestId(long requestId);
}
