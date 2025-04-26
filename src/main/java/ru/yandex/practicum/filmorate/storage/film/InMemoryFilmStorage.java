//package ru.yandex.practicum.filmorate.storage.film;
//
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//
//import java.time.LocalDate;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class InMemoryFilmStorage implements FilmStorage {
//    @Getter
//    private final Map<Long, Film> films = new HashMap<>();
//    Long filmId = 1L;
//
//    public Film addFilm(Film film) {
//        filmValidator(film);
//        film.setId(filmId);
//        filmId++;
//        films.put(film.getId(), film);
//        return film;
//    }
//
//    public Film updateFilm(Film newFilm) {
//        if (newFilm.getId() == null) {
//            throw new ConditionsNotMetException("Id должен быть указан");
//        }
//        if (films.containsKey(newFilm.getId())) {
//            Film oldFilm = films.get(newFilm.getId());
//            filmValidator(newFilm);
//            oldFilm.setName(newFilm.getName());
//            oldFilm.setDescription(newFilm.getDescription());
//            oldFilm.setReleaseDate(newFilm.getReleaseDate());
//            oldFilm.setDuration(newFilm.getDuration());
//            log.info("Данные фильма обновлены");
//            return oldFilm;
//        }
//        throw new NotFoundException(String.format("Фильм с id %d не найден", newFilm.getId()));
//    }
//
//    public Film showFilm(Long id) {
//        if (!getFilms().containsKey(id)) {
//            throw new NotFoundException(String.format("Фильм с id %d не найден", id));
//        }
//        return getFilms().get(id);
//    }
//
//    public Collection<Film> showAllFilms() {
//        return films.values();
//    }
//
//    private void filmValidator(Film film) {
//        if (film.getName() == null || film.getName().isBlank()) {
//            throw new ConditionsNotMetException("Название не может быть пустым");
//        }
//        log.info("Выбрано название фильма: {}", film.getName());
//        if (film.getDescription().length() > 200) {
//            throw new ConditionsNotMetException("Длина описания не может превышать 200 символов");
//        }
//        log.info("Выбрано описание фильма: {}", film.getDescription());
//        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
//            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года");
//        }
//        log.info("Выбрана дата релиза фильма: {}", film.getReleaseDate());
//        if (film.getDuration() < 0) {
//            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
//        }
//        log.info("Выбрана продолжительность фильма: {}", film.getDuration());
//    }
//}
