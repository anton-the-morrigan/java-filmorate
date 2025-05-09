package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    private final String likePath = "/{id}/like/{user-id}";

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping
    public Collection<Film> showAllFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film showFilm(@PathVariable("id") Long id) {
        return filmService.getById(id);
    }

    @PutMapping(likePath)
    public void likeFilm(@PathVariable("id") Long id, @PathVariable("user-id") Long userId) {
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping(likePath)
    public void unlikeFilm(@PathVariable("id") Long id, @PathVariable("user-id") Long userId) {
        filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> showMostLikedFilms(@RequestParam(value = "count", defaultValue = "10", required = false) Integer count) {
        if (count < 0) {
            throw new ValidationException("count", "Количество должно быть больше нуля");
        }
        return filmService.showMostLikedFilms(count);
    }

}
