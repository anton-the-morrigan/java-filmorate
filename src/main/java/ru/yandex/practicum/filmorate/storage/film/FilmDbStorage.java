package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.*;
import java.util.Collection;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FilmRow addFilm(FilmRow film) {
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa) VALUES(?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setLong(5, film.getMpaId());

            return preparedStatement;
        }, keyHolder);

        film.setId((keyHolder.getKey()).longValue());
        return film;
    }

    public FilmRow updateFilm(FilmRow newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (dbContainsFilm(newFilm.getId())) {
            String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
            jdbcTemplate.update(sql, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(), newFilm.getDuration(), newFilm.getMpaId(), newFilm.getId());
            return newFilm;
        }
        throw new NotFoundException(String.format("Фильм с id %d не найден", newFilm.getId()));
    }

    public FilmRow showFilm(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        if (dbContainsFilm(id)) {
            return jdbcTemplate.queryForObject(sql, this::filmMapper, id);
        }
        throw new NotFoundException(String.format("Фильм с id %d не найден", id));
    }

    public Collection<FilmRow> showAllFilms() {
        String sql = "SELECT * FROM films ORDER BY film_id";
        return jdbcTemplate.query(sql, this::filmMapper);
    }

    public boolean dbContainsFilm(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            jdbcTemplate.queryForObject(sql, this::filmMapper, id);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private FilmRow filmMapper(ResultSet resultSet, int rowNum) throws SQLException {
        FilmRow film = new FilmRow();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        film.setMpaId(resultSet.getLong("mpa"));
        return film;
    }
}
