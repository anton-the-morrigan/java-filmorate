package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {

    public Mpa showMpa(Long id);

    public Collection<Mpa> showAllMpa();
}
