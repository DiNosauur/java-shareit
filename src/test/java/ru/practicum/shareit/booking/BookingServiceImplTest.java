package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingService service;

    @Test
    void findUserBookings() {
        User user = new User();
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
        em.persist(user);
        em.flush();

        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");
        em.persist(owner);
        em.flush();

        Item item = new Item();
        item.setName("удочка");
        item.setDescription("инструмент для ловли рыбы");
        item.setAvailable(true);
        item.setOwner(owner.getId());
        em.persist(item);
        em.flush();

        LocalDateTime now = LocalDateTime.now();

        List<Booking> sourceBookings = List.of(
                makeBooking(user.getId(), item.getId(), now.minusDays(20), now.minusDays(15), BookingStatus.CANCELED),
                makeBooking(user.getId(), item.getId(), now.minusDays(14), now.minusDays(11), BookingStatus.APPROVED),
                makeBooking(user.getId(), item.getId(), now.plusDays(100), now.plusDays(120), BookingStatus.WAITING)
        );

        for (Booking sourceBooking : sourceBookings) {
            em.persist(sourceBooking);
        }
        em.flush();

        Collection<BookingFullDto> targetBookings = service.findUserBookings(user.getId(), "ALL", 0, 20);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("booker", equalTo(user)),
                    hasProperty("item", equalTo(item))
            )));
        }
    }

    private Booking makeBooking(long userId, long itemId, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setBookerId(userId);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }
}