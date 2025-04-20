package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
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
        if (!filmStorage.getFilms().containsKey(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден", id));
        }
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", userId));
        }

        filmStorage.getFilms().get(id).getLikes().add(userId);
        userStorage.getUsers().get(userId).getLikedFilms().add(id);
    }

    public void unlikeFilm(Long id, Long userId) {
        if (!filmStorage.getFilms().containsKey(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден", id));
        }
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", userId));
        }

        if (!filmStorage.getFilms().get(id).getLikes().contains(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %d не отметил фильм с id %d понравившимся", userId, id));
        }
        if (!userStorage.getUsers().get(userId).getLikedFilms().contains(id)) {
            throw new NotFoundException(String.format("Фильм с id %d не найден в понравившихся у пользователя с id %d", id, userId));
        }

        filmStorage.getFilms().get(id).getLikes().remove(userId);
        userStorage.getUsers().get(userId).getLikedFilms().remove(id);
    }

    public Collection<Film> showMostLikedFilms(Integer count) {
        return filmStorage.getFilms().values().stream().sorted((f0, f1) -> {
            int comp = f0.getLikesAmount().compareTo(f1.getLikesAmount());
            return comp;
        }).limit(count).collect(Collectors.toList());
    }
}
