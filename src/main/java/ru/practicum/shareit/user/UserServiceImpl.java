package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Collection<User> getAllUsers() {
        return repository.findAll();
    }

    private void validate(User user) {
        Optional<User> foundUser = repository.findByEmail(user.getEmail());
        if (foundUser.isPresent() && foundUser.get().getId() != user.getId()) {
            throw new ConflictException("Пользователь с таким email уже зарегестрирован");
        }
    }

    @Transactional
    @Override
    public User saveUser(User user) {
        log.info("Добавление пользователя {}", user.toString());
        return repository.save(user);
    }

    @Transactional
    @Override
    public Optional<User> updateUser(User user) {
        log.info("Редактирование пользователя {}", user.toString());
        validate(user);
        Optional<User> changeableUser = getUser(user.getId());
        if (changeableUser.isPresent()) {
            if (user.getEmail() != null) {
                changeableUser.get().setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                changeableUser.get().setName(user.getName());
            }
            repository.save(changeableUser.get());
        }
        return changeableUser;
    }

    @Transactional
    @Override
    public boolean deleteUser(long id) {
        log.info("Удаление пользователя (id={})", id);
        Optional<User> user = getUser(id);
        if (user.isPresent()) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<User> getUser(long id) {
        return repository.findById(id);
    }
}
