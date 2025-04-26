package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRow;
import ru.yandex.practicum.filmorate.storage.film_genres.FilmGenresDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    UserDbStorage userStorage;
    FilmDbStorage filmStorage;
    GenreDbStorage genreStorage;
    FilmGenresDbStorage filmGenresStorage;
    MpaDbStorage mpaStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(UserDbStorage userStorage, FilmDbStorage filmStorage, GenreDbStorage genreStorage, FilmGenresDbStorage filmGenresStorage, MpaDbStorage mpaStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.filmGenresStorage = filmGenresStorage;
        this.mpaStorage = mpaStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film create(Film film) {
        filmValidator(film);

        FilmRow filmRow = mapFilmToFilmRow(film);
        filmRow = filmStorage.addFilm(filmRow);
        if (film.getGenres() != null) {
            filmGenresStorage.setGenresForFilm(filmRow.getId(), film.getGenres().stream().map(Genre::getId).toList());
        }
        return getById(filmRow.getId());
    }

    public Film update(Film film) {
        filmValidator(film);

        FilmRow filmRow = mapFilmToFilmRow(film);
        filmStorage.updateFilm(filmRow);
        if (film.getGenres() != null) {
            filmGenresStorage.setGenresForFilm(filmRow.getId(), film.getGenres().stream().map(Genre::getId).toList());
        }
        return getById(filmRow.getId());
    }

    public Film getById(Long id) {
        FilmRow filmRow = filmStorage.showFilm(id);
        Film film = new Film();
        film.setId(filmRow.getId());
        film.setName(filmRow.getName());
        film.setDescription(filmRow.getDescription());
        film.setReleaseDate(filmRow.getReleaseDate());
        film.setDuration(filmRow.getDuration());
        if (filmRow.getMpaId() != null) {
            film.setMpa(mpaStorage.showMpa(filmRow.getMpaId()));
        }
        List<Genre> genres = filmGenresStorage.getGenresForFilm(filmRow.getId()).stream().map(genreId -> {
            return genreStorage.showGenre(genreId);
        }).toList();

        film.setGenres(genres);
        return film;
    }

    public Collection<Film> getAll() {
        return filmStorage.showAllFilms().stream().map(FilmRow::getId).map(this::getById).toList();
    }

    private FilmRow mapFilmToFilmRow(Film film) {
        FilmRow filmRow = new FilmRow();
        filmRow.setId(film.getId());
        filmRow.setName(film.getName());
        filmRow.setDescription(film.getDescription());
        filmRow.setReleaseDate(film.getReleaseDate());
        filmRow.setDuration(film.getDuration());
        filmRow.setMpaId(film.getMpa().getId());
        return filmRow;
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
        String sql = "SELECT count(*) AS likes_count, film_id AS film_id from likes GROUP BY film_id ORDER BY count(*) DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::filmIdMapper, count).stream().map(this::getById).toList();
    }

    private Long filmIdMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("film_id");
    }

    private void filmValidator(Film film) {
        try {
            mpaStorage.showMpa(film.getMpa().getId());
        } catch (RuntimeException e) {
            throw new NotFoundException("Данный MPA не найден");
        }

        if (film.getGenres() != null) {
            try {
                for (Genre genre : film.getGenres()) {
                    genreStorage.showGenre(genre.getId());
                }
            } catch (RuntimeException e) {
                throw new NotFoundException("Данный жанр не найден");
            }
        }

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
