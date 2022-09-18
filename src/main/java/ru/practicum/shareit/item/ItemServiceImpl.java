package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Collection<ItemBookingDto> findUserItems(long userId) {
        return repository.findByOwner(userId)
                .stream()
                .map(item -> ItemMapper.toItemBookingDto(item,
                        bookingRepository.findByItemIdAndBookerIdNotAndStartBeforeOrderByStartDesc(item.getId(), userId, LocalDateTime.now()),
                        bookingRepository.findByItemIdAndBookerIdNotAndStartAfterOrderByStartAsc(item.getId(), userId, LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    private void validateUser(long userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
    }

    private Optional<Item> validateUserItem(long itemId, long userId) {
        Optional<Item> item = repository.findById(itemId);
        if (!item.isPresent()) {
            throw new NotFoundException(String.format("Вещь (id = %s) не найдена", itemId));
        } else if (!item.get().getOwner().equals(userId)) {
            throw new NotFoundException(String.format(
                    "Вещь (id = %s) не найдена у пользователя (id = %s)", itemId, userId));
        }
        return item;
    }

    @Transactional
    @Override
    public Item saveItem(ItemDto itemDto, long userId) {
        validateUser(userId);
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Не передан статус вещи");
        }
        Item item = ItemMapper.toItem(itemDto, userId, null);
        return repository.save(item);
    }

    @Transactional
    @Override
    public Optional<Item> updateItem(long itemId, ItemDto itemDto, long userId) {
        validateUser(userId);
        Optional<Item> itemOld = validateUserItem(itemId, userId);
        Item item = ItemMapper.toItem(itemDto, itemOld.get());
        repository.save(item);
        return Optional.of(item);
    }

    @Transactional
    @Override
    public boolean deleteItem(long id, long userId) {
        validateUser(userId);
        Optional<Item> item = repository.findById(id);
        if (item.isPresent()) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<ItemBookingDto> getItem(long id, long userId) {
        Optional<Item> item = repository.findById(id);
        if (item.isPresent()) {
            return Optional.of(ItemMapper.toItemBookingDto(item.get(),
                    bookingRepository.findByItemIdAndBookerIdNotAndStartBeforeOrderByStartDesc(id, userId, LocalDateTime.now()),
                    bookingRepository.findByItemIdAndBookerIdNotAndStartAfterOrderByStartAsc(id, userId, LocalDateTime.now())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Item> searchItems(String text) {
        return text == null || text.isBlank() ? new ArrayList<>() : repository.search(text);
    }
}
