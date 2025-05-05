package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Mpa mpa;

    Collection<Genre> genres;
    Collection<Integer> likes;
}
