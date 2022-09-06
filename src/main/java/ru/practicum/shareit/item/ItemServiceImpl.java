package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    @Override
    public Collection<Item> findUserItems(long userId) {
        return repository.findUserItems(userId);
    }

    @Override
    public Item saveItem(Item item) {
        return repository.save(item);
    }

    @Override
    public Optional<Item> updateItem(Item item) {
        return repository.update(item);
    }

    @Override
    public boolean deleteItem(long id, long userId) {
        return repository.delete(id, userId);
    }

    @Override
    public Optional<Item> getItem(long id) {
        return repository.get(id);
    }

    @Override
    public Collection<Item> searchItems(String text) {
        return repository.search(text);
    }

    @Override
    public boolean checkItem(Item item) {
        return repository.checkItem(item);
    }
}
