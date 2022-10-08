package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private User validateUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
        return user.get();
    }

    private Item validateItem(BookingDto bookingDto, Long bookerId) {
        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if (!item.isPresent()) {
            throw new NotFoundException(String.format("Вещь (id = %s) не найдена", bookingDto.getItemId()));
        } else if (!item.get().getAvailable()) {
            throw new ValidationException(String.format("Вещь (id = %s) не доступна", bookingDto.getItemId()));
        } else if (bookerId != null && item.get().getOwner().equals(bookerId)) {
            throw new NotFoundException(String.format("Нельзя забронировать вещь (id = %s), являясь её владельцем",
                    bookingDto.getItemId()));
        } else if (repository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                bookingDto.getItemId(), BookingStatus.APPROVED, bookingDto.getEnd(), bookingDto.getStart()).size() > 0) {
            throw new ValidationException(String.format("Вещь (id = %s) уже забронирована на эти даты", bookingDto.getItemId()));
        }
        return item.get();
    }

    private Booking validateBooking(Long bookingId, Long ownerId, Long bookerId) {
        Optional<Booking> booking = repository.findById(bookingId);
        if (!booking.isPresent()) {
            throw new NotFoundException(String.format("Бронь (id = %s) не найдена", bookingId));
        } else {
            Item item = itemRepository.findById(booking.get().getItemId()).get();
            if (bookerId == null && !item.getOwner().equals(ownerId)) {
                throw new NotFoundException(String.format(
                        "Пользователь (id = %s) не является владельцем вещи (id = %s)", ownerId, booking.get().getItemId()));
            } else if (ownerId == null && !booking.get().getBookerId().equals(bookerId)) {
                throw new NotFoundException(String.format(
                        "Пользователь (id = %s) не является автором бронирования вещи (id = %s)",
                        bookerId, booking.get().getItemId()));
            } else if (bookerId != null && ownerId != null
                    && !booking.get().getBookerId().equals(bookerId)
                    && !item.getOwner().equals(ownerId)) {
                throw new NotFoundException(String.format(
                        "Пользователь (id = %s) не является ни автором бронирования, ни владельцем вещи (id = %s)",
                        bookerId, booking.get().getItemId()));
            }
        }
        return booking.get();
    }

    private void validateBooking(BookingDto bookingDto) {
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException(String.format("Дата начала брони (%s) находится в прошлом", bookingDto.getStart()));
        } else if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException(String.format("Дата окончания брони (%s) находится в прошлом", bookingDto.getEnd()));
        } else if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException(String.format("Дата окончания брони (%s) раньше даты начала (%s)",
                    bookingDto.getEnd(), bookingDto.getStart()));
        }
    }

    private int validatePage(int from, int size) {
        if (size <= 0) {
            throw new ValidationException(String.format("Параметр size (%s) задан некорректно", size));
        }
        if (from < 0) {
            throw new ValidationException(String.format("Параметр from (%s) задан некорректно", from));
        }
        int page = from / size;
        return page;
    }

    @Transactional
    @Override
    public BookingFullDto saveBooking(BookingDto bookingDto, Long bookerId) {
        log.info("Заявка на бронирование {} от пользователя (id={})", bookingDto.toString(), bookerId);
        validateBooking(bookingDto);
        User booker = validateUser(bookerId);
        Item item = validateItem(bookingDto, bookerId);
        Booking booking = repository.save(BookingMapper.toBooking(bookingDto, bookerId, BookingStatus.WAITING));
        return BookingMapper.toBookingFullDto(booking, booker, item);
    }

    @Transactional
    @Override
    public Optional<BookingFullDto> updateBooking(Long id, Long ownerId, Boolean approved) {
        log.info("Изменение статуса брони (id={}) от пользователя (id={}) на {}",
                id, ownerId, approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        validateUser(ownerId);
        Booking booking = validateBooking(id, ownerId, null);
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(String.format("Статус брони отличен от %s", BookingStatus.WAITING));
        }
        User booker = validateUser(booking.getBookerId());
        Item item = validateItem(BookingMapper.toBookingDto(booking), booking.getBookerId());
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return Optional.of(BookingMapper.toBookingFullDto(repository.save(booking), booker, item));
    }

    @Override
    public Collection<BookingFullDto> findUserBookings(long bookerId, String state, int from, int size) {
        log.info("Поиск бронирований от пользователя (id={}) для state = {}", bookerId, state);
        User booker = validateUser(bookerId);
        int page = validatePage(from, size);
        if (state.equals(BookingState.ALL.name())) {
            return repository.findAllByBookerId(bookerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else if (state.equals(BookingState.CURRENT.name())) {
            return repository.findAllByBookerIdAndStartBeforeAndEndAfter(
                    bookerId, LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else if (state.equals(BookingState.PAST.name())) {
            return repository.findAllByBookerIdAndEndBefore(
                    bookerId, LocalDateTime.now(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else if (state.equals(BookingState.FUTURE.name())) {
            return repository.findAllByBookerIdAndStartAfter(
                    bookerId, LocalDateTime.now(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else if (state.equals(BookingState.WAITING.name())) {
            return repository.findAllByBookerIdAndStatus(
                    bookerId, BookingStatus.WAITING, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else if (state.equals(BookingState.REJECTED.name())) {
            return repository.findAllByBookerIdAndStatus(
                    bookerId, BookingStatus.REJECTED, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start")))
                    .stream()
                    .map(booking -> BookingMapper.toBookingFullDto(booking, booker, itemRepository.findById(booking.getItemId()).get()))
                    .collect(Collectors.toList());
        } else {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public Collection<BookingFullDto> findOwnerBookings(long ownerId, String state, int from, int size) {
        log.info("Поиск бронирований вещей пользователя (id={}) для state = {}", ownerId, state);
        validateUser(ownerId);
        try {
            BookingState bookingState = BookingState.valueOf(state);
        } catch (Exception e) {
            throw new ValidationException(String.format("Unknown state: %s", state));
        }
        int page = validatePage(from, size);
        return repository.findAllByOwnerId(ownerId, state, LocalDateTime.now(), PageRequest.of(page, size))
                .stream()
                .map(booking -> BookingMapper.toBookingFullDto(booking,
                        userRepository.findById(booking.getBookerId()).get(),
                        itemRepository.findById(booking.getItemId()).get()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookingFullDto> getBooking(long id, long userId) {
        log.info("Запрос брони (id={}) от пользователя (id={})", id, userId);
        Booking booking = validateBooking(id, userId, userId);
        Item item = validateItem(BookingMapper.toBookingDto(booking), null);
        User booker = validateUser(booking.getBookerId());
        return Optional.of(BookingMapper.toBookingFullDto(booking, booker, item));
    }
}
