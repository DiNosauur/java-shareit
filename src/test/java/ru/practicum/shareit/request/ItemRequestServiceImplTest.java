package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
class ItemRequestServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;

    @Test
    void findUserItemRequests() {
        User user = new User();
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
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

        Collection<ItemRequestFullDto> targetItemRequests = service.findUserItemRequests(user.getId(), 0, 20);

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
        User user = new User();
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
        long userId = userService.saveUser(user).getId();

        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "палатка");
        service.saveItemRequest(itemRequestDto, userId);

        TypedQuery<ItemRequest> query =
                em.createQuery("Select r from ItemRequest r where r.description = :desc", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("desc", itemRequestDto.getDescription()).getSingleResult();

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
    }
}