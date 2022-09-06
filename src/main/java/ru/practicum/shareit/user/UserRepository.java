package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

interface UserRepository {
    Collection<User> findAll();

    User save(User user);

    Optional<User> update(User user);

    boolean delete(long id);

    Optional<User> get(long id);

    boolean checkUser(User user);
}