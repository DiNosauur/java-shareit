package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByBookerId(long bookerId, Sort sort);

    Collection<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    Collection<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime end, Sort sort);

    Collection<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime start, Sort sort);

    Collection<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status, Sort sort);

    Collection<Booking> findByItemIdAndStatusAndStartBeforeAndEndAfter(
            long itemId, BookingStatus status, LocalDateTime start, LocalDateTime end);

    @Query(" select b from Item i, Booking b " +
            " where i.owner = ?1 " +
            "   and b.itemId = i.id " +
            "   and (?2 = 'ALL' or " +
            "        (?2 = 'CURRENT' and ?3 between b.start and b.end) or " +
            "        (?2 = 'PAST' and ?3 > b.end) or " +
            "        (?2 = 'FUTURE' and ?3 < b.start) or " +
            "        (?2 = 'WAITING' and b.status = ?2) or " +
            "        (?2 = 'REJECTED' and b.status = ?2) " +
            "       )" +
            " order by b.start desc")
    Collection<Booking> findAllByOwnerId(long ownerId, String state, LocalDateTime localDateTime);

    Optional<Booking> findByItemIdAndBookerIdNotAndStartBeforeOrderByStartDesc(long itemId, long userId, LocalDateTime now);

    Optional<Booking> findByItemIdAndBookerIdNotAndStartAfterOrderByStartAsc(long itemId, long userId, LocalDateTime now);
}
