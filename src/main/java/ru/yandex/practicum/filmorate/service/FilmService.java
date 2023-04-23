package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.database.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmDbStorage filmStorage;

    @Autowired
    public FilmService(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(int id, int userId) {
        if (filmStorage.getFilm(id) == null || userId < 0) {
            throw new NullPointerException("Такого фильма не существует");
        } else {
            filmStorage.addLike(id, userId);
        }
    }

    public void deleteLike(int id, int userId) {
        if (filmStorage.getFilm(id) == null || userId < 0) {
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
