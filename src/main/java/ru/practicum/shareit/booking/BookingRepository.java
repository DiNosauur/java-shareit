package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByBookerId(long bookerId, Sort sort);

    Collection<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    Collection<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime end, Sort sort);

    Collection<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime start, Sort sort);

    Collection<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status, Sort sort);

    Collection<Booking> findByItemIdAndStatusAndStartBeforeAndEndAfter(
            long itemId, BookingStatus status, LocalDateTime start, LocalDateTime end);
}
