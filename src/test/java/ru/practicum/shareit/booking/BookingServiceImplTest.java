package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
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

        Booking booking = makeBooking(user.getId(), item.getId(), now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        booking.setId(1L);

        BookingDto bookingDto = new BookingDto(item.getId(), now.minusDays(10), now.minusDays(5));

        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals(String.format("Дата начала брони (%s) находится в прошлом", bookingDto.getStart()),
                validationException.getMessage());

        bookingDto.setStart(now.plusDays(5));

        validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals(String.format("Дата окончания брони (%s) находится в прошлом", bookingDto.getEnd()),
                validationException.getMessage());

        bookingDto.setEnd(now.plusDays(1));

        validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        Assertions.assertEquals(String.format("Дата окончания брони (%s) раньше даты начала (%s)",
                bookingDto.getEnd(), bookingDto.getStart()),
                validationException.getMessage());

        bookingDto.setEnd(now.plusDays(10));

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

        validationException = Assertions.assertThrows(
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

        Mockito
                .when(mockBookingRepository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of());

        Mockito
                .when(mockBookingRepository.save(Mockito.any()))
                .thenReturn(booking);

        BookingFullDto bookingFullDto = bookingService.saveBooking(bookingDto, 1L);
        Assertions.assertEquals(booking.getItemId(), bookingFullDto.getItem().getId());
    }

    @Test
    void updateBooking() {
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Item item = makeItem(user.getId(), "удочка", "инструмент для ловли рыбы");
        item.setId(1L);

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(2L, item.getId(), now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        booking.setId(1L);

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBooking(2L, 1L, true));

        Assertions.assertEquals("Бронь (id = 2) не найдена", notFoundException.getMessage());

        notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBooking(1L, 2L, true));

        Assertions.assertEquals("Пользователь (id = 2) не является владельцем вещи (id = 1)", notFoundException.getMessage());

        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.updateBooking(1L, 1L, true));

        Assertions.assertEquals(String.format("Статус брони отличен от %s", BookingStatus.WAITING),
                validationException.getMessage());

        booking.setStatus(BookingStatus.WAITING);

        Mockito
                .when(mockBookingRepository.save(Mockito.any()))
                .thenReturn(booking);

        BookingFullDto bookingFullDto = bookingService.updateBooking(1L, 1L, true).get();
        Assertions.assertEquals(booking.getStatus(), bookingFullDto.getStatus());
    }

    @Test
    void findOwnerBookings() {
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Item item = makeItem(user.getId(), "удочка", "инструмент для ловли рыбы");
        item.setId(1L);

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(2L, item.getId(), now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        booking.setId(1L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.findOwnerBookings(1L, "LAST", 0, 20));

        Assertions.assertEquals("Unknown state: LAST", validationException.getMessage());

        validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.findOwnerBookings(1L, "ALL", -1, 20));

        Assertions.assertEquals("Параметр from (-1) задан некорректно", validationException.getMessage());

        validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.findOwnerBookings(1L, "ALL", 0, 0));

        Assertions.assertEquals("Параметр size (0) задан некорректно", validationException.getMessage());

        List<Booking> sourceBookings = List.of(
                makeBooking(1L, item.getId(), now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED),
                makeBooking(1L, item.getId(), now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED),
                makeBooking(1L, item.getId(), now.plusDays(5), now.plusDays(6), BookingStatus.WAITING)
        );
        long id = 2;
        for (Booking sourceBooking : sourceBookings) {
            sourceBooking.setId(id++);
        }

        Mockito
                .when(mockBookingRepository.findAllByOwnerId(
                        Mockito.anyLong(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(new PageImpl<>(sourceBookings));

        Collection<BookingFullDto> targetBookings =
                bookingService.findOwnerBookings(1L, "ALL", 0, 20);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBooking() {
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Item item = makeItem(user.getId(), "удочка", "инструмент для ловли рыбы");
        item.setId(1L);

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(2L, item.getId(), now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        booking.setId(1L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(2L, 1L));

        Assertions.assertEquals("Бронь (id = 2) не найдена", notFoundException.getMessage());

        notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(1L, 3L));

        Assertions.assertEquals("Пользователь (id = 3) не является ни автором бронирования, ни владельцем вещи (id = 1)", notFoundException.getMessage());

        BookingFullDto bookingFullDto = bookingService.getBooking(1L, 1L).get();
        Assertions.assertEquals(booking.getStatus(), bookingFullDto.getStatus());
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

    private Booking makeBooking(long userId, long itemId, LocalDateTime start, LocalDateTime end, BookingStatus
            status) {
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setBookerId(userId);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }
}