package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    @Getter
    private final Map<Long, User> users = new HashMap<>();
    Long userId = 1L;

    public User createUser(User user) {
        userValidator(user);
        user.setId(userId);
        userId++;
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            userValidator(newUser);
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Данные пользователя обновлены");
            return oldUser;
        }
        throw new NotFoundException(String.format("Пользователь с id %d не найден", newUser.getId()));
    }

    public User showUser(Long id) {
        if (!getUsers().containsKey(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        return getUsers().get(id);
    }

    public Collection<User> showAllUsers() {
        return users.values();
    }

    private void userValidator(User user) {
        if (!user.getEmail().contains("@") || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ");
        }
        log.info("Пользователь ввёл электронную почту: {}", user.getEmail());
        if (user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробел");
        }
        log.info("Пользователь ввёл логин: {}", user.getLogin());
        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.info("Пользователь не ввёл имя, поэтому вместо него используется логин");
        } else {
            log.info("Пользователь ввёл имя: {}", user.getName());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
        log.info("Пользователь ввёл дату рождения: {}", user.getBirthday());
    }
}
