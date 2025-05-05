package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilmRow {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Long mpaId;

}
