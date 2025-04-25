package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre showGenre(Long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::genreMapper, id);
        } catch (RuntimeException e) {
            throw new NotFoundException(String.format("Жанр с id %d не найден", id));
        }
    }

    public Collection<Genre> showAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, this::genreMapper);
    }

    private Genre genreMapper(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getLong("genre_id"));
        genre.setName(resultSet.getString("genre_name"));
        return genre;
    }
}
