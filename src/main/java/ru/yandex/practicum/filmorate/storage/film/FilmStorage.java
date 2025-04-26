package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public FilmRow addFilm(FilmRow film);

    public FilmRow updateFilm(FilmRow newFilm);

    public FilmRow showFilm(Long id);

    public Collection<FilmRow> showAllFilms();
}
