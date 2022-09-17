package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;


public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getItemId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public static Booking toBooking(BookingDto bookingDto, Long bookerId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItemId(bookingDto.getItemId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setBookerId(bookerId);
        booking.setStatus(status);
        return booking;
    }

    public static BookingFullDto toBookingFullDto(Booking booking, User booker, Item item) {
        return new BookingFullDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booker,
                item
        );
    }
}
