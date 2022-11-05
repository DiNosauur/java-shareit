package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService service;

    @PostMapping
    public ResponseEntity<BookingFullDto> createBooking(@RequestBody BookingDto bookingDto,
                                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return new ResponseEntity<>(service.saveBooking(bookingDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingFullDto> updateBooking(@PathVariable long id,
                                                        @RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam Boolean approved) {
        return service.updateBooking(id, userId, approved)
                .map(updatedBooking -> new ResponseEntity<>(updatedBooking, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingFullDto> findBookingById(@PathVariable long id,
                                                          @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getBooking(id, userId).map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Collection<BookingFullDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "20") int size) {
        return service.findUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingFullDto> findOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam(defaultValue = "ALL") String state,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "20") int size) {
        return service.findOwnerBookings(userId, state, from, size);
    }
}
