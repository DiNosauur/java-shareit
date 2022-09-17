package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService service;

    @GetMapping
    public Collection<BookingFullDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        return service.findUserBookings(userId, state);
    }

    @PostMapping
    public ResponseEntity<BookingFullDto> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return new ResponseEntity<>(service.saveBooking(bookingDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingFullDto> updateBooking(@PathVariable long id,
                                                        @RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam Boolean approved) {
        return service.updateBooking(id, userId, approved).map(updatedBooking -> new ResponseEntity<>(updatedBooking, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingFullDto> findBookingById(@PathVariable long id,
                                                   @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getBooking(id, userId).map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
