package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {
    private final InMemoryUserStorage userStorage;
    private final UserService userService;

    private final String friendsPath = "/{id}/friends/{friend-id}";

    public UserController(InMemoryUserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userStorage.createUser(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userStorage.updateUser(newUser);
    }

    @GetMapping
    public Collection<User> showAllUsers() {
        return userStorage.showAllUsers();
    }

    @GetMapping("/{id}")
    public User showUser(@PathVariable("id") Long id) {
        return userStorage.showUser(id);
    }

    @PutMapping(friendsPath)
    public void addFriend(@PathVariable("id") Long id, @PathVariable("friend-id") Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping(friendsPath)
    public void removeFriend(@PathVariable("id") Long id, @PathVariable("friend-id") Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> showFriends(@PathVariable("id") Long id) {
        return userService.showFriends(id);
    }

    @GetMapping("/{id}/friends/common/{other-id}")
    public Collection<User> showCommonFriends(@PathVariable("id") Long id, @PathVariable("other-id") Long otherId) {
        return userService.showCommonFriends(id, otherId);
    }

}
