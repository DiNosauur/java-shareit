package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Collection<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public User saveUser(User user) {
        return repository.save(user);
    }

    @Override
    public Optional<User> updateUser(User user) {
        return repository.update(user);
    }

    @Override
    public boolean deleteUser(long id) {
        return repository.delete(id);
    }

    @Override
    public Optional<User> getUser(long id) {
        return repository.get(id);
    }

    @Override
    public boolean checkUser(User user) {
        return repository.checkUser(user);
    }
}
