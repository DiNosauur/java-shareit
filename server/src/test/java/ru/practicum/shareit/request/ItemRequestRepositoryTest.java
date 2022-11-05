package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository repository;

    private User user = new User();

    private ItemRequest itemRequest = new ItemRequest();

    private long userId;

    @BeforeEach
    void setUp() {
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
        userId = em.persist(user).getId();

        itemRequest.setRequestor(userId);
        itemRequest.setCreated(LocalDateTime.of(2022, 10, 1, 1, 1, 1));
        itemRequest.setDescription("4-местная байдарка");
        em.persist(itemRequest);
    }

    @Test
    void findByRequestorOrderByCreatedDesc() {
        Page<ItemRequest> foundItemRequests = repository
                .findByRequestorOrderByCreatedDesc(userId, PageRequest.of(0, 20));
        assertThat(foundItemRequests).hasSize(1);
    }
}