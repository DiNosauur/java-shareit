package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Comment;
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
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService service;

    private User user;
    private ItemDto itemDto;
    private Item item;
    private List<Item> sourceItems;

    @Test
    void findUserItems() {
        // given
        User user = makeUser("dimano@mail.ru", "Dima");
        em.persist(user);
        em.flush();

        List<Item> sourceItems = List.of(
                makeItem(user.getId(), "лодка", "плавсредство"),
                makeItem(user.getId(), "палатка", "укрытие"),
                makeItem(user.getId(), "удочка", "инструмент для ловли рыбы")
        );

        for (Item sourceItem : sourceItems) {
            em.persist(sourceItem);
        }
        em.flush();

        // when
        Collection<ItemFullDto> targetItems = service.findUserItems(user.getId());

        // then
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (Item sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("name", equalTo(sourceItem.getName()))
            )));
        }
    }

    private void givenItems() {
        user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);
        itemDto = new ItemDto(user.getId(), "удочка", "инструмент для ловли рыбы", null, null);
        item = makeItem(user.getId(), itemDto.getName(), itemDto.getDescription());
        item.setId(1L);
        sourceItems = List.of(
                makeItem(user.getId(), "байдарка", "плавсредство для похода"),
                makeItem(user.getId(), "палатка", "переносной домик для похода"),
                makeItem(user.getId(), "удочка", "инструмент для ловли рыбы в походе")
        );
        long id = 1;
        for (Item sourceItem : sourceItems) {
            sourceItem.setId(id++);
        }
    }

    @Test
    void saveItemUserIsNotFound() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        // when
        final NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.saveItem(itemDto, 2L));

        // then
        Assertions.assertEquals("Пользователь (id = 2) не найден", notFoundException.getMessage());
    }

    @Test
    void saveItemStatusIsNotTransferred() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.saveItem(itemDto, 1L));

        // then
        Assertions.assertEquals("Не передан статус вещи", validationException.getMessage());
    }

    @Test
    void saveItemIsOk() {
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        itemDto.setAvailable(true);

        Assertions.assertEquals(item, itemService.saveItem(itemDto, 1L));
    }

    @Test
    void updateItemIsNotFound() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(2L, itemDto, 1L));

        // then
        Assertions.assertEquals("Вещь (id = 2) не найдена", notFoundException.getMessage());
    }

    @Test
    void updateItemUserIsNotOwner() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        // when
        NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(1L, itemDto, 2L));

        // then
        Assertions.assertEquals("Вещь (id = 1) не найдена у пользователя (id = 2)",
                notFoundException.getMessage());
    }

    @Test
    void updateItemIsOk() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockItemRepository.save(Mockito.any()))
                .thenReturn(item);

        itemDto.setName("Спининг");

        // when and then
        Assertions.assertEquals("Спининг",
                itemService.updateItem(1L, itemDto, 1L).get().getName());
    }

    @Test
    void deleteItemIsOk() {
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Assertions.assertEquals(true, itemService.deleteItem(1L, 1L));
    }

    @Test
    void deleteItemIsNotFound() {
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Assertions.assertEquals(false, itemService.deleteItem(2L, 1L));
    }

    @Test
    void getItem() {
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Assertions.assertEquals("удочка", itemService.getItem(1L, 1L).get().getName());
    }

    @Test
    void searchItems() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.search(Mockito.anyString()))
                .thenReturn(sourceItems);

        // when
        Collection<Item> targetItems =
                itemService.searchItems("поход");

        // then
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (Item sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("name", equalTo(sourceItem.getName()))
            )));
        }
    }

    @Test
    void addItemCommentUserIsNotBooker() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(user.getId(), item.getId(), now.minusDays(14), now.minusDays(11),
                BookingStatus.APPROVED);
        booking.setId(1L);

        CommentDto commentDto = new CommentDto(1L, "спининг", "Dmitriy", now);

        Comment comment = CommentMapper.toComment(commentDto, user.getId(), item.getId());
        comment.setId(1L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        Mockito
                .when(mockCommentRepository.save(Mockito.any()))
                .thenReturn(comment);

        // when
        ValidationException validationException = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.addItemComment(2L, 1L, commentDto));

        // then
        Assertions.assertEquals("Пользователь (id = 1) не брал вещь (id = 2) в аренду",
                validationException.getMessage());
    }

    @Test
    void addItemCommentIsOk() {
        // given
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        BookingRepository mockBookingRepository = Mockito.mock(BookingRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ItemServiceImpl itemService =
                new ItemServiceImpl(mockItemRepository, mockUserRepository, mockBookingRepository,
                        mockCommentRepository);

        givenItems();

        LocalDateTime now = LocalDateTime.now();

        Booking booking = makeBooking(user.getId(), item.getId(), now.minusDays(14), now.minusDays(11),
                BookingStatus.APPROVED);
        booking.setId(1L);

        CommentDto commentDto = new CommentDto(1L, "спининг", "Dmitriy", now);

        Comment comment = CommentMapper.toComment(commentDto, user.getId(), item.getId());
        comment.setId(1L);

        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(mockBookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        Mockito
                .when(mockCommentRepository.save(Mockito.any()))
                .thenReturn(comment);

        Mockito
                .when(mockBookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(booking));

        // when and then
        Assertions.assertEquals(true,
                itemService.addItemComment(1L, 1L, commentDto).isPresent());
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

    private Booking makeBooking(long userId, long itemId, LocalDateTime start, LocalDateTime end,
                                BookingStatus status) {
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setBookerId(userId);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }

}