package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    public Genre showGenre(Long id);

    public Collection<Genre> showAllGenres();
}
