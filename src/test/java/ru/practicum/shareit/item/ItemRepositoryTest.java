package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository repository;

    private User user = new User();

    private Item item = new Item();

    @BeforeEach
    void setUp() {
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");

        item.setName("Дрель");
        item.setDescription("Инструмент для сверления");
        item.setAvailable(true);
        item.setOwner(em.persist(user).getId());
        em.persist(item);
    }

    @Test
    void search() {
        Collection<Item> foundItems = repository.search("дрель");
        assertThat(foundItems).hasSize(1);
    }
}