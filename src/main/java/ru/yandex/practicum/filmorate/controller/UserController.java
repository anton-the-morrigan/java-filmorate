package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    Integer userId = 0;

    @PostMapping
    public User createUser(@RequestBody User user) {
        userValidator(user);
        user.setId(userId);
        userId++;
        users.put(user.getId(), user);
        return user;
    }

    @PatchMapping
    public User updateUser(@RequestBody User newUser) {
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

    @GetMapping
    public Collection<User> showAllUsers() {
        return users.values();
    }

    void userValidator(User user) {
        if (!user.getEmail().contains("@") || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ");
        }
        log.info("Пользователь ввёл электронную почту: {}", user.getEmail());
        if (user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            throw new ConditionsNotMetException("логин не может быть пустым и содержать пробел");
        }
        log.info("Пользователь ввёл логин: {}", user.getLogin());
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователь не ввёл имя, поэтому вместо него используется логин");
        }
        log.info("Пользователь ввёл имя: {}", user.getName());
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
        log.info("Пользователь ввёл дату рождения: {}", user.getBirthday());
    }
}
