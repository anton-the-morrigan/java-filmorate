package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final InMemoryFilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(InMemoryFilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    @GetMapping
    public Collection<Film> showAllFilms() {
        return filmStorage.showAllFilms();
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void likeFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/films/popular")
    public Collection<Film> showMostLikedFilms(@RequestParam(value = "count", defaultValue = "10", required = false) Integer count) {
        if (count < 0) {
            throw new ValidationException("count", "Количество должно быть больше нуля");
        }
        return filmService.showMostLikedFilms(count);
    }

}
