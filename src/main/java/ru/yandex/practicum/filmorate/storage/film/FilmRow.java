package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class FilmRow {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Long mpaId;

}
