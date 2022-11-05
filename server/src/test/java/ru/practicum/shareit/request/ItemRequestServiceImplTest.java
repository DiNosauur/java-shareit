package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
class ItemRequestServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;

    @Test
    void findUserItemRequests() {
        // given
        User user = makeUser("dimano@mail.ru", "Dima");
        em.persist(user);
        em.flush();

        List<ItemRequest> sourceItemRequests = List.of(
                makeItemRequest(user.getId(), "4-местная байдарка", LocalDateTime.now()),
                makeItemRequest(user.getId(), "палатка", LocalDateTime.now()),
                makeItemRequest(user.getId(), "складной столик", LocalDateTime.now())
        );

        for (ItemRequest sourceItemRequest : sourceItemRequests) {
            em.persist(sourceItemRequest);
        }
        em.flush();

        // when
        Collection<ItemRequestFullDto> targetItemRequests = service.findUserItemRequests(user.getId(), 0, 20);

        // then
        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequest sourceItemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequest.getDescription())),
                    hasProperty("created", equalTo(sourceItemRequest.getCreated()))
            )));
        }
    }

    private ItemRequest makeItemRequest(long userId, String desc, LocalDateTime created) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(userId);
        itemRequest.setCreated(created);
        itemRequest.setDescription(desc);
        return itemRequest;
    }

    @Test
    void saveItemRequest() {
        // given
        User user = makeUser("dimano@mail.ru", "Dima");
        long userId = userService.saveUser(user).getId();

        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "палатка");

        // when
        service.saveItemRequest(itemRequestDto, userId);

        // then
        TypedQuery<ItemRequest> query =
                em.createQuery("Select r from ItemRequest r where r.description = :desc", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("desc", itemRequestDto.getDescription()).getSingleResult();

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void findAllItemRequestsUserIsNotFaund() {
        // given
        ItemRequestRepository mockRepository = Mockito.mock(ItemRequestRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        ItemRequestServiceImpl itemRequestService =
                new ItemRequestServiceImpl(mockRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // when
        final NotFoundException notFoundException = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findAllItemRequests(2L, 0, 20));

        // then
        Assertions.assertEquals("Пользователь (id = 2) не найден", notFoundException.getMessage());
    }

    @Test
    void findAllItemRequestsIsOk() {
        // given
        ItemRequestRepository mockRepository = Mockito.mock(ItemRequestRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        ItemRequestServiceImpl itemRequestService =
                new ItemRequestServiceImpl(mockRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        List<ItemRequest> sourceItemRequests = List.of(
                makeItemRequest(user.getId(), "4-местная байдарка", LocalDateTime.now()),
                makeItemRequest(user.getId(), "палатка", LocalDateTime.now()),
                makeItemRequest(user.getId(), "складной столик", LocalDateTime.now())
        );
        long id = 1;
        for (ItemRequest sourceItemRequest : sourceItemRequests) {
            sourceItemRequest.setId(id++);
        }

        Mockito
                .when(mockRepository.findByRequestorNotOrderByCreatedDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(sourceItemRequests));

        // when
        Collection<ItemRequestFullDto> targetItemRequests =
                itemRequestService.findAllItemRequests(user.getId(), 0, 20);

        // then
        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequest sourceItemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequest.getDescription())),
                    hasProperty("created", equalTo(sourceItemRequest.getCreated()))
            )));
        }
    }

    @Test
    void getItemRequestIsOk() {
        // given
        ItemRequestRepository mockRepository = Mockito.mock(ItemRequestRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        ItemRequestServiceImpl itemRequestService =
                new ItemRequestServiceImpl(mockRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ItemRequest itemRequest = makeItemRequest(user.getId(), "палатка", LocalDateTime.now());
        itemRequest.setId(1L);

        Mockito
                .when(mockRepository.findById(1L))
                .thenReturn(Optional.of(itemRequest));

        // when and then
        Assertions.assertEquals(true,
                itemRequestService.getItemRequest(1L, 1L).isPresent());
    }

    @Test
    void getItemRequestIsNotFound() {
        // given
        ItemRequestRepository mockRepository = Mockito.mock(ItemRequestRepository.class);
        ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        ItemRequestServiceImpl itemRequestService =
                new ItemRequestServiceImpl(mockRepository, mockItemRepository, mockUserRepository);

        User user = makeUser("dimano@mail.ru", "Dima");
        user.setId(1L);

        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ItemRequest itemRequest = makeItemRequest(user.getId(), "палатка", LocalDateTime.now());
        itemRequest.setId(1L);

        Mockito
                .when(mockRepository.findById(1L))
                .thenReturn(Optional.of(itemRequest));

        // when and then
        Assertions.assertEquals(true,
                itemRequestService.getItemRequest(2L, 1L).isEmpty());
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}