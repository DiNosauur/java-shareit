package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
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
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService service;

    @Test
    void findUserItems() {
        User user = new User();
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
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

        Collection<ItemFullDto> targetItems = service.findUserItems(user.getId());

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (Item sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("name", equalTo(sourceItem.getName()))
            )));
        }
    }

    private Item makeItem(long userId, String name, String desc) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(desc);
        item.setAvailable(true);
        item.setOwner(userId);
        return item;
    }
}