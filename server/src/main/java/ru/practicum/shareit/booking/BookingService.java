package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import java.util.Collection;
import java.util.Optional;

public interface BookingService {
    BookingFullDto saveBooking(BookingDto bookingDto, Long bookerId);

    Optional<BookingFullDto> updateBooking(Long id, Long ownerId, Boolean approved);

    Collection<BookingFullDto> findUserBookings(long bookerId, String state, int from, int size);

    Collection<BookingFullDto> findOwnerBookings(long ownerId, String state, int from, int size);

    Optional<BookingFullDto> getBooking(long id, long userId);
}
