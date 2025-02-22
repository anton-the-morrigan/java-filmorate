package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    Integer filmId = 1;

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        filmValidator(film);
        film.setId(filmId);
        filmId++;
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            filmValidator(newFilm);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(oldFilm.getDuration());
            log.info("Данные фильма обновлены");
            return oldFilm;
        }
        throw new NotFoundException(String.format("Фильм с id %d не найден", newFilm.getId()));
    }

    @GetMapping
    public Collection<Film> showAllFilms() {
        return films.values();
    }

    void filmValidator(Film film) {
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
        if (film.getDuration().isNegative()) {
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        }
        log.info("Выбрана продолжительность фильма: {}", film.getDuration());
    }

}
