package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
@Service
public class UserService {
    UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(UserDbStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Long id, Long friendId) {
        if (!userStorage.dbContainsUser(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        if (!userStorage.dbContainsUser(friendId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", friendId));
        }
        String sql = "INSERT INTO friend_requests (user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, id, friendId);
        log.info("Пользователь {} теперь дружит с пользователем {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        if (!userStorage.dbContainsUser(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        if (!userStorage.dbContainsUser(friendId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", friendId));
        }
        String sql = "DELETE FROM friend_requests WHERE user_id = ? AND friend_id = ?";
        if (jdbcTemplate.update(sql, id, friendId) == 0) {
            throw new NoContentException(String.format("Пользователь с id %d не найден в друзьях у пользователя с id %d", id, friendId));
        }
        log.info("Пользователь {} больше не дружит с пользователем {}", id, friendId);
    }

    public Collection<User> showFriends(Long id) {
        if (userStorage.dbContainsUser(id)) {
            String sql = "SELECT * FROM users WHERE user_id IN (SELECT friend_id FROM friend_requests WHERE user_id = ?)";
            return jdbcTemplate.query(sql, this::userMapper, id);
        }
        throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
    }

    public Collection<User> showCommonFriends(Long id, Long otherId) {
        if (!userStorage.dbContainsUser(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        if (!userStorage.dbContainsUser(otherId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", otherId));
        }
        String sql = "SELECT * FROM users WHERE user_id IN (SELECT friend_id FROM friend_requests WHERE user_id = ? OR user_id = ?)";
        return jdbcTemplate.query(sql, this::userMapper, id, otherId);
    }

    private User userMapper(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
    }
}
