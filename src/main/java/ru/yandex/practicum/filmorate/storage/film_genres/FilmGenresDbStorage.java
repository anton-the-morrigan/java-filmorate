package ru.yandex.practicum.filmorate.storage.film_genres;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class FilmGenresDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmGenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setGenresForFilm(Long filmId, Collection<Long> genreIds) {
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, filmId);

        for (Long genreId : new HashSet<>(genreIds)) {
            String insertGenreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.update(insertGenreSql, filmId, genreId);
        }
    }

    public Collection<Long> getGenresForFilm(Long filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        return jdbcTemplate.query(sql, this::genreIdMapper, filmId);
    }

    private Long genreIdMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("genre_id");
    }
}
