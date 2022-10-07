package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        User user = makeUser("dimano@mail.ru", "Dima");
        em.persist(user);
        em.flush();

        User owner = makeUser("owner@mail.ru", "Owner");
        em.persist(owner);
        em.flush();

        Item item = makeItem(owner.getId(), "удочка", "инструмент для ловли рыбы");
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

        assertThat(service.findUserBookings(user.getId(), "CURRENT", 0, 20), hasSize(0));
        assertThat(service.findUserBookings(user.getId(), "PAST", 0, 20), hasSize(2));
        assertThat(service.findUserBookings(user.getId(), "FUTURE", 0, 20), hasSize(1));
        assertThat(service.findUserBookings(user.getId(), "WAITING", 0, 20), hasSize(1));
        assertThat(service.findUserBookings(user.getId(), "REJECTED", 0, 20), hasSize(0));
    }

    @Test
    void saveBooking() {
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Item item = makeItem(user.getId(), "удочка", "инструмент для ловли рыбы");
        item.setId(1L);
        item.setAvailable(false);

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(user.getId(), item.getId(), now.plusDays(1), now.plusDays(11), BookingStatus.APPROVED);
        booking.setId(1L);

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(1), now.plusDays(5));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 2L));

        Assertions.assertEquals("Пользователь (id = 2) не найден", notFoundException.getMessage());

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals("Вещь (id = 1) не найдена", notFoundException.getMessage());

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals("Вещь (id = 1) не доступна", validationException.getMessage());

        item.setAvailable(true);

        notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals("Нельзя забронировать вещь (id = 1), являясь её владельцем", notFoundException.getMessage());

        Mockito
                .when(mockBookingRepository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(booking));

        item.setOwner(2L);

        validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals("Вещь (id = 1) уже забронирована на эти даты", validationException.getMessage());

        }

    @Test
    void updateBooking() {
    }

    @Test
    void findOwnerBookings() {
    }

    @Test
    void getBooking() {
    }

    private Item makeItem(long userId, String name, String desc) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(desc);
        item.setAvailable(true);
        item.setOwner(userId);
        return item;
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
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