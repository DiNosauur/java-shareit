package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository repository;

    private User owner = new User();

    private User booker = new User();

    private Item item = new Item();

    private Booking lastBooking = new Booking();

    private Booking nextBooking = new Booking();

    private long ownerId;
    private long bookerId;
    private long itemId;

    @BeforeEach
    void setup() {
        owner.setName("Dima");
        owner.setEmail("dimano@mail.ru");
        ownerId = em.persist(owner).getId();

        item.setName("Дрель");
        item.setDescription("Инструмент для сверления");
        item.setAvailable(true);
        item.setOwner(ownerId);
        itemId = em.persist(item).getId();

        booker.setName("DN");
        booker.setEmail("dimano@yandex.ru");
        bookerId = em.persist(booker).getId();

        LocalDateTime lastStart = LocalDateTime.now().minusDays(10);
        LocalDateTime lastEnd = lastStart.plusDays(7);

        lastBooking.setItemId(itemId);
        lastBooking.setBookerId(bookerId);
        lastBooking.setStart(lastStart);
        lastBooking.setEnd(lastEnd);
        lastBooking.setStatus(BookingStatus.APPROVED);
        em.persist(lastBooking);

        LocalDateTime nextStart = LocalDateTime.now().plusDays(1);
        LocalDateTime nextEnd = nextStart.plusDays(7);

        nextBooking.setItemId(itemId);
        nextBooking.setBookerId(bookerId);
        nextBooking.setStart(nextStart);
        nextBooking.setEnd(nextEnd);
        nextBooking.setStatus(BookingStatus.WAITING);
        em.persist(nextBooking);
    }

    @Test
    void findAllByOwnerId() {
        Page<Booking> foundBookings = repository
                .findAllByOwnerId(ownerId, "ALL", LocalDateTime.now(), PageRequest.of(0, 20));
        assertThat(foundBookings).hasSize(2);
    }

    @Test
    void findLastBookingIsPresent() {
        Optional<Booking> foundBookings = repository.findLastBooking(itemId, ownerId, LocalDateTime.now());
        assertThat(foundBookings.isPresent());
    }

    @Test
    void findLastBookingIsEmpty() {
        Optional<Booking> foundBookings = repository.findLastBooking(itemId, ownerId, LocalDateTime.now().minusDays(11));
        assertThat(foundBookings.isEmpty());
    }

    @Test
    void findNextBookingIsPresent() {
        Optional<Booking> foundBookings = repository.findNextBooking(itemId, ownerId, LocalDateTime.now());
        assertThat(foundBookings.isPresent());
    }

    @Test
    void findNextBookingIsEmpty() {
        Optional<Booking> foundBookings = repository.findNextBooking(itemId, ownerId, LocalDateTime.now().plusDays(11));
        assertThat(foundBookings.isEmpty());
    }
}