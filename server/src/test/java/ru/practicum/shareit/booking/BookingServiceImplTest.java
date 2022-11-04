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
        properties = "db.name=testDB",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingService service;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private List<Booking> sourceBookings;
    private LocalDateTime now = LocalDateTime.now();

    private void givenUserBookings() {
        user = makeUser("dimano@mail.ru", "Dima");
        em.persist(user);
        em.flush();

        owner = makeUser("owner@mail.ru", "Owner");
        em.persist(owner);
        em.flush();

        item = makeItem(owner.getId(), "удочка", "инструмент для ловли рыбы");
        em.persist(item);
        em.flush();

        sourceBookings = List.of(
                makeBooking(user.getId(), item.getId(), now.minusDays(20), now.minusDays(15), BookingStatus.CANCELED),
                makeBooking(user.getId(), item.getId(), now.minusDays(14), now.minusDays(11), BookingStatus.APPROVED),
                makeBooking(user.getId(), item.getId(), now.plusDays(100), now.plusDays(120), BookingStatus.WAITING)
        );

        for (Booking sourceBooking : sourceBookings) {
            em.persist(sourceBooking);
        }
        em.flush();
    }

    @Test
    void findUserBookingsStateALL() {
        // given
        givenUserBookings();

        // when
        Collection<BookingFullDto> targetBookings = service.findUserBookings(user.getId(), "ALL", 0, 20);

        // then
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("booker", equalTo(user)),
                    hasProperty("item", equalTo(item))
            )));
        }
    }

    @Test
    void findUserBookingsStateCURRENT() {
        givenUserBookings();
        assertThat(service.findUserBookings(user.getId(), "CURRENT", 0, 20), hasSize(0));
    }

    @Test
    void findUserBookingsStatePAST() {
        givenUserBookings();
        assertThat(service.findUserBookings(user.getId(), "PAST", 0, 20), hasSize(2));
    }

    @Test
    void findUserBookingsStateFUTURE() {
        givenUserBookings();
        assertThat(service.findUserBookings(user.getId(), "FUTURE", 0, 20), hasSize(1));
    }

    @Test
    void findUserBookingsStateWAITING() {
        givenUserBookings();
        assertThat(service.findUserBookings(user.getId(), "WAITING", 0, 20), hasSize(1));
    }

    @Test
    void findUserBookingsStateREJECTED() {
        givenUserBookings();
        assertThat(service.findUserBookings(user.getId(), "REJECTED", 0, 20), hasSize(0));
    }

    private void givenBookings(int startPlus, int endPlus, boolean available, long ownerId, long bookerId) {
        user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        item = makeItem(ownerId, "удочка", "инструмент для ловли рыбы");
        item.setId(1L);
        item.setAvailable(available);

        booking = makeBooking(
                bookerId, item.getId(), now.plusDays(startPlus), now.plusDays(endPlus), BookingStatus.APPROVED);
        booking.setId(1L);
    }

    private void givenBookings(boolean available) {
        givenBookings(5, 10, available, 1L, 1L);
    }

    private void givenBookings(long ownerId, long bookerId) {
        givenBookings(5, 10, true, ownerId, bookerId);
    }

    private void givenBookings(long ownerId) {
        givenBookings(5, 10, true, ownerId, 1L);
    }

    private void givenBookings() {
        givenBookings(5, 10, true, 1L, 1L);
    }

    @Test
    void saveBookingUserIsNotFound() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings();

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 2L));

        // then
        Assertions.assertEquals("Пользователь (id = 2) не найден", notFoundException.getMessage());
    }

    @Test
    void saveBookingItemIsNotFound() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings();

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        // then
        Assertions.assertEquals("Вещь (id = 1) не найдена", notFoundException.getMessage());
    }

    @Test
    void saveBookingItemIsNotAvailable() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(false);

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        // then
        Assertions.assertEquals("Вещь (id = 1) не доступна", validationException.getMessage());
    }

    @Test
    void saveBookingBookerIsTheOwner() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings();

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        // then
        Assertions.assertEquals("Нельзя забронировать вещь (id = 1), являясь её владельцем", notFoundException.getMessage());
    }

    @Test
    void saveBookingItemIsBooked() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(2L);

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(booking));

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.saveBooking(bookingDto, 1L));

        // then
        Assertions.assertEquals("Вещь (id = 1) уже забронирована на эти даты", validationException.getMessage());
    }

    @Test
    void saveBookingIsOk() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(2L);

        BookingDto bookingDto = new BookingDto(item.getId(), now.plusDays(5), now.plusDays(10));

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(booking));

        Mockito
                .when(mockBookingRepository.findByItemIdAndStatusAndStartBeforeAndEndAfter(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of());

        Mockito
                .when(mockBookingRepository.save(Mockito.any()))
                .thenReturn(booking);

        // when
        BookingFullDto bookingFullDto = bookingService.saveBooking(bookingDto, 1L);

        // then
        Assertions.assertEquals(booking.getItemId(), bookingFullDto.getItem().getId());
    }

    @Test
    void updateBookingIsNotFound() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBooking(2L, 1L, true));

        // then
        Assertions.assertEquals("Бронь (id = 2) не найдена", notFoundException.getMessage());
    }

    @Test
    void updateBookingUserIsNotOwner() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBooking(1L, 2L, true));

        // then
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
    void updateBookingStatusIsNotWAITING() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.updateBooking(1L, 1L, true));

        // then
        Assertions.assertEquals(String.format("Статус брони отличен от %s", BookingStatus.WAITING),
                validationException.getMessage());
    }

    @Test
    void updateBookingStatusIsOk() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);
        booking.setStatus(BookingStatus.WAITING);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        Mockito
                .when(mockBookingRepository.save(Mockito.any()))
                .thenReturn(booking);

        // when
        BookingFullDto bookingFullDto = bookingService.updateBooking(1L, 1L, true).get();

        // then
        Assertions.assertEquals(booking.getStatus(), bookingFullDto.getStatus());
    }

    @Test
    void findOwnerBookingsUnknownStateLAST() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L,2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.findOwnerBookings(1L, "LAST", 0, 20));

        // then
        Assertions.assertEquals("Unknown state: LAST", validationException.getMessage());
    }

    @Test
    void findOwnerBookingsIsOk() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L,2L);
        List<Booking> sourceBookings = List.of(
                makeBooking(2L, item.getId(), now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED),
                makeBooking(2L, item.getId(), now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED),
                makeBooking(2L, item.getId(), now.plusDays(5), now.plusDays(6), BookingStatus.WAITING)
        );
        long id = 1;
        for (Booking sourceBooking : sourceBookings) {
            sourceBooking.setId(id++);
        }

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        Mockito
                .when(mockBookingRepository.findAllByOwnerId(
                        Mockito.anyLong(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(new PageImpl<>(sourceBookings));

        // when
        Collection<BookingFullDto> targetBookings =
                bookingService.findOwnerBookings(1L, "ALL", 0, 20);

        // then
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getBookingIsNotFound() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(2L, 1L));

        // then
        Assertions.assertEquals("Бронь (id = 2) не найдена", notFoundException.getMessage());
    }

    @Test
    void getBookingUserIsNotCorrect() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(1L, 3L));

        // then
        Assertions.assertEquals("Пользователь (id = 3) не является ни автором бронирования, ни владельцем вещи (id = 1)", notFoundException.getMessage());
    }

    @Test
    void getBookingIsOk() {
        // given
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingServiceImpl bookingService =
                new BookingServiceImpl(mockBookingRepository, mockItemRepository, mockUserRepository);

        givenBookings(1L, 2L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // when
        BookingFullDto bookingFullDto = bookingService.getBooking(1L, 1L).get();

        // then
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