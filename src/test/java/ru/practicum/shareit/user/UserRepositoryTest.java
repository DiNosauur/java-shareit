package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository repository;

    private User user = new User();

    @BeforeEach
    void setUp() {
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
        em.persist(user);
    }

    @Test
    void findByEmailIsPresent() {
        Optional<User> foundItem = repository.findByEmail("dimano@mail.ru");
        assertThat(foundItem.isPresent());
    }

    @Test
    void findByEmailIsEmpty() {
        Optional<User> foundItem = repository.findByEmail("dimano2@mail.ru");
        assertThat(foundItem.isEmpty());
    }
}