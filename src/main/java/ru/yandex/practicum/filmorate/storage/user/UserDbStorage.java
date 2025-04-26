package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User createUser(User user) {
        userValidator(user);
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, new Date(user.getBirthday().getYear(), user.getBirthday().getMonthValue(), user.getBirthday().getDayOfMonth()) );
            return ps;
        }, keyHolder);

        user.setId((keyHolder.getKey()).longValue());
        return user;
    }

    public User updateUser(User newUser) {
        userValidator(newUser);
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (dbContainsUser(newUser.getId())) {
            String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
            jdbcTemplate.update(sql, newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(), newUser.getId());
            return newUser;
        }
        throw new NotFoundException(String.format("Пользователь с id %d не найден", newUser.getId()));
    }

    public User showUser(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        if (dbContainsUser(id)) {
            return jdbcTemplate.queryForObject(sql, this::userMapper, id);
        }
        throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
    }

    public Collection<User> showAllUsers() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        return jdbcTemplate.query(sql, this::userMapper);
    }

    public boolean dbContainsUser(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            jdbcTemplate.queryForObject(sql, this::userMapper, id);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
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
