package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    UserDbStorage userStorage;
    FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(UserDbStorage userStorage, FilmDbStorage filmStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void likeFilm(Long id, Long userId) {
        if (!filmStorage.dbContainsFilm(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден", id));
        }
        if (!userStorage.dbContainsUser(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        String sql = "INSERT INTO likes (film_id, user_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    public void unlikeFilm(Long id, Long userId) {
        if (!filmStorage.dbContainsFilm(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден", id));
        }
        if (!userStorage.dbContainsUser(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        if (jdbcTemplate.update(sql, id, userId) == 0) {
            throw new NoContentException(String.format("Фильм с id %d не найден в понравившихся у пользователя с id %d", id, userId));
        }
        log.info("Пользователь {} убрал лайк фильму {}", userId, id);
    }

    public Collection<Film> showMostLikedFilms(Integer count) {
        String sql = "SELECT films.* FROM films JOIN likes on films.film_id = likes.film_id GROUP BY films.film_id ORDER BY COUNT(likes.user_id) DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::filmMapper, count);
    }

    private Film filmMapper(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        film.setGenre(resultSet.getInt("genre"));
        film.setMpa(resultSet.getInt("mpa"));
        return film;
    }
}
