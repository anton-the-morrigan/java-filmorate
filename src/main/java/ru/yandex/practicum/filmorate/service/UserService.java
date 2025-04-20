package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class UserService {
    InMemoryUserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long id, Long friendId) {
        userStorage.showUser(id).getFriends().add(friendId);
        log.info(String.format("Пользователь %d теперь дружит с пользователем %d", id, friendId));
        userStorage.showUser(friendId).getFriends().add(id);
        log.info(String.format("Пользователь %d теперь дружит с пользователем %d", friendId, id));
    }

    public void removeFriend(Long id, Long friendId) {

        if (!userStorage.getUsers().get(id).getFriends().contains(friendId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден в друзьях у пользователя с id %d", id, friendId));
        }
        if (!userStorage.getUsers().get(friendId).getFriends().contains(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден в друзьях у пользователя с id %d", friendId, id));
        }

        userStorage.showUser(id).getFriends().remove(friendId);
        log.info(String.format("Пользователь %d больше не дружит с пользователем %d", id, friendId));
        userStorage.showUser(friendId).getFriends().remove(id);
        log.info(String.format("Пользователь %d больше не дружит с пользователем %d", friendId, id));
    }

    public Collection<User> showFriends(Long id) {
        Collection<User> friends = new ArrayList<>();
        for (Long friend : userStorage.getUsers().get(id).getFriends()) {
            friends.add(userStorage.getUsers().get(friend));
        }
        return friends;
    }

    public Collection<User> showCommonFriends(Long id, Long otherId) {
        Collection<User> commonFriends = new ArrayList<>();
        for (Long friend : userStorage.showUser(id).getFriends()) {
            if (userStorage.showUser(otherId).getFriends().contains(friend)) {
                commonFriends.add(userStorage.showUser(friend));
            }
        }
        return commonFriends;
    }
}
