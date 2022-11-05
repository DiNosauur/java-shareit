package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserService {
    Collection<User> getAllUsers();

    User saveUser(User user);

    Optional<User> updateUser(User user);

    boolean deleteUser(long id);

    Optional<User> getUser(long id);
}
