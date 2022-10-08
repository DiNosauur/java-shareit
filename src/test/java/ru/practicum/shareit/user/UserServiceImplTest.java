package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ConflictException;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
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
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void getAllUsers() {

        List<User> sourceUsers = List.of(
                makeUser("ivan@mail.ru", "Ivan"),
                makeUser("petr@mail.ru", "Petr"),
                makeUser("vasilii@mail.ru", "Vasilii")
        );

        for (User sourceUser : sourceUsers) {
            em.persist(sourceUser);
        }
        em.flush();

        Collection<User> targetUsers = service.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (User sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    @Test
    void saveUser() {
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(mockRepository);

        User saveUser = makeUser("dimano@mail.ru", "Dima");
        saveUser.setId(1L);

        Mockito
                .when(mockRepository.save(Mockito.any()))
                .thenReturn(saveUser);

        User checkUser = userService.saveUser(saveUser);

        Assertions.assertEquals(saveUser, checkUser);
    }

    @Test
    void updateUser() {
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(mockRepository);

        User getUser = makeUser("dimano@mail.ru", "Dima");
        getUser.setId(1L);

        Mockito
                .when(mockRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.of(getUser));

        final ConflictException exception = Assertions.assertThrows(
                ConflictException.class,
                () -> userService.updateUser(makeUser("dimano@mail.ru", "DiNо")));

        Assertions.assertEquals("Пользователь с таким email уже зарегестрирован", exception.getMessage());

        Mockito
                .when(mockRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.empty());

        Mockito
                .when(mockRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(getUser));

        User updUser = userService.updateUser(makeUser("dimano@yandex.ru", "Dmitriy")).get();

        Assertions.assertEquals("dimano@yandex.ru", updUser.getEmail());
        Assertions.assertEquals("Dmitriy", updUser.getName());
    }

    @Test
    void deleteUser() {
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(mockRepository);

        User getUser = makeUser("dimano@mail.ru", "Dima");
        getUser.setId(1L);

        Mockito
                .when(mockRepository.findById(1L))
                .thenReturn(Optional.of(getUser));

        Assertions.assertEquals(true, userService.deleteUser(1L));

        Assertions.assertEquals(false, userService.deleteUser(2L));
    }

    @Test
    void getUser() {
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(mockRepository);

        User getUser = makeUser("dimano@mail.ru", "Dima");
        getUser.setId(1L);

        Mockito
                .when(mockRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(getUser));

        User checkUser = userService.getUser(1L).get();

        Assertions.assertEquals(getUser, checkUser);
    }
}