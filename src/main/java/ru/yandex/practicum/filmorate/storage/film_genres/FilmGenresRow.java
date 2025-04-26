package ru.yandex.practicum.filmorate.storage.film_genres;

import lombok.Data;

@Data
public class FilmGenresRow {
    private Long filmId;
    private Long genreId;
}
