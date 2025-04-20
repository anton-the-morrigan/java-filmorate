package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    InMemoryUserStorage userStorage;
    InMemoryFilmStorage filmStorage;

    @Autowired
    public FilmService(InMemoryUserStorage userStorage, InMemoryFilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void likeFilm(Long id, Long userId) {
        filmStorage.showFilm(id).getLikes().add(userId);
        userStorage.showUser(userId).getLikedFilms().add(id);
        updateLikeAmount(id);
    }

    public void unlikeFilm(Long id, Long userId) {
        if (!filmStorage.showFilm(id).getLikes().contains(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не отметил фильм с id %d понравившимся", userId, id));
        }
        if (!userStorage.showUser(userId).getLikedFilms().contains(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден в понравившихся у пользователя с id %d", id, userId));
        }

        filmStorage.showFilm(id).getLikes().remove(userId);
        userStorage.showUser(userId).getLikedFilms().remove(id);
        updateLikeAmount(id);
    }

    public Collection<Film> showMostLikedFilms(Integer count) {
        return filmStorage.getFilms().values().stream().sorted((f0, f1) -> {
            int comp = f0.getLikesAmount().compareTo(f1.getLikesAmount());
            return (-1) * comp;
        }).limit(count).collect(Collectors.toList());
    }

    private void updateLikeAmount(Long id) {
        filmStorage.showFilm(id).setLikesAmount(filmStorage.getFilms().get(id).getLikes().size());
    }
}
