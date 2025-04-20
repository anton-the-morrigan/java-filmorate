package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;


public interface UserStorage {

    public User createUser(User user);

    public User updateUser(User newUser);

    public Collection<User> showAllUsers();
}
