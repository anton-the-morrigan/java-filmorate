package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film addFilm(Film film) {
        filmValidator(film);
        String sql = "INSERT INTO films(name, description, release_date, duration, genre, mpa) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getGenre(), film.getMpa());
        return film;
    }

    public Film updateFilm(Film newFilm) {
        filmValidator(newFilm);
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (dbContainsFilm(newFilm.getId())) {
            String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, genre = ?, mpa = ? WHERE film_id = ?";
            jdbcTemplate.update(sql, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(), newFilm.getDuration(), newFilm.getGenre(), newFilm.getMpa(), newFilm.getId());
            return newFilm;
        }
        throw new NotFoundException(String.format("Фильм с id %d не найден", newFilm.getId()));
    }

    public Film showFilm(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        if (dbContainsFilm(id)) {
            return jdbcTemplate.queryForObject(sql, this::filmMapper, id);
        }
        throw new NotFoundException(String.format("Фильм с id %d не найден", id));
    }

    public Collection<Film> showAllFilms() {
        String sql = "SELECT * FROM films ORDER BY film_id";
        return jdbcTemplate.query(sql, this::filmMapper);
    }

    public boolean dbContainsFilm(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            jdbcTemplate.queryForObject(sql, this::filmMapper, id);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
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

    private void filmValidator(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
        log.info("Выбрано название фильма: {}", film.getName());
        if (film.getDescription().length() > 200) {
            throw new ConditionsNotMetException("Длина описания не может превышать 200 символов");
        }
        log.info("Выбрано описание фильма: {}", film.getDescription());
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        log.info("Выбрана дата релиза фильма: {}", film.getReleaseDate());
        if (film.getDuration() < 0) {
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        }
        log.info("Выбрана продолжительность фильма: {}", film.getDuration());
    }
}
