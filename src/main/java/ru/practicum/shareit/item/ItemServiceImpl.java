package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Override
    public Collection<Item> findUserItems(long userId) {
        return repository.findUserItems(userId);
    }

    @Override
    public Item saveItem(ItemDto itemDto, long userId) {
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Не передан статус вещи");
        }
        if (!userRepository.get(userId).isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
        User user = userRepository.get(userId).get();
        Item item = ItemMapper.toItem(itemDto, user, null);
        return repository.save(item);
    }

    @Override
    public Optional<Item> updateItem(long itemId, ItemDto itemDto, long userId) {
        if (!userRepository.get(userId).isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
        User user = userRepository.get(userId).get();
        itemDto.setId(itemId);
        Item item = ItemMapper.toItem(itemDto, user, null);
        if (!getItem(itemId).isPresent()) {
            throw new NotFoundException(String.format("Вещь (id = %s) не найдена", itemId));
        } else if (!getItem(itemId).get().getOwner().equals(user)) {
            throw new NotFoundException(String.format(
                    "Вещь (id = %s) не найдена у пользователя (id = %s)", itemId, userId));
        }
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
}
