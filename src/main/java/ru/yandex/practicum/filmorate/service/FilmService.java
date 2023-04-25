package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(int id, int userId) {
        if (filmStorage.getFilm(id) == null) {
            throw new NullPointerException("Такого фильма не существует");
        } else {
            filmStorage.addLike(id, userId);
        }
    }

    public void deleteLike(int id, int userId) {
        if (filmStorage.getFilm(id) == null) {
            throw new NullPointerException("Такого фильма не существует");
        } else {
            filmStorage.deleteLike(id, userId);
        }
    }

    public List<Film> getPopularFilms(Integer count) {
        Set<Film> popularFilms = new TreeSet<>(Comparator.comparingInt(Film::getRate)
                .thenComparing(Film::getId).reversed());
        popularFilms.addAll(filmStorage.getFilms());
        return new ArrayList<Film>(popularFilms.stream().limit(count).collect(Collectors.toList()));
    }
}