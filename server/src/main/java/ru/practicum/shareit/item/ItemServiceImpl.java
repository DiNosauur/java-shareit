package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Collection<ItemFullDto> findUserItems(long userId) {
        log.info("Поиск всех вещей пользователя (id={})", userId);
        return repository.findByOwnerOrderById(userId)
                .stream()
                .map(item -> ItemMapper.toItemFullDto(item,
                        bookingRepository.findLastBooking(item.getId(), userId, LocalDateTime.now()),
                        bookingRepository.findNextBooking(item.getId(), userId, LocalDateTime.now()),
                        commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId())
                                .stream()
                                .map(comment -> CommentMapper.toCommentDto(comment, validateUser(comment.getAuthorId()).getName()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private User validateUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
        return user.get();
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

    private boolean validateBookingItem(long itemId, long userId) {
        Optional<Booking> booking = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (booking.isPresent()) {
            return true;
        } else {
            throw new ValidationException(String.format(
                    "Пользователь (id = %s) не брал вещь (id = %s) в аренду", userId, itemId));
        }
    }

    @Transactional
    @Override
    public Item saveItem(ItemDto itemDto, long userId) {
        log.info("Добавление вещи {} пользователем (id={})", itemDto.toString(), userId);
        validateUser(userId);
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Не передан статус вещи");
        }
        Item item = ItemMapper.toItem(itemDto, userId);
        return repository.save(item);
    }

    @Transactional
    @Override
    public Optional<Item> updateItem(long itemId, ItemDto itemDto, long userId) {
        log.info("Редактирование информации {} о вещи (id={}) пользователем (id={})",
                itemDto.toString(), itemId, userId);
        validateUser(userId);
        Optional<Item> itemOld = validateUserItem(itemId, userId);
        Item item = ItemMapper.toItem(itemDto, itemOld.get());
        repository.save(item);
        return Optional.of(item);
    }

    @Transactional
    @Override
    public boolean deleteItem(long id, long userId) {
        log.info("Удаление вещи (id={}) пользователем (id={})", id, userId);
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
    public Optional<ItemFullDto> getItem(long id, long userId) {
        log.info("Получение информации о вещи (id={}) пользователем (id={})", id, userId);
        Optional<Item> item = repository.findById(id);
        if (item.isPresent()) {
            return Optional.of(ItemMapper.toItemFullDto(item.get(),
                    bookingRepository.findLastBooking(id, userId, LocalDateTime.now()),
                    bookingRepository.findNextBooking(id, userId, LocalDateTime.now()),
                    commentRepository.findAllByItemIdOrderByCreatedDesc(id)
                            .stream()
                            .map(comment -> CommentMapper.toCommentDto(comment, validateUser(comment.getAuthorId()).getName()))
                            .collect(Collectors.toList())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Item> searchItems(String text) {
        log.info("Поиск вещи по строке ({})", text);
        return text == null || text.isBlank() ? new ArrayList<>() : repository.search(text);
    }

    @Transactional
    @Override
    public Optional<CommentDto> addItemComment(long itemId, long userId, CommentDto commentDto) {
        log.info("Добавление комментария ({}) о вещи (id={}) пользователем (id={})",
                commentDto.getText(), itemId, userId);
        User user = validateUser(userId);
        if (validateBookingItem(itemId, userId)) {
            Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, itemId, userId));
            return Optional.of(CommentMapper.toCommentDto(comment, user.getName()));
        } else {
            return Optional.empty();
        }
    }
}
