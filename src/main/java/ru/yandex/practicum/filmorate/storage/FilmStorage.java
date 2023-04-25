package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getFilms();

    Film getFilm(int id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    void validationFilm(Film film);

    void addLike(int id, int userId);

    void deleteLike(int id, int userId);
}
